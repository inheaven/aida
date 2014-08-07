package ru.inheaven.aida.coin.web;

import com.google.common.base.Throwables;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.common.NotificationPanel;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.GlyphIconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.table.TableBehavior;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.effects.HighlightEffectJavaScriptResourceReference;
import ru.inheaven.aida.coin.entity.ExchangeMessage;
import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.service.TraderBean;
import ru.inheaven.aida.coin.service.TraderService;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static org.apache.wicket.model.Model.of;

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
    private Map<ExchangePair, Component> buyMap = new HashMap<>();
    private Map<ExchangePair, Component> sellMap = new HashMap<>();

    private NotificationPanel notificationPanel;

    public TraderList() {
        //start service
        traderService.getBittrexExchange();

        notificationPanel = new NotificationPanel("notification");
        notificationPanel.setOutputMarkupId(true);
        add(notificationPanel);

        add(new BootstrapLink<String>("add", Buttons.Type.Link) {
            @Override
            public void onClick() {
                setResponsePage(TraderEdit.class);
            }
        }.setIconType(GlyphIconType.plus).setLabel(of("Добавить")));

        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new PropertyColumn<>(of("Рынок"), "exchange"));
        list.add(new PropertyColumn<>(of("Монета"), "pair"));
        list.add(new PropertyColumn<>(of("Верх"), "high"));
        list.add(new PropertyColumn<>(of("Низ"), "low"));
        list.add(new PropertyColumn<>(of("Объем"), "volume"));
        list.add(new PropertyColumn<>(of("Спред"), "spread"));
        list.add(new TraderColumn(of("Цена"), lastMap));
        list.add(new TraderColumn(of("Покупка"), buyMap));
        list.add(new TraderColumn(of("Продажа"), sellMap));
        list.add(new AbstractColumn<Trader, String>(of("Работа")){
            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                cellItem.add(new Label(componentId, Model.of(rowModel.getObject().isRunning() ? "Да" : "Нет")));
            }
        });

        list.add(new AbstractColumn<Trader, String>(of("")) {
            @Override
            public void populateItem(final Item<ICellPopulator<Trader>> cellItem, String componentId, final IModel<Trader> rowModel) {
                cellItem.add(new NavbarAjaxLink(componentId, of("")) {
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
        table.add(new TableBehavior());

        add(table);

        table.add(new WebSocketBehavior() {
            @Override
            protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message) {
                if (message instanceof ExchangeMessage) {
                    ExchangeMessage exchangeMessage = (ExchangeMessage) message;
                    Object payload = exchangeMessage.getPayload();

                    if (payload instanceof Ticker) {
                        Ticker ticker = (Ticker) exchangeMessage.getPayload();

                        Component component = lastMap.get(ExchangePair.of(exchangeMessage.getExchange(),
                                ticker.getCurrencyPair()));

                        update(handler, component, ticker.getLast().toString());

                    }else if (payload instanceof OpenOrders){
                        OpenOrders openOrders = (OpenOrders) exchangeMessage.getPayload();

                        Map<ExchangePair, Integer> countBuyMap = new HashMap<>();
                        Map<ExchangePair, Integer> countSellMap = new HashMap<>();

                        for (ExchangePair ep : buyMap.keySet()){
                            countBuyMap.put(ep, 0);
                            countSellMap.put(ep, 0);
                        }

                        for (LimitOrder order : openOrders.getOpenOrders()){
                            ExchangePair ep = ExchangePair.of(exchangeMessage.getExchange(), order.getCurrencyPair());

                            if (buyMap.get(ep) != null) {
                                switch (order.getType()){
                                    case BID:
                                        countBuyMap.put(ep, countBuyMap.get(ep) + 1);
                                        break;
                                    case ASK:
                                        countSellMap.put(ep, countSellMap.get(ep) + 1);
                                        break;
                                }
                            }
                        }

                        countBuyMap.forEach(new BiConsumer<ExchangePair, Integer>() {
                            @Override
                            public void accept(ExchangePair exchangePair, Integer integer) {
                                update(handler, buyMap.get(exchangePair), integer.toString());
                            }
                        });

                        countSellMap.forEach(new BiConsumer<ExchangePair, Integer>() {
                            @Override
                            public void accept(ExchangePair exchangePair, Integer integer) {
                                update(handler, sellMap.get(exchangePair), integer.toString());
                            }
                        });
                    }else if (payload instanceof Exception){
                        error(Throwables.getRootCause((Throwable) payload).getMessage());

                        handler.add(notificationPanel);
                    }
                }
            }
        });

        Label testLabel =  new Label("test_label", of(""));
        testLabel.setOutputMarkupId(true);
        add(testLabel);

        add(new BootstrapLink<String>("test", Buttons.Type.Link) {
            @Override
            public void onClick() {

            }
        }.setIconType(GlyphIconType.warningsign).setLabel(of("test")));
    }

    private void update(WebSocketRequestHandler handler, Component component, String newValue){
        if (component != null){
            int compare = newValue.compareTo(component.getDefaultModelObjectAsString());

            if (compare != 0){
                String color = compare > 0 ? "'#A9F5A9'" : "'#F5A9A9'";

                handler.appendJavaScript(new JsStatement().$(component)
                        .chain("effect", "\"highlight\"", "{color: " + color + "}")
                        .render());

                component.setDefaultModelObject(newValue);
                handler.add(component);
            }
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forReference(HighlightEffectJavaScriptResourceReference.get()));
    }
}
