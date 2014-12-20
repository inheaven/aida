package ru.inheaven.aida.coin.web;

import com.googlecode.wickedcharts.highcharts.jackson.JsonRenderer;
import com.googlecode.wickedcharts.highcharts.options.*;
import com.googlecode.wickedcharts.highcharts.options.color.HexColor;
import com.googlecode.wickedcharts.highcharts.options.color.HighchartsColor;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.highcharts.theme.GrayTheme;
import com.googlecode.wickedcharts.wicket6.highcharts.Chart;
import com.googlecode.wickedcharts.wicket6.highcharts.JsonRendererFactory;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import de.agilecoders.wicket.core.markup.html.bootstrap.list.BootstrapListView;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.table.TableBehavior;
import org.apache.wicket.AttributeModifier;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.effects.HighlightEffectJavaScriptResourceReference;
import ru.inheaven.aida.coin.entity.*;
import ru.inheaven.aida.coin.service.TraderBean;
import ru.inheaven.aida.coin.service.TraderService;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiConsumer;

import static java.math.BigDecimal.*;
import static org.apache.wicket.model.Model.of;
import static ru.inheaven.aida.coin.entity.ExchangeType.OKCOIN;

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
    private Map<ExchangePair, Component> estimateMap = new HashMap<>();
    private Map<ExchangePair, Component> buyMap = new HashMap<>();
    private Map<ExchangePair, Component> sellMap = new HashMap<>();
    private Map<ExchangePair, Component> positionMap = new HashMap<>();
    private Map<ExchangePair, Component> volatilityMap = new HashMap<>();
    private Map<ExchangePair, Component> profitMap = new HashMap<>();
    private Map<ExchangePair, Component> predictionMap = new HashMap<>();
    private Map<ExchangePair, Component> predictionTestMap = new HashMap<>();

    private Component notificationLabel, notificationLabel2, notificationLabel3;
    private long notificationTime = System.currentTimeMillis();

    private Component bittrexBTC, bittrexCoins;
    private Component cexioBTC, cexioCoins;
    private Component cryptsyBTC, cryptsyCoins;
    private Component btceBTC, btceCoins;
    private Component bterBTC, bterCoins;
    private Component bitfinexBTC, bitfinexCoins;
    private Component okcoinBTC, okcoinCoins;

    private Component sumEstimate, tradesCount;

    private BigDecimal lastChartValue = BigDecimal.ZERO;

    private long lastChartTime = System.currentTimeMillis();
    private long lastChart4Time = System.currentTimeMillis();

    private Chart chart, chart2, chart3, chart4;

    private int chartIndex = 1;
    private int chart2Index = 1;
    private int chart3Index1 = 1;
    private int chart3Index2 = 1;

    Date startDay = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    Date startDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    long startWeekDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7;
    long pageInitTime = System.currentTimeMillis();

    private TraderEditModal traderEditModal;

    private Component orders;

    BigDecimalConverter bigDecimalConverter2 = new BigDecimalConverter() {
        @Override
        protected NumberFormat newNumberFormat(Locale locale) {
            NumberFormat numberFormat = super.newNumberFormat(locale);
            numberFormat.setMinimumFractionDigits(2);
            numberFormat.setMaximumFractionDigits(2);

            return numberFormat;
        }
    };

    public TraderList() {
        setVersioned(false);

        add(notificationLabel = new Label("notification", Model.of("")).setOutputMarkupId(true));
        add(notificationLabel2 = new Label("notification2", Model.of("")).setOutputMarkupId(true));
        add(notificationLabel3 = new Label("notification3", Model.of("")).setOutputMarkupId(true));

        add(new Label("header", of(getTitle())));

        add(new Label("tradersCount", of(traderBean.getTradersCount())));
        add(tradesCount = new Label("tradesCount", Model.of("0")).setOutputMarkupId(true));
        add(sumEstimate = new Label("sumEstimate", Model.of("0")).setOutputMarkupId(true));

        add(bittrexBTC = new Label("bittrexBTC", Model.of("0")).setOutputMarkupId(true));
        add(bittrexCoins = new Label("bittrexCoins", Model.of("0")).setOutputMarkupId(true));

        add(cexioBTC = new Label("cexioBTC", Model.of("0")).setOutputMarkupId(true));
        add(cexioCoins = new Label("cexioCoins", Model.of("0")).setOutputMarkupId(true));

        add(cryptsyBTC = new Label("cryptsyBTC", Model.of("0")).setOutputMarkupId(true));
        add(cryptsyCoins = new Label("cryptsyCoins", Model.of("0")).setOutputMarkupId(true));

        add(btceBTC = new Label("btceBTC", Model.of("0")).setOutputMarkupId(true));
        add(btceCoins = new Label("btceCoins", Model.of("0")).setOutputMarkupId(true));

        add(bterBTC = new Label("bterBTC", Model.of("0")).setOutputMarkupId(true));
        add(bterCoins = new Label("bterCoins", Model.of("0")).setOutputMarkupId(true));

        add(bitfinexBTC = new Label("bitfinexBTC", Model.of("0")).setOutputMarkupId(true));
        add(bitfinexCoins = new Label("bitfinexCoins", Model.of("0")).setOutputMarkupId(true));

        add(okcoinBTC = new Label("okcoinBTC", Model.of("0")).setOutputMarkupId(true));
        add(okcoinCoins = new Label("okcoinCoins", Model.of("0")).setOutputMarkupId(true));

        String quote = ".·´`·.¸¸.·´´`·.¸.·´´``·.¸.·´``·.´``·.¸¸.·´´``· <º>< ><º>";

        add(new Label("quote", Model.of(quote)));

        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new AbstractColumn<Trader, String>(of("")) {
            @Override
            public Component getHeader(String componentId) {
                return new NavbarAjaxLink<String>(componentId, Model.of("Trader")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        traderEditModal.show(target, new Trader());
                    }
                };
            }

            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                final Trader trader = rowModel.getObject();

                String name = trader.getExchange().getShortName() + " " + trader.getPair()
                        + (trader.getType().equals(TraderType.SHORT) ? " ☯" : "");

                cellItem.add(new NavbarAjaxLink<String>(componentId, Model.of(name)) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        traderEditModal.show(target, trader);
                    }
                });
            }
        });
        list.add(new TraderColumn(of("Estimate"), estimateMap));
        list.add(new AbstractColumn<Trader, String>(of("Lot")) {
            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                Trader trader = rowModel.getObject();

                BigDecimal lot = trader.getLot() != null
                        ? trader.getLot()
                        : traderService.getMinOrderVolume(trader.getExchangePair());

                cellItem.add(new Label(componentId, of(lot)));
            }
        });
        list.add(new TraderColumn(of("Buy"), buyMap));
        list.add(new TraderColumn(of("Sell"), sellMap));
        list.add(new TraderColumn(of("Last"), lastMap){
            @Override
            protected String getInitValue(Trader trader) {
                return traderService.getTickerNotNull(trader.getExchangePair()).getBid().toPlainString();
            }
        });
        list.add(new TraderColumn(of("Volatility"), volatilityMap){
            @Override
            protected String getInitValue(Trader trader) {
                return traderService.getVolatilityIndex(trader.getExchangePair()).toPlainString() + "%";
            }
        });
        list.add(new TraderColumn(of("Prediction"), predictionMap));
        list.add(new TraderColumn(of("Test"), predictionTestMap){
            @Override
            protected String getInitValue(Trader trader) {
                return traderService.getPredictionTestIndex(trader.getExchangePair()).toPlainString() + "%";
            }
        });
        list.add(new TraderColumn(of("Position"), positionMap));
        list.add(new TraderColumn(of("Day"), profitMap){
            @Override
            protected String getInitValue(Trader trader) {
                return traderService.getOrderStatProfit(trader.getExchangePair(), startDay).toPlainString();
            }
        });
        list.add(new PropertyColumn<>(of("Week"), "weekProfit"));

        final DataTable<Trader, String> table = new DataTable<>("traders", list, new ListDataProvider<Trader>(){
            @Override
            protected List<Trader> getData() {
                List<Trader> traders = traderBean.getTraders();

                for (Trader trader : traders){
                    trader.setWeekProfit(traderService.getOrderStatProfit(trader.getExchangePair(), new Date(startWeekDate)));
                }

                Collections.sort(traders, new Comparator<Trader>() {
                    @Override
                    public int compare(Trader t1, Trader t2) {
                        if (t2.getExchange().equals(OKCOIN) && !t1.getExchange().equals(OKCOIN)){
                            return 1;
                        }

                        if (!t2.getExchange().equals(OKCOIN) && t1.getExchange().equals(OKCOIN)){
                            return -1;
                        }

                        return t2.getWeekProfit().compareTo(t1.getWeekProfit());
                    }
                });

                return traders;
            }
        }, 500);
        table.setOutputMarkupId(true);
        table.addTopToolbar(new HeadersToolbar<>(table, null));
        table.add(new TableBehavior().bordered().condensed());

        add(table);

        add(traderEditModal = new TraderEditModal("traderEditModal"){
            @Override
            protected void onSave(AjaxRequestTarget target) {
                target.add(table);
            }
        });

        table.add(new WebSocketBehavior() {
            @Override
            protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message) {
                if (message instanceof ExchangeMessage) {
                    ExchangeMessage exchangeMessage = (ExchangeMessage) message;
                    Object payload = exchangeMessage.getPayload();

                    if (payload instanceof AccountInfo) {
                        AccountInfo accountInfo = ((AccountInfo) payload);

                        switch (exchangeMessage.getExchangeType()) {
                            case CEXIO:
                                update(handler, cexioBTC, accountInfo.getBalance("BTC"));
                                break;
                            case CRYPTSY:
                                update(handler, cryptsyBTC, accountInfo.getBalance("BTC"));
                                break;
                            case BITTREX:
                                update(handler, bittrexBTC, accountInfo.getBalance("BTC"));
                                break;
                            case BTCE:
                                update(handler, btceBTC, accountInfo.getBalance("BTC"));
                                break;
                            case BTER:
                                update(handler, bterBTC, accountInfo.getBalance("BTC"));
                                break;
                            case BITFINEX:
                                update(handler, bitfinexBTC, accountInfo.getBalance("BTC"));
                                break;
                            case OKCOIN:
                                update(handler, okcoinBTC, accountInfo.getBalance("BTC"));
                                break;
                        }
                    } else if (payload instanceof Equity) {
                        Equity equity = (Equity) payload;

                        if (exchangeMessage.getExchangeType() != null) {
                            switch (exchangeMessage.getExchangeType()) {
                                case CEXIO:
                                    update(handler, cexioCoins, equity.getVolume());
                                    break;
                                case CRYPTSY:
                                    update(handler, cryptsyCoins, equity.getVolume());
                                    break;
                                case BITTREX:
                                    update(handler, bittrexCoins, equity.getVolume());
                                    break;
                                case BTCE:
                                    update(handler, btceCoins, equity.getVolume());
                                    break;
                                case BTER:
                                    update(handler, bterCoins, equity.getVolume());
                                    break;
                                case BITFINEX:
                                    update(handler, bitfinexCoins, equity.getVolume());
                                    break;
                                case OKCOIN:
                                    update(handler, okcoinCoins, equity.getVolume());
                                    break;
                            }
                        } else {
                            update(handler, sumEstimate, equity.getVolume());

                            //update chart balance
                            if (lastChartValue.compareTo(equity.getVolume()) != 0) {
                                ExchangePair exchangePair = ExchangePair.of(OKCOIN, "BTC/USD");
                                Ticker ticker = traderService.getTicker(exchangePair);
                                BigDecimal prediction = ONE.add(traderService.getPredictionIndex(exchangePair)).multiply(ticker.getLast());

                                lastChartValue = equity.getVolume();

                                JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();

                                if (System.currentTimeMillis() - lastChartTime > 60000) {
                                    lastChartTime = System.currentTimeMillis();

                                    String javaScript = "eval(" + chart.getJavaScriptVarName() + ").series[" + 2 + "].addPoint("
                                            + renderer.toJson(new Point(System.currentTimeMillis(), equity.getVolume())) + ", true, true);";
                                    handler.appendJavaScript(javaScript);

                                    javaScript = "eval(" + chart.getJavaScriptVarName() + ").series[" + 1 + "].addPoint("
                                            + renderer.toJson(new Point(System.currentTimeMillis(), ticker.getLast())) + ", true, true);";
                                    handler.appendJavaScript(javaScript);

                                    javaScript = "eval(" + chart.getJavaScriptVarName() + ").series[" + 0 + "].addPoint("
                                            + renderer.toJson(new Point(System.currentTimeMillis() + 1000*60*15, prediction)) + ", true, true);";
                                    handler.appendJavaScript(javaScript);
                                } else {
                                    String javaScript = "var s = eval(" + chart.getJavaScriptVarName() + ").series[2];" +
                                            "s.data[s.data.length - 1].update(" + equity.getVolume().toPlainString() + ")";
                                    handler.appendJavaScript(javaScript);

                                    javaScript = "var s = eval(" + chart.getJavaScriptVarName() + ").series[1];" +
                                            "s.data[s.data.length - 1].update(" + ticker.getLast().toPlainString() + ")";
                                    handler.appendJavaScript(javaScript);

                                    javaScript = "var s = eval(" + chart.getJavaScriptVarName() + ").series[0];" +
                                            "s.data[s.data.length - 1].update(" + prediction.toPlainString() + ")";
                                    handler.appendJavaScript(javaScript);
                                }
                            }
                        }
                    } else if (payload instanceof BalanceHistory) {
                        //update profit column
                        BalanceHistory bh = (BalanceHistory) payload;
                        ExchangePair ep = ExchangePair.of(bh.getExchangeType(), bh.getPair());

                        update(handler, profitMap.get(ep), traderService.getOrderStatProfit(ep, startDay), false, true);

                        ep = new ExchangePair(bh.getExchangeType(), bh.getPair(), TraderType.SHORT);
                        update(handler, profitMap.get(ep), traderService.getOrderStatProfit(ep, startDay), false, true);

                        //update trades count
                        update(handler, tradesCount, traderBean.getOrderHistoryCount(startDate, OrderStatus.CLOSED).toString());

                        //update total
                        if (System.currentTimeMillis() - lastChart4Time > 60000) {
                            lastChart4Time = System.currentTimeMillis();

                            OrderVolume orderVolume = traderService.getOrderVolumeRate(startDate);

                            JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();

                            //update chart order rate
                            if (orderVolume.getVolume().compareTo(ZERO) != 0) {
                                {
                                    String javaScript = "eval(" + chart3.getJavaScriptVarName() + ").series[" + 0 + "].addPoint("
                                            + renderer.toJson(new Point(orderVolume.getDate().getTime(), orderVolume.getBidVolume()))
                                            + ", true, true);";

                                    handler.appendJavaScript(javaScript);
                                }

                                {
                                    String javaScript = "eval(" + chart3.getJavaScriptVarName() + ").series[" + 1 + "].addPoint("
                                            + renderer.toJson(new Point(orderVolume.getDate().getTime(), orderVolume.getAskVolume()))
                                            + ", true, true);";

                                    handler.appendJavaScript(javaScript);
                                }
                            }
                        }
                    } else if (payload instanceof TickerHistory) {
                        TickerHistory tickerHistory = (TickerHistory) exchangeMessage.getPayload();

                        ExchangePair ep = ExchangePair.of(exchangeMessage.getExchangeType(), tickerHistory.getPair());
                        ExchangePair ep2 = new ExchangePair(exchangeMessage.getExchangeType(), tickerHistory.getPair(), TraderType.SHORT);

                        //last
                        update(handler, lastMap.get(ep), tickerHistory.getPrice());
                        update(handler, lastMap.get(ep2), tickerHistory.getPrice());

                        //volatility
                        BigDecimal volatility = tickerHistory.getVolatility();
                        update(handler, volatilityMap.get(ep), volatility, true, false);
                        update(handler, volatilityMap.get(ep2), volatility, true, false);

                        //prediction
                        BigDecimal prediction = tickerHistory.getPrediction().multiply(BigDecimal.valueOf(100));
                        update(handler, predictionMap.get(ep), prediction, true, true);
                        update(handler, predictionMap.get(ep2), prediction, true, true);

                        //prediction test
                        BigDecimal predictionTest = traderService.getPredictionTestIndex(ep);
                        update(handler, predictionTestMap.get(ep), predictionTest, true, false);
                        update(handler, predictionTestMap.get(ep2), predictionTest, true, false);
                    } else if (payload instanceof OpenOrders) {
                        OpenOrders openOrders = (OpenOrders) exchangeMessage.getPayload();

                        Map<ExchangePair, BigDecimal> countBuyMap = new HashMap<>();
                        Map<ExchangePair, BigDecimal> countSellMap = new HashMap<>();

                        for (ExchangePair ep : buyMap.keySet()) {
                            if (ep.getExchangeType().equals(exchangeMessage.getExchangeType())) {
                                countBuyMap.put(ep, ZERO);
                                countSellMap.put(ep, ZERO);
                            }
                        }

                        for (LimitOrder order : openOrders.getOpenOrders()) {
                            ExchangePair ep = order.getId().contains("&2") || order.getId().contains("&4")
                                    ? ExchangePair.of(exchangeMessage.getExchangeType(), order.getCurrencyPair(), TraderType.SHORT)
                                    : ExchangePair.of(exchangeMessage.getExchangeType(), order.getCurrencyPair());

                            if (buyMap.get(ep) != null) {
                                switch (order.getType()) {
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
                            public void accept(ExchangePair ep, BigDecimal amount) {
                                Ticker ticker = traderService.getTickerNotNull(ep);

                                if (ticker != null) {
                                    update(handler, buyMap.get(ep), traderService.getBTCVolume(ep, amount, ticker.getLast()));
                                }
                            }
                        });

                        countSellMap.forEach(new BiConsumer<ExchangePair, BigDecimal>() {
                            @Override
                            public void accept(ExchangePair ep, BigDecimal amount) {
                                Ticker ticker = traderService.getTickerNotNull(ep);

                                if (ticker != null) {
                                    update(handler, sellMap.get(ep), traderService.getBTCVolume(
                                            ep, amount, ticker.getLast()));

                                    //estimate
                                    BigDecimal balance = traderService.getAccountInfo(exchangeMessage.getExchangeType()).getBalance(ep.getCurrency());

                                    if (!ep.getExchangeType().equals(OKCOIN)) {
                                        balance = balance.add(amount);
                                    }

                                    update(handler, estimateMap.get(ep), traderService.getEstimateBalance(
                                            ep.getExchangeType(), ep.getCurrency(), balance));

                                    //position
                                    BigDecimal position;
                                    if (countBuyMap.get(ep).compareTo(countSellMap.get(ep)) == 0) {
                                        position = ZERO;
                                    } else {
                                        position = BigDecimal.valueOf(100 * (countBuyMap.get(ep).floatValue()
                                                - countSellMap.get(ep).floatValue()) / (countBuyMap.get(ep).floatValue()
                                                + countSellMap.get(ep).floatValue()));
                                    }

                                    update(handler, positionMap.get(ep), position, true, true);
                                }
                            }
                        });
                    } else if (payload instanceof OrderHistory) {
                        OrderHistory orderHistory = (OrderHistory) payload;

                        if (orderHistory.getExchangeType().equals(OKCOIN)) {
                            orderHistory.setFilledAmountScale(0);

                            if (orderHistory.getPair().contains("LTC/")) {
                                orderHistory.setPriceScale(3);
                            } else if (orderHistory.getPair().contains("BTC/")) {
                                orderHistory.setPriceScale(2);
                            }
                        }

                        notificationLabel3.setDefaultModelObject(orderHistory.toString());
                        handler.add(notificationLabel3);

                        if (orderHistory.getStatus().equals(OrderStatus.CLOSED)) {
                            String style = "style= \"color: " + (orderHistory.getType().equals(Order.OrderType.ASK) ? "#62c462" : "#ee5f5b") + "\"";
                            handler.appendJavaScript("$('#orders').prepend('<tr " + style + "><td>" + orderHistory.toString() + "</td></tr>')");
                        }
                    } else if (payload instanceof Futures) {
                        Futures futures = (Futures) payload;

                        chart4.getOptions().getSeries().get(0).getData().clear();
                        chart4.getOptions().getSeries().get(1).getData().clear();
                        chart4.getOptions().getSeries().get(2).getData().clear();
                        chart4.getOptions().getSeries().get(3).getData().clear();

                        //volume
                        ExchangePair btcUsd = ExchangePair.of(OKCOIN, "BTC/USD");
                        List<OrderStat> orderStats = traderBean.getOrderStatVolume(btcUsd, startDate);
                        BigDecimal last = traderService.getTicker(btcUsd).getLast().setScale(1, ROUND_UP);

                        BigDecimal predictionPrice = ONE.add(traderService.getPredictionIndex(btcUsd))
                                .multiply(last).setScale(1, ROUND_UP);

                        for (OrderStat s : orderStats) {
                            if (s.getAvgPrice().compareTo(futures.getEquity().get(0).getPrice()) < 0
                                    || s.getAvgPrice().compareTo(futures.getEquity().get(futures.getEquity().size() - 1).getPrice()) > 0) {
                                continue;
                            }

                            Point point = new Point(s.getAvgPrice(), s.getSumAmount());

                            if (predictionPrice.compareTo(s.getAvgPrice()) > 0 && last.compareTo(s.getAvgPrice()) < 0) {
                                point.setColor(new HexColor("#62c462"));
                            } else if (predictionPrice.compareTo(s.getAvgPrice()) < 0 && last.compareTo(s.getAvgPrice()) > 0) {
                                point.setColor(new HexColor("#ee5f5b"));
                            } else if (last.compareTo(s.getAvgPrice()) == 0) {
                                point.setColor(new HexColor("#C8C8C8"));
                            }

                            //noinspection unchecked
                            chart4.getOptions().getSeries().get(0).getData().add(point);
                        }

                        for (int i = 0; i < futures.getAsks().size(); ++i) {
                            //noinspection unchecked
                            chart4.getOptions().getSeries().get(1).getData().add(new Point(futures.getAsks().get(i).getPrice().setScale(1, ROUND_UP),
                                    futures.getAsks().get(i).getAmount().setScale(4, ROUND_UP)));

                            //noinspection unchecked
                            chart4.getOptions().getSeries().get(2).getData().add(new Point(futures.getBids().get(i).getPrice().setScale(1, ROUND_UP),
                                    futures.getBids().get(i).getAmount().setScale(4, ROUND_UP)));

                            //noinspection unchecked
                            chart4.getOptions().getSeries().get(3).getData().add(new Point(futures.getEquity().get(i).getPrice().setScale(1, ROUND_UP),
                                    futures.getMargin().subtract(futures.getRealProfit()).subtract(futures.getEquity().get(i).getAmount()).setScale(4, ROUND_UP)));
                        }

                        handler.add(chart4);
                    } else if (payload instanceof String) {
                        if (((String) payload).contains("Unable to Authorize Request")) {
                            return;
                        }

                        if (System.currentTimeMillis() - notificationTime > 1000) {
                            if (!((String) payload).contains("@")) {
                                handler.add(notificationLabel.setDefaultModelObject(payload));
                            } else {
                                handler.add(notificationLabel2.setDefaultModelObject(payload));
                            }

                            notificationTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        });

        //Orders
        List<OrderHistory> orderHistories = traderBean.getOrderHistories(OrderStatus.CLOSED, new Date(System.currentTimeMillis() - 1000*60*60));
        orderHistories.sort((o1, o2) -> o2.getClosed().compareTo(o1.getClosed()));

        add(orders = new BootstrapListView<OrderHistory>("orders", orderHistories) {
            @Override
            protected void populateItem(ListItem<OrderHistory> item) {
                OrderHistory orderHistory = item.getModelObject();

                if (orderHistory.getExchangeType().equals(OKCOIN)){
                    orderHistory.setFilledAmountScale(0);

                    if (orderHistory.getPair().contains("LTC/")){
                        orderHistory.setPriceScale(3);
                    }else if (orderHistory.getPair().contains("BTC/")){
                        orderHistory.setPriceScale(2);
                    }
                }

                item.add(new AttributeModifier("style", "color: " + (orderHistory.getType().equals(Order.OrderType.ASK) ? "#62c462" : "#ee5f5b")));
                item.add(new Label("order", Model.of(orderHistory.toString())));
            }
        });

        //Chart
        {
            Options options = new GrayTheme();
            options.setChartOptions(new ChartOptions(SeriesType.LINE).setHeight(366).setZoomType(ZoomType.X)
                    .setBackgroundColor(HexColor.fromString("#272b30")));
            options.setGlobal(new Global().setUseUTC(false));
            options.setCredits(new CreditOptions().setEnabled(false));

            options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
            options.setTitle(new Title(""));

            options.setxAxis(new Axis().setType(AxisType.DATETIME));
            options.setyAxis(Arrays.asList(new Axis().setTitle(new Title("")), new Axis().setOpposite(true).setTitle(new Title(""))));

            options.setPlotOptions(new PlotOptionsChoice().setLine(new PlotOptions()
                    .setMarker(new Marker(false))
                    .setLineWidth(1)
                    .setTurboThreshold(20000)));

            List<Point> data = new ArrayList<>();
            List<Equity> equities = traderBean.getEquities(startDate);

            long time = 0L;
            for (Equity equity : equities){
                if (equity.getDate().getTime() - time > 1000*60){
                    data.add(new Point(equity.getDate().getTime(), equity.getVolume()));
                    time = equity.getDate().getTime();
                }
            }

            if (!data.isEmpty() && data.size() < 1000) {
                long x = data.get(0).getX().longValue();
                BigDecimal v = (BigDecimal) data.get(0).getY();

                for (int i = 0; i < 1000 - data.size(); ++i) {
                    data.add(0, new Point(x, v));
                }
            }

            List<Point> data2 = new ArrayList<>();
            List<Point> data3 = new ArrayList<>();
            List<TickerHistory> tickerHistories = traderBean.getTickerHistories(ExchangePair.of(OKCOIN, "BTC/USD"), startDate);

            time = 0L;
            for (TickerHistory tickerHistory : tickerHistories){
                if (tickerHistory.getDate().getTime() - time > 1000*60){
                    data2.add(new Point(tickerHistory.getDate().getTime(), tickerHistory.getPrice()));

                    if (tickerHistory.getPrediction().abs().doubleValue() < 0.01) {
                        data3.add(new Point(tickerHistory.getDate().getTime() + 1000*60*15, ONE.add(tickerHistory.getPrediction()).multiply(tickerHistory.getPrice())));
                    }

                    time = tickerHistory.getDate().getTime();
                }
            }

            options.addSeries(new PointSeries().setData(data3).setName("Prediction").setColor(new HighchartsColor(1)).setyAxis(1));
            options.addSeries(new PointSeries().setData(data2).setName("BTC/USD").setColor(new HexColor("#DDDF0D")).setyAxis(1));
            options.addSeries(new PointSeries().setData(data).setName("Equity").setColor(new HexColor("#7798BF")).setyAxis(0));

            add(chart = new Chart("chart", options));
        }

        //Chart 2
        List<OrderVolume> orderVolumes = traderService.getOrderVolumeRates(null, startDate);

        List<OrderVolume> filteredOrderVolumes = new ArrayList<>();
        long time = 0L;

        for (OrderVolume orderVolume : orderVolumes){
            if (orderVolume.getDate().getTime() - time > 60000){
                filteredOrderVolumes.add(orderVolume);
                time = orderVolume.getDate().getTime();
            }
        }

        {
            Options options = new GrayTheme();
            options.setChartOptions(new ChartOptions(SeriesType.SPLINE).setHeight(366).setZoomType(ZoomType.X)
                    .setBackgroundColor(HexColor.fromString("#272b30")));
            options.setGlobal(new Global().setUseUTC(false));
            options.setCredits(new CreditOptions().setEnabled(false));

            options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
            options.setTitle(new Title(""));

            options.setxAxis(new Axis().setType(AxisType.DATETIME));

            options.setyAxis(new Axis().setTitle(new Title("")));

            options.setPlotOptions(new PlotOptionsChoice().setSpline(new PlotOptions()
                    .setMarker(new Marker(false))
                    .setTurboThreshold(20000)));

            List<Point> dataAsk = new ArrayList<>();
            List<Point> dataBid = new ArrayList<>();

            for (OrderVolume orderVolume : filteredOrderVolumes){
                dataAsk.add(new Point(orderVolume.getDate().getTime(), orderVolume.getAskVolume()));
                dataBid.add(new Point(orderVolume.getDate().getTime(), orderVolume.getBidVolume()));
            }

            options.addSeries(new PointSeries().setData(dataBid).setName("Bid").setColor(new HexColor("#ee5f5b")));
            options.addSeries(new PointSeries().setData(dataAsk).setName("Ask").setColor(new HexColor("#62c462")));

            add(chart3 = new Chart("chart3", options));
        }

        //Chart 4
        {
            Options options = new GrayTheme();
            options.setChartOptions(new ChartOptions(SeriesType.SPLINE).setHeight(366).setZoomType(ZoomType.X)
                    .setBackgroundColor(HexColor.fromString("#272b30")));
            options.setGlobal(new Global().setUseUTC(false));
            options.setCredits(new CreditOptions().setEnabled(false));

            options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
            options.setTitle(new Title(""));

            options.setxAxis(new Axis().setType(AxisType.LINEAR));

            options.setyAxis(Arrays.asList(new Axis().setTitle(new Title("")), new Axis().setOpposite(true).setTitle(new Title(""))));

            options.setPlotOptions(new PlotOptionsChoice()
                    .setSpline(new PlotOptions()
                            .setAnimation(false)
                            .setMarker(new Marker(false))
                            .setTurboThreshold(20000))
                    .setColumn(new PlotOptions()
                            .setBorderWidth(0)
                            .setPointPadding(0f)
                            .setAnimation(false)));

            options.addSeries(new PointSeries().setData(new ArrayList<>()).setName("Volume").setColor(new HighchartsColor(1))
                    .setyAxis(1).setType(SeriesType.COLUMN));
            options.addSeries(new PointSeries().setData(new ArrayList<>()).setName("Short").setColor(new HexColor("#ee5f5b")));
            options.addSeries(new PointSeries().setData(new ArrayList<>()).setName("Long").setColor(new HexColor("#62c462")));
            options.addSeries(new PointSeries().setData(new ArrayList<>()).setName("Risk").setColor(new HexColor("#DDDF0D")));

            add(chart4 = new Chart("chart4", options));
        }
    }

    private void update(WebSocketRequestHandler handler, Component component, BigDecimal newValue){
        update(handler, component, newValue, false, false);
    }

    private void update(WebSocketRequestHandler handler, Component component, BigDecimal newValue, boolean percent, boolean negative){
        if (component != null && newValue != null){
            IConverter<BigDecimal> converter = percent ? bigDecimalConverter2 : getConverter(BigDecimal.class);

            String s =  converter.convertToString(newValue, getLocale());

            int compare = s.compareTo(component.getDefaultModelObjectAsString().replace("%", ""));

            if (negative && compare != 0) {
                compare = s.contains("-") ? -1 : 1;
            }

            if (compare != 0){
                String color = compare > 0 ? "'#62c462'" : "'#ee5f5b'";

                if (System.currentTimeMillis() - pageInitTime > 5000) {
                    handler.appendJavaScript(new JsStatement().$(component)
                            .chain("parent")
                            .chain("animate", "{color: '#FAFAFA'}")
                            .render());

                    handler.appendJavaScript(new JsStatement().$(component)
                            .chain("parent")
                            .chain("animate", "{color:" + color + "}")
                            .render());
                }

                component.setDefaultModelObject(s + (percent ? "%" : ""));
                handler.add(component);
            }
        }
    }

    private void update(WebSocketRequestHandler handler, Component component, String newValue){
        int compare = newValue.compareTo(component.getDefaultModelObjectAsString());

        if (compare != 0){
            String color = compare > 0 ? "'#62c462'" : "'#ee5f5b'";

            if (System.currentTimeMillis() - pageInitTime > 5000) {
                handler.appendJavaScript(new JsStatement().$(component)
                        .chain("parent")
                        .chain("animate", "{color: '#FAFAFA'}")
                        .render());

                handler.appendJavaScript(new JsStatement().$(component)
                        .chain("parent")
                        .chain("animate", "{color:" + color + "}")
                        .render());
            }

            component.setDefaultModelObject(newValue);

            handler.add(component);
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new PackageResourceReference(TraderList.class, "TraderList.css")));
        response.render(JavaScriptHeaderItem.forReference(HighlightEffectJavaScriptResourceReference.get()));
    }
}
