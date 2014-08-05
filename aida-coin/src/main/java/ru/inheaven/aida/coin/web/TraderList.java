package ru.inheaven.aida.coin.web;

import com.xeiam.xchange.dto.marketdata.Ticker;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.GlyphIconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarAjaxLink;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.effects.HighlightEffect;
import org.odlabs.wiquery.ui.effects.HighlightEffectJavaScriptResourceReference;
import ru.inheaven.aida.coin.entity.ExchangeMessage;
import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.Exchanges;
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.service.TraderBean;
import ru.inheaven.aida.coin.service.TraderService;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.*;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *          Date: 07.01.14 20:54
 */
public class TraderList extends AbstractPage{
    @EJB
    private TraderBean traderBean;

    @EJB
    private TraderService traderService;

    private Map<ExchangePair, Component> lastMap = new HashMap<>();

    public TraderList() {
        //start service
        traderService.getBittrexExchange();

        add(new BootstrapLink<String>("add", Buttons.Type.Link) {
            @Override
            public void onClick() {
                setResponsePage(TraderEdit.class);
            }
        }.setIconType(GlyphIconType.plus).setLabel(Model.of("Добавить")));

        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new PropertyColumn<>(Model.of("Рынок"), "exchange"));
        list.add(new PropertyColumn<>(Model.of("Монета"), "pair"));
        list.add(new PropertyColumn<>(Model.of("Верх"), "high"));
        list.add(new PropertyColumn<>(Model.of("Низ"), "low"));
        list.add(new PropertyColumn<>(Model.of("Объем"), "volume"));
        list.add(new PropertyColumn<>(Model.of("Спред"), "spread"));
        list.add(new AbstractColumn<Trader, String>(Model.of("Дата")){

            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                cellItem.add(DateLabel.forDateStyle(componentId, new PropertyModel<>(rowModel, "date"), "SS"));
            }
        });
        list.add(new AbstractColumn<Trader, String>(Model.of("Цена")) {
            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, final IModel<Trader> rowModel) {
                Trader trader = rowModel.getObject();

                Label label = new Label(componentId, Model.of("0"));
                label.setOutputMarkupId(true);

                cellItem.add(label);

                lastMap.put(new ExchangePair(trader.getExchange(), trader.getPair()), label);
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

        table.add(new WebSocketBehavior() {
            @Override
            protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message) {
                if (message instanceof ExchangeMessage) {
                    ExchangeMessage exchangeMessage = (ExchangeMessage) message;

                    if (exchangeMessage.getPayload() instanceof Ticker) {
                        Ticker ticker = (Ticker) exchangeMessage.getPayload();

                        Component component = lastMap.get(ExchangePair.of(((ExchangeMessage) message).getExchange(),
                                ticker.getCurrencyPair()));

                        if (component != null){
                            int compare = ticker.getLast().compareTo(new BigDecimal(component.getDefaultModelObjectAsString()));

                            if (compare != 0){
                                String color = compare > 0 ? "'#A9F5A9'" : "'#F5A9A9'";

                                handler.appendJavaScript(new JsStatement().$(component)
                                        .chain("effect", "\"highlight\"", "{color: " + color + "}")
                                        .render());

                                component.setDefaultModelObject(ticker.getLast().toString());

                                handler.add(component);
                            }
                        }
                    }
                }
            }
        });

        Label testLabel =  new Label("test_label", Model.of("subscribe"));
        testLabel.setOutputMarkupId(true);
        add(testLabel);

        add(new BootstrapLink<String>("test", Buttons.Type.Link) {
            @Override
            public void onClick() {

            }
        }.setIconType(GlyphIconType.warningsign).setLabel(Model.of("test")));
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(HighlightEffectJavaScriptResourceReference.get()));
    }
}
