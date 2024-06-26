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
import ru.inheaven.aida.coin.service.*;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.*;
import java.util.function.BiConsumer;

import static java.math.BigDecimal.*;
import static org.apache.wicket.model.Model.of;
import static ru.inheaven.aida.coin.entity.ExchangeType.BTCE;
import static ru.inheaven.aida.coin.entity.ExchangeType.OKCOIN_SPOT;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *          Date: 07.01.14 20:54
 */
public class TraderList extends AbstractPage{
    @EJB
    private TraderBean traderBean;

    @EJB
    private TraderService traderService;

    @EJB
    private StatService statService;

    @EJB
    private DataService dataService;

    @EJB
    private AccountService accountService;

    private Map<ExchangePair, Component> lastMap = new HashMap<>();
    private Map<ExchangePair, Component> estimateMap = new HashMap<>();
    private Map<ExchangePair, Component> buyMap = new HashMap<>();
    private Map<ExchangePair, Component> sellMap = new HashMap<>();
    private Map<ExchangePair, Component> positionMap = new HashMap<>();
    private Map<ExchangePair, Component> volatilityMap = new HashMap<>();
    private Map<ExchangePair, Component> profitMap = new HashMap<>();
    private Map<ExchangePair, Component> predictionMap = new HashMap<>();
    private Map<ExchangePair, Component> averageMap = new HashMap<>();

    private Component notificationLabel, notificationLabel2, notificationLabel3;
    private long notificationTime = System.currentTimeMillis();
    private long notificationErrorTime = System.currentTimeMillis();

    private Component bittrexBTC, bittrexCoins;
    private Component cexioBTC, cexioCoins;
    private Component cryptsyBTC, cryptsyCoins;
    private Component btceBTC, btceCoins;
    private Component bterBTC, bterCoins;
    private Component bitfinexBTC, bitfinexCoins;
    private Component okcoinBTC, okcoinCoins;

    private Component sumEstimate, tradesCount, rubEstimate;

    private BigDecimal lastChartValue = BigDecimal.ZERO;

    private long lastChartTime = System.currentTimeMillis();
    private long lastChart4Time = System.currentTimeMillis();

    private Chart chart, chart2, chart3, chart4;

    private int chartIndex = 1;
    private int chart2Index = 1;
    private int chart3Index1 = 1;
    private int chart3Index2 = 1;

