package ru.inheaven.aida.coin.web;

import com.googlecode.wickedcharts.highcharts.jackson.JsonRenderer;
import com.googlecode.wickedcharts.highcharts.options.*;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.wicket6.highcharts.Chart;
import com.googlecode.wickedcharts.wicket6.highcharts.JsonRendererFactory;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
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
import ru.inheaven.aida.coin.entity.BalanceHistory;
import ru.inheaven.aida.coin.entity.ExchangeMessage;
import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.service.TraderBean;
import ru.inheaven.aida.coin.service.TraderService;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static org.apache.wicket.model.Model.of;
import static ru.inheaven.aida.coin.entity.ExchangeType.*;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *          Date: 07.01.14 20:54
 */
public class TraderList extends AbstractPage{
    @EJB
    private TraderBean traderBean;

    @EJB
    private TraderService traderService;

    private Map<ExchangePair, Component> askMap = new HashMap<>();
    private Map<ExchangePair, Component> bidMap = new HashMap<>();
    private Map<ExchangePair, Component> estimateMap = new HashMap<>();
    private Map<ExchangePair, Component> balanceMap = new HashMap<>();
    private Map<ExchangePair, Component> buyMap = new HashMap<>();
    private Map<ExchangePair, Component> sellMap = new HashMap<>();

    private NotificationPanel notificationPanel;

    private Component bittrexBTC, bittrexCoins;
    private Component cexioBTC, cexioCoins;
    private Component cryptsyBTC, cryptsyCoins;
    private Component btceBTC, btceCoins;

    private BigDecimal lastChartValue = new BigDecimal("0");
    private Integer lastChart2Value = new Integer(0);
    private Chart chart, chart2;
    private int chartIndex = 0;
    private int chart2Index = 0;

