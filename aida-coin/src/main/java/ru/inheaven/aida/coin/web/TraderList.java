package ru.inheaven.aida.coin.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.GlyphIconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarAjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.event.IEvent;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.event.WebSocketPushPayload;
import org.apache.wicket.protocol.ws.api.message.ConnectedMessage;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.protocol.ws.api.message.TextMessage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.service.ManagedService;
import ru.inheaven.aida.coin.service.TraderBean;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *          Date: 07.01.14 20:54
 */
public class TraderList extends AbstractPage{
    @EJB
    private TraderBean traderBean;

    @EJB
    private ManagedService managedService;

    private Label testLabel;

    public TraderList() {
        add(new BootstrapLink<String>("add", Buttons.Type.Link) {
            @Override
            public void onClick() {
                setResponsePage(TraderEdit.class);
            }
        }.setIconType(GlyphIconType.plus).setLabel(Model.of("Добавить")));

        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new PropertyColumn<Trader, String>(Model.of("Рынок"), "market"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Товар"), "name"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Открыто"), "open"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Верх"), "high"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Низ"), "low"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Объем"), "volume"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Спред"), "spread"));
        list.add(new AbstractColumn<Trader, String>(Model.of("Дата")){

            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                cellItem.add(DateLabel.forDateStyle(componentId, new PropertyModel<Date>(rowModel, "date"), "MM"));
            }
        });
        list.add(new AbstractColumn<Trader, String>(Model.of("Цена")) {
            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, final IModel<Trader> rowModel) {
                Label label = new Label(componentId, "event bus test");
                label.setOutputMarkupId(true);
                cellItem.add(label);
            }
        });
        list.add(new AbstractColumn<Trader, String>(Model.of("")) {
            @Override
            public void populateItem(final Item<ICellPopulator<Trader>> cellItem, String componentId, final IModel<Trader> rowModel) {
                cellItem.add(new NavbarAjaxLink(componentId, Model.of("Редактировать")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setResponsePage(new TraderEdit(new PageParameters().add("id", rowModel.getObject().getId())));
                    }
                }.setIconType(GlyphIconType.edit));
            }
        });

        DataTable<Trader, String> table = new DataTable<>("traders", list, new ListDataProvider<Trader>(){
            @Override
            protected List<Trader> getData() {
                return traderBean.getTraders();
            }
        }, 100);
        table.setOutputMarkupId(true);
        table.addTopToolbar(new HeadersToolbar<>(table, null));
        add(table);

        testLabel =  new Label("test_label", Model.of("subscribe"));
        testLabel.setOutputMarkupId(true);
        add(testLabel);

        add(new WebSocketBehavior() {
            @Override
            protected void onMessage(WebSocketRequestHandler handler, TextMessage message) {
                testLabel.setDefaultModelObject(message.getText());

                handler.add(testLabel);
            }

            @Override
            protected void onConnect(ConnectedMessage message) {
                System.out.println("CONNECTED!!!");
            }
        });

        add(new BootstrapLink<String>("test", Buttons.Type.Link) {
            @Override
            public void onClick() {
                managedService.startTestTickerUpdateManagedService();

                IWebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(getApplication());

                WebSocketPushBroadcaster broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
                broadcaster.broadcastAll(getApplication(), new IWebSocketPushMessage() {
                    @Override
                    public String toString() {
                        return new Date().toString();
                    }
                });


            }
        }.setIconType(GlyphIconType.warningsign).setLabel(Model.of("test")));
    }

    @Override
    public void onEvent(IEvent<?> event) {
        if (event.getPayload() instanceof WebSocketPushPayload) {
            WebSocketPushPayload wsEvent = (WebSocketPushPayload) event.getPayload();

            wsEvent.getHandler().add(testLabel);

            testLabel.setDefaultModelObject(wsEvent.getMessage().toString());
        }
    }


}