    Date startDay = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24);
    Date startDate = new Date(System.currentTimeMillis() - 1000 * 60 * 60 * 24 * 7);
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

        add(rubEstimate = new Label("quote", Model.of(quote)).setOutputMarkupId(true));

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

                String name = trader.getExchangeType().getShortName() + " " + trader.getPair()
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
                return dataService.getTicker(trader.getExchangePair()).getBid().toPlainString();
            }
        });
        list.add(new TraderColumn(of("Average"), averageMap));
        list.add(new TraderColumn(of("Volatility"), volatilityMap){
            @Override
            protected String getInitValue(Trader trader) {
                return BigDecimal.valueOf(100).multiply(statService.getVolatilitySigma(trader.getExchangePair())).toPlainString() + "%";
            }
        });
        list.add(new TraderColumn(of("Prediction"), predictionMap));
        list.add(new TraderColumn(of("Position"), positionMap));
        list.add(new TraderColumn(of("Day"), profitMap){
            @Override
            protected String getInitValue(Trader trader) {
                return statService.getOrderStatProfit(trader.getExchangePair(), startDay).toPlainString();
            }
        });
        list.add(new PropertyColumn<>(of("Week"), "weekProfit"));

        final DataTable<Trader, String> table = new DataTable<>("traders", list, new ListDataProvider<Trader>(){
            @Override
            protected List<Trader> getData() {
                List<Trader> traders = traderBean.getTraders();

                for (Trader trader : traders){
                    trader.setWeekProfit(statService.getOrderStatProfit(trader.getExchangePair(), new Date(startWeekDate)));
                }

                Collections.sort(traders, new Comparator<Trader>() {
                    @Override
                    public int compare(Trader t1, Trader t2) {
                        if (t2.getExchangeType().equals(OKCOIN_SPOT) && !t1.getExchangeType().equals(OKCOIN_SPOT)){
                            return 1;
                        }

                        if (!t2.getExchangeType().equals(OKCOIN_SPOT) && t1.getExchangeType().equals(OKCOIN_SPOT)){
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
                            case OKCOIN_SPOT:
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
                                case OKCOIN_SPOT:
                                    update(handler, okcoinCoins, equity.getVolume());
                                    break;
                            }
                        } else {
                            update(handler, sumEstimate, equity.getVolume());
                            handler.add(rubEstimate.setDefaultModelObject(equity.getVolume().multiply(dataService.getTicker(
                                    ExchangePair.of(BTCE, "BTC/RUR")).getLast()).setScale(2, ROUND_UP).toString() + "\u20BD"));

                            //update chart balance
                            if (lastChartValue.compareTo(equity.getVolume()) != 0) {
                                ExchangePair exchangePair = ExchangePair.of(OKCOIN_SPOT, "BTC/USD");
                                Ticker ticker = dataService.getTicker(exchangePair);
                                BigDecimal predictionIndex = statService.getPredictionIndex(exchangePair);
                                if (predictionIndex.abs().doubleValue() > 0.05){
                                    predictionIndex = BigDecimal.valueOf(0.05);
                                }

                                BigDecimal prediction = ONE.add(predictionIndex).multiply(ticker.getLast()).setScale(2, ROUND_UP);

                                lastChartValue = equity.getVolume();

                                JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();

                                String id = chart.getJavaScriptVarName();

                                if (System.currentTimeMillis() - lastChartTime > 1000*60*5) {
                                    lastChartTime = System.currentTimeMillis();

                                    String javaScript = id + ".series[2].addPoint("
                                            + renderer.toJson(new Point(System.currentTimeMillis(), equity.getVolume())) + ", true, true);";
                                    javaScript += id + ".series[1].addPoint("
                                            + renderer.toJson(new Point(System.currentTimeMillis(), ticker.getLast())) + ", true, true);";
                                    javaScript += id + ".series[0].addPoint("
                                            + renderer.toJson(new Point(System.currentTimeMillis(), prediction)) + ", true, true);";

                                    handler.appendJavaScript(javaScript);
                                } else {
                                    String javaScript = "var s = " + id + ".series[2]; s.data[s.data.length - 1].update("
                                            + equity.getVolume().toPlainString() + ");";
                                    javaScript += "var s = " + id + ".series[1]; s.data[s.data.length - 1].update("
                                            + ticker.getLast().toPlainString() + ");";
                                    javaScript += "var s = " + id + ".series[0]; s.data[s.data.length - 1].update("
                                            + prediction.toPlainString() + ");";

                                    handler.appendJavaScript(javaScript);
                                }
                            }
                        }
                    } else if (payload instanceof BalanceHistory) {
                        //update profit column
                        BalanceHistory bh = (BalanceHistory) payload;
                        ExchangePair ep = ExchangePair.of(bh.getExchangeType(), bh.getPair());

                        update(handler, profitMap.get(ep), statService.getOrderStatProfit(ep, startDay), false, true);

                        ep = new ExchangePair(bh.getExchangeType(), bh.getPair(), TraderType.SHORT);
                        update(handler, profitMap.get(ep), statService.getOrderStatProfit(ep, startDay), false, true);

                        //update total
                        if (System.currentTimeMillis() - lastChart4Time > 60000*15) {
                            lastChart4Time = System.currentTimeMillis();

                            OrderVolume orderVolume = statService.getOrderVolumeRate(startDate);

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
                        BigDecimal average = statService.getAverage(ep);
                        update(handler, averageMap.get(ep), average);
                        update(handler, averageMap.get(ep2), average);
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
                                Ticker ticker = dataService.getTicker(ep);

                                if (ticker != null) {
                                    update(handler, buyMap.get(ep), statService.getBTCVolume(ep, amount, ticker.getLast()));
                                }
                            }
                        });

                        countSellMap.forEach(new BiConsumer<ExchangePair, BigDecimal>() {
                            @Override
                            public void accept(ExchangePair ep, BigDecimal amount) {
                                Ticker ticker = dataService.getTicker(ep);

                                if (ticker != null) {
                                    update(handler, sellMap.get(ep), statService.getBTCVolume(
                                            ep, amount, ticker.getLast()));

                                    //estimate
                                    BigDecimal balance = accountService.getAccountInfo(exchangeMessage.getExchangeType()).getBalance(ep.getCurrency());

                                    if (!ep.getExchangeType().equals(OKCOIN_SPOT)) {
                                        balance = balance.add(amount);
                                    }

                                    update(handler, estimateMap.get(ep), statService.getEstimateBalance(
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
                    } else if (payload instanceof Order) {
                        Order order = (Order) payload;


                        handler.add(notificationLabel3.setDefaultModelObject(order.toString()));

                        if (order.getStatus().equals(OrderStatus.CLOSED)) {
                            String style = "style= \"color: " + (order.getType().equals(OrderType.ASK)
                                    ? "#62c462" : "#ee5f5b") + "; display: none\"";
                            String row = "var row = '<tr " + style + "><td>" + order.toString() + "</td></tr>';";

                            handler.appendJavaScript(row + "$(row).insertAfter('#orders').fadeIn('slow');");
                        }

                        //update trades count
                        update(handler, tradesCount, traderBean.getOrderHistoryCount(startDay, OrderStatus.CLOSED).toString());
                    } else if (payload instanceof Futures) {
                        Futures futures = (Futures) payload;

                        List<Point> data0 = new ArrayList<>();
                        List<Point> data1 = new ArrayList<>();
                        List<Point> data2 = new ArrayList<>();
                        List<Point> data3 = new ArrayList<>();

                        //volume
                        ExchangePair ltcUsd = ExchangePair.of(OKCOIN_SPOT, "LTC/USD");
                        List<OrderStat> orderStats = traderBean.getOrderStatVolume(ltcUsd, startDate);
                        BigDecimal last = dataService.getTicker(ltcUsd).getLast().setScale(2, ROUND_UP);

                        BigDecimal predictionPrice = ONE.add(statService.getPredictionIndex(ltcUsd))
                                .multiply(last).setScale(4, ROUND_UP);

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
                            } else {
                                point.setColor(new HexColor("#434348"));
                            }

                            //noinspection unchecked
                            data0.add(point);
                        }

                        int size = futures.getAsks().size();

                        BigDecimal avg = statService.getAverage(ltcUsd);
                        BigDecimal ltcEquity = accountService.getAccountInfo(OKCOIN_SPOT).getBalance("LTC");

                        for (int i = 0; i < size; ++i) {
                            //noinspection unchecked
                            data1.add(new Point(futures.getAsks().get(i).getPrice().setScale(4, ROUND_UP),
                                    futures.getAsks().get(i).getAmount().add(ltcEquity).setScale(4, ROUND_UP)));

                            //noinspection unchecked
                            data2.add(new Point(futures.getBids().get(i).getPrice().setScale(4, ROUND_UP),
                                    futures.getBids().get(i).getAmount().add(ltcEquity).setScale(4, ROUND_UP)));

                            //noinspection unchecked
                            data3.add(new Point(futures.getEquity().get(i).getPrice().setScale(4, ROUND_UP),
                                    futures.getEquity().get(i).getAmount().add(ltcEquity).setScale(4, ROUND_UP))
                                    .setMarker(new Marker(false)));
                        }

                        for(Point point : data3){
                            if (((BigDecimal)point.getX()).subtract(avg).abs().doubleValue() < 0.01){
                                point.setMarker(new Marker(true).setSymbol(new Symbol(Symbol.PredefinedSymbol.TRIANGLEDOWN)));
                                break;
                            }
                        }
                        for(Point point : data3){
                            if (((BigDecimal)point.getX()).subtract(futures.getAvgPosition()).abs().doubleValue() < 0.01){
                                point.setMarker(new Marker(true).setSymbol(new Symbol(Symbol.PredefinedSymbol.TRIANGLE)));
                                break;
                            }
                        }

                        //render js
                        JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();
                        String id = chart4.getJavaScriptVarName();

                        String js = id  + ".series[0].setData(" + renderer.toJson(data0) + ", false);"
                                + id  + ".series[1].setData(" + renderer.toJson(data1) + ", false);"
                                + id  + ".series[2].setData(" + renderer.toJson(data2) + ", false);"
                                + id  + ".series[3].setData(" + renderer.toJson(data3) + ", false);"
                                + id + ".redraw();";

                        handler.appendJavaScript(js);
                    } else if (payload instanceof String) {
                        String logMessage = ((String) payload);

                        if (logMessage.contains("Unable to Authorize Request")) {
                            return;
                        }

                        if (System.currentTimeMillis() - notificationTime > 500) {
                            if (logMessage.contains("@")) {
                                handler.add(notificationLabel2.setDefaultModelObject(payload));
                            } else {
                                handler.add(notificationLabel.setDefaultModelObject(payload));
                                notificationErrorTime = System.currentTimeMillis();
                            }

                            notificationTime = System.currentTimeMillis();
                        }

                        if (System.currentTimeMillis() - notificationErrorTime > 1000*60*10){
                            handler.add(notificationLabel.setDefaultModelObject(""));
                            notificationErrorTime = System.currentTimeMillis();
                        }
                    }
                }
            }
        });

        //Orders
        List<Order> orderHistories = traderBean.getOrderHistories(OrderStatus.CLOSED, new Date(System.currentTimeMillis() - 1000*60*60));
        orderHistories.sort((o1, o2) -> o2.getClosed().compareTo(o1.getClosed()));

        add(orders = new BootstrapListView<Order>("orders", orderHistories) {
            @Override
            protected void populateItem(ListItem<Order> item) {
                Order order = item.getModelObject();

                item.add(new AttributeModifier("style", "color: " + (order.getType().equals(OrderType.ASK) ? "#62c462" : "#ee5f5b")));
                item.add(new Label("order", Model.of(order.toString())));
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
                    .setTurboThreshold(20000)));

            List<Point> data = new ArrayList<>();
            List<Equity> equities = traderBean.getEquities(startDate);

            long time = 0L;
            for (Equity equity : equities){
                if (equity.getDate().getTime() - time > 60000*15){
                    data.add(new Point(equity.getDate().getTime(), equity.getVolume()));
                    time = equity.getDate().getTime();
                }
            }

            List<Point> data2 = new ArrayList<>();
            List<Point> data3 = new ArrayList<>();
            List<TickerHistory> tickerHistories = statService.getTickerHistories(ExchangePair.of(OKCOIN_SPOT, "BTC/USD"), startDate);

            time = 0L;
            for (TickerHistory tickerHistory : tickerHistories){
                if (tickerHistory.getDate().getTime() - time > 60000*15){
                    data2.add(new Point(tickerHistory.getDate().getTime(), tickerHistory.getPrice()));

                    BigDecimal predictionIndex = tickerHistory.getPrediction();

                    if (predictionIndex.abs().doubleValue() > 0.05){
                        predictionIndex = BigDecimal.valueOf(0.05);
                    }

                    data3.add(new Point(tickerHistory.getDate().getTime(), ONE.add(predictionIndex)
                            .multiply(tickerHistory.getPrice()).setScale(8, ROUND_UP)));

                    time = tickerHistory.getDate().getTime();
                }
            }

            options.addSeries(new PointSeries().setData(data3).setName("Prediction").setColor(new HighchartsColor(1)).setyAxis(1));
            options.addSeries(new PointSeries().setData(data2).setName("Btc / Usd").setColor(new HexColor("#DDDF0D")).setyAxis(1));
            options.addSeries(new PointSeries().setData(data).setName("Equity").setColor(new HexColor("#7798BF")).setyAxis(0));

            add(chart = new Chart("chart", options));
        }

        //Chart 2
        List<OrderVolume> orderVolumes = statService.getOrderVolumeRates(null, startDate);

        List<OrderVolume> filteredOrderVolumes = new ArrayList<>();
        long time = 0L;

        for (OrderVolume orderVolume : orderVolumes){
            if (orderVolume.getDate().getTime() - time > 60000*15){
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
                            .setMarker(new Marker(false))
                            .setTurboThreshold(20000))
                    .setColumn(new PlotOptions()
                            .setBorderWidth(0)
                            .setPointPadding(0f)));

            options.addSeries(new PointSeries().setData(new ArrayList<>()).setName("Volume").setColor(new HighchartsColor(1))
                    .setyAxis(1).setType(SeriesType.COLUMN));
            options.addSeries(new PointSeries().setData(new ArrayList<>()).setName("Short").setColor(new HexColor("#ee5f5b")));
            options.addSeries(new PointSeries().setData(new ArrayList<>()).setName("Long").setColor(new HexColor("#62c462")));
            options.addSeries(new PointSeries().setData(new ArrayList<>()).setName("Hedge").setColor(new HexColor("#DDDF0D")));

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