    public TraderList() {
        setVersioned(false);

        notificationPanel = new NotificationPanel("notification");
        notificationPanel.setMaxMessages(3);
        notificationPanel.setOutputMarkupId(true);
        add(notificationPanel);

        add(bittrexBTC = new Label("bittrexBTC", Model.of("0")).setOutputMarkupId(true));
        add(bittrexCoins = new Label("bittrexCoins", Model.of("0")).setOutputMarkupId(true));

        add(cexioBTC = new Label("cexioBTC", Model.of("0")).setOutputMarkupId(true));
        add(cexioCoins = new Label("cexioCoins", Model.of("0")).setOutputMarkupId(true));

        add(cryptsyBTC = new Label("cryptsyBTC", Model.of("0")).setOutputMarkupId(true));
        add(cryptsyCoins = new Label("cryptsyCoins", Model.of("0")).setOutputMarkupId(true));

        add(btceBTC = new Label("btceBTC", Model.of("0")).setOutputMarkupId(true));
        add(btceCoins = new Label("btceCoins", Model.of("0")).setOutputMarkupId(true));

        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new PropertyColumn<>(of("Рынок"), "exchange"));
        list.add(new PropertyColumn<>(of("Монета"), "pair"));
        list.add(new TraderColumn(of("Баланс"), balanceMap));
                list.add(new TraderColumn(of("Капитализация"), estimateMap));
        list.add(new AbstractColumn<Trader, String>(of("Лот")) {
            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                Trader trader = rowModel.getObject();
                BigDecimal lot = new BigDecimal("0");

                try {
                    OrderBook orderBook = traderService.getOrderBook(trader.getExchangePair());

                    if (orderBook != null ){
                        lot = trader.getVolume().multiply(orderBook.getAsks().get(0).getLimitPrice())
                                .divide(trader.getHigh().subtract(trader.getLow()).divide(trader.getSpread(),
                                        8, ROUND_HALF_UP), 8, ROUND_HALF_UP);
                    }
                } catch (Exception e) {
                    //zero
                }

                cellItem.add(new Label(componentId, of(lot)));
            }
        });
        list.add(new TraderColumn(of("Покупка"), buyMap));
        list.add(new TraderColumn(of("Продажа"), sellMap));
        list.add(new TraderColumn(of("Спрос"),askMap));
        list.add(new TraderColumn(of("Предложение"), bidMap));
        list.add(new AbstractColumn<Trader, String>(of("Работа")){
            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                cellItem.add(new Label(componentId, Model.of(rowModel.getObject().isRunning() ? "Да" : "Нет")));
            }
        });

        list.add(new AbstractColumn<Trader, String>(of("")) {
            @Override
            public Component getHeader(String componentId) {
                return new NavbarAjaxLink<String>(componentId, Model.of("Добавить")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setResponsePage(TraderEdit.class);
                    }
                }.setIconType(GlyphIconType.plus);
            }

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
        table.add(new TableBehavior().bordered().condensed());

        add(table);

        table.add(new WebSocketBehavior() {
            @Override
            protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message) {
                if (message instanceof ExchangeMessage) {
                    ExchangeMessage exchangeMessage = (ExchangeMessage) message;
                    Object payload = exchangeMessage.getPayload();

                    if (payload instanceof AccountInfo){
                        BigDecimal estimate = new BigDecimal("0");

                        for (ExchangePair exchangePair : balanceMap.keySet()){
                            if (exchangePair.getExchangeType().equals(exchangeMessage.getExchangeType())){
                                BigDecimal balance = ((AccountInfo) payload).getBalance(exchangePair.getCurrency());

                                if (traderService.getOrderBook(exchangePair) == null){
                                    continue;
                                }

                                BigDecimal price = traderService.getOrderBook(exchangePair).getAsks().get(0).getLimitPrice();

                                if (traderService.getOrderBook(exchangePair) != null && exchangePair.getCounterSymbol().equals("BTC")) {
                                    estimate = estimate.add(balance.multiply(price)).setScale(8, BigDecimal.ROUND_HALF_UP);
                                }

                                update(handler, balanceMap.get(exchangePair), balance);

                            }
                        }

                        if (exchangeMessage.getExchangeType().equals(BITTREX)) {
                            update(handler, bittrexCoins, estimate);
                            update(handler, bittrexBTC, ((AccountInfo) payload).getBalance("BTC"));


                            //update chart balance
                            if (lastChartValue.compareTo(((AccountInfo) payload).getBalance("BTC")) != 0) {
                                lastChartValue = ((AccountInfo) payload).getBalance("BTC");

                                Point point = new Point(chartIndex++, lastChartValue);
                                JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();
                                String jsonPoint = renderer.toJson(point);
                                String javaScript = "var chartVarName = " + chart.getJavaScriptVarName() + ";\n";
                                javaScript += "var seriesIndex = " + 0 + ";\n";
                                javaScript += "eval(chartVarName).series[seriesIndex].addPoint(" + jsonPoint + ", true, true);\n";

                                handler.appendJavaScript(javaScript);
                            }
                        }else if (exchangeMessage.getExchangeType().equals(CEXIO)){
                            update(handler, cexioCoins, estimate);
                            update(handler, cexioBTC, ((AccountInfo) payload).getBalance("BTC"));
                        }else if (exchangeMessage.getExchangeType().equals(CRYPTSY)){
                            update(handler, cryptsyCoins, estimate);
                            update(handler, cryptsyBTC, ((AccountInfo) payload).getBalance("BTC"));
                        }else if (exchangeMessage.getExchangeType().equals(BTCE)){
                            update(handler, btceCoins, estimate);
                            update(handler, btceBTC, ((AccountInfo) payload).getBalance("BTC"));
                        }
                    }else if (payload instanceof BalanceHistory){
                        int orderRate = traderService.getOrderRate();

                        //update chart order rate
                        if (!lastChart2Value.equals(orderRate)) {
                            lastChart2Value = orderRate;

                            Point point = new Point(chart2Index++, lastChart2Value);
                            JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();
                            String jsonPoint = renderer.toJson(point);
                            String javaScript = "var chartVarName = " + chart2.getJavaScriptVarName() + ";\n";
                            javaScript += "var seriesIndex = " + 0 + ";\n";
                            javaScript += "eval(chartVarName).series[seriesIndex].addPoint(" + jsonPoint + ", true, true);\n";

                            handler.appendJavaScript(javaScript);
                        }
                    }else if (payload instanceof OrderBook) {
                        OrderBook orderBook = (OrderBook) exchangeMessage.getPayload();

                        BigDecimal askPrice =  orderBook.getAsks().get(0).getLimitPrice();

                        CurrencyPair currencyPair = orderBook.getAsks().get(0).getCurrencyPair();
                        ExchangePair exchangePair = ExchangePair.of(exchangeMessage.getExchangeType(), currencyPair);

                        //ask
                        update(handler, askMap.get(exchangePair), askPrice);

                        //bid
                        update(handler, bidMap.get(exchangePair), orderBook.getBids().get(orderBook.getBids().size()-1)
                                .getLimitPrice());

                        //estimate
                        update(handler, estimateMap.get(exchangePair), askPrice
                                .multiply(traderService.getAccountInfo(exchangeMessage.getExchangeType())
                                        .getBalance(currencyPair.baseSymbol)).setScale(8, BigDecimal.ROUND_HALF_UP));

                    }else if (payload instanceof OpenOrders){
                        OpenOrders openOrders = (OpenOrders) exchangeMessage.getPayload();

                        Map<ExchangePair, BigDecimal> countBuyMap = new HashMap<>();
                        Map<ExchangePair, BigDecimal> countSellMap = new HashMap<>();

                        for (ExchangePair ep : buyMap.keySet()){
                            if (ep.getExchangeType().equals(exchangeMessage.getExchangeType())) {
                                countBuyMap.put(ep, new BigDecimal("0"));
                                countSellMap.put(ep, new BigDecimal("0"));
                            }
                        }

                        for (LimitOrder order : openOrders.getOpenOrders()){
                            ExchangePair ep = ExchangePair.of(exchangeMessage.getExchangeType(), order.getCurrencyPair());

                            if (buyMap.get(ep) != null) {
                                switch (order.getType()){
                                    case BID:
                                        countBuyMap.put(ep, countBuyMap.get(ep).add(order.getTradableAmount()));
                                        break;
                                    case ASK:
                                        countSellMap.put(ep, countSellMap.get(ep).add(order.getTradableAmount()));
                                        break;
                                }
                            }
                        }

                        countBuyMap.forEach(new BiConsumer<ExchangePair, BigDecimal>() {
                            @Override
                            public void accept(ExchangePair exchangePair, BigDecimal bigDecimal) {
                                update(handler, buyMap.get(exchangePair), bigDecimal);
                            }
                        });

                        countSellMap.forEach(new BiConsumer<ExchangePair, BigDecimal>() {
                            @Override
                            public void accept(ExchangePair exchangePair, BigDecimal bigDecimal) {
                                update(handler, sellMap.get(exchangePair), bigDecimal);
                            }
                        });
                    }else if (payload instanceof String){
                        if ("Cryptsy returned an error: Unable to Authorize Request - Check Your Post Data".equals(payload)){
                            return;
                        }

                        warn((String) payload);

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

            @Override
            public boolean isVisible() {
                return false;
            }
        }.setIconType(GlyphIconType.warningsign).setLabel(of("test")));

        //Chart
        {
            Options options = new Options();
            options.setChartOptions(new ChartOptions(SeriesType.SPLINE).setHeight(250));
            options.setGlobal(new Global().setUseUTC(false));

            options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
            options.setTitle(new Title(""));
            options.setLegend(new Legend(Boolean.FALSE));

            options.setxAxis(new Axis().setType(AxisType.LINEAR));

            options.setyAxis(new Axis().setTitle(new Title("")));

            options.setPlotOptions(new PlotOptionsChoice().setSpline(new PlotOptions()
                    .setMarker(new Marker(false))
                    .setLineWidth(1)));


            List<Point> data = new ArrayList<>();
            BigDecimal value = BigDecimal.ZERO;

            AccountInfo accountInfo = traderService.getAccountInfo(BITTREX);
            if (accountInfo != null) {
                value = accountInfo.getBalance("BTC");
            }

            for (int i = 0; i < 600; ++i) {
                data.add(0, new Point(0, value));
            }

            options.addSeries(new PointSeries().setData(data).setName("Bittrex"));

            add(chart = new Chart("chart", options));
        }

        //Chart
        {
            Options options2 = new Options();
            options2.setChartOptions(new ChartOptions(SeriesType.SPLINE).setHeight(250));
            options2.setGlobal(new Global().setUseUTC(false));

            options2.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
            options2.setTitle(new Title(""));
            options2.setLegend(new Legend(Boolean.FALSE));

            options2.setxAxis(new Axis().setType(AxisType.LINEAR));

            options2.setyAxis(new Axis().setTitle(new Title("")));

            options2.setPlotOptions(new PlotOptionsChoice().setSpline(new PlotOptions()
                    .setMarker(new Marker(false))
                    .setLineWidth(1)));

            List<Point> data = new ArrayList<>();
            Integer value = traderService.getOrderRate();

            for (int i = 0; i < 600; ++i) {
                data.add(0, new Point(0, value));
            }

            options2.addSeries(new PointSeries().setData(data).setName("Order Rate"));

            add(chart2 = new Chart("chart2", options2));
        }
    }

    private void update(WebSocketRequestHandler handler, Component component, BigDecimal newValue){
        if (component != null){
            int compare = newValue.toString().compareTo(component.getDefaultModelObjectAsString());

            if (compare != 0){
                String color = compare > 0 ? "'#EFFBEF'" : "'#FBEFEF'";

                handler.appendJavaScript(new JsStatement().$(component)
                        .chain("parent")
                        .chain("css", "\"background-color\"", color)
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
