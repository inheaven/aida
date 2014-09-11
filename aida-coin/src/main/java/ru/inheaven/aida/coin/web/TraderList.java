package ru.inheaven.aida.coin.web;

import com.googlecode.wickedcharts.highcharts.jackson.JsonRenderer;
import com.googlecode.wickedcharts.highcharts.options.*;
import com.googlecode.wickedcharts.highcharts.options.color.HighchartsColor;
import com.googlecode.wickedcharts.highcharts.options.color.LinearGradient;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.wicket6.highcharts.Chart;
import com.googlecode.wickedcharts.wicket6.highcharts.JsonRendererFactory;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.dto.trade.Wallet;
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
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.odlabs.wiquery.core.javascript.JsStatement;
import ru.inheaven.aida.coin.entity.*;
import ru.inheaven.aida.coin.service.TraderBean;
import ru.inheaven.aida.coin.service.TraderService;
import ru.inheaven.aida.coin.util.TraderUtil;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.BigDecimal.ZERO;
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
    private Component bterBTC, bterCoins;

    private Map<ExchangeType, BigDecimal> lastChartValueMap = new HashMap<>();

    private BigDecimal lastChart4Value = BigDecimal.ZERO;
    private long lastChart4Time = System.currentTimeMillis();

    private Chart chart, chart2, chart3, chart4;

    private int chartIndex = 1;
    private int chart2Index = 1;
    private int chart3Index1 = 1;
    private int chart3Index2 = 1;

    public TraderList() {
        setVersioned(false);

        notificationPanel = new NotificationPanel("notification");
        notificationPanel.setMaxMessages(3);
        notificationPanel.setOutputMarkupId(true);
        add(notificationPanel);

        add(new Label("header", of(getTitle())));
        add(new Label("tradersCount", of(traderBean.getTradersCount())));

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

        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new PropertyColumn<>(of("Exchange"), "exchange"));
        list.add(new PropertyColumn<>(of("Coin"), "pair"));
        list.add(new TraderColumn(of("Balance"), balanceMap));
        list.add(new TraderColumn(of("Estimate"), estimateMap));
        list.add(new AbstractColumn<Trader, String>(of("Lot")) {
            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                Trader trader = rowModel.getObject();
                BigDecimal lot = new BigDecimal("0");

                try {
                    OrderBook orderBook = traderService.getOrderBook(trader.getExchangePair());

                    if (orderBook != null ){
                        BigDecimal price = orderBook.getAsks().get(0).getLimitPrice();

                        BigDecimal minOrderAmount = traderService.getMinOrderVolume(trader.getCounterSymbol());

                        minOrderAmount = minOrderAmount.divide(price, 8, ROUND_HALF_UP);

                        lot = trader.getVolume().divide(trader.getHigh().subtract(trader.getLow()).divide(trader.getSpread(),
                                8, ROUND_HALF_UP), 8, ROUND_HALF_UP);

                        lot = lot.compareTo(minOrderAmount) > 0 ? lot : minOrderAmount;

                        lot = lot.multiply(price);
                    }
                } catch (Exception e) {
                    //zero
                }

                cellItem.add(new Label(componentId, of(lot)));
            }
        });
        list.add(new TraderColumn(of("Buy"), buyMap));
        list.add(new TraderColumn(of("Sell"), sellMap));
        list.add(new TraderColumn(of("Bid"), bidMap));
        list.add(new TraderColumn(of("Ask"),askMap));
        list.add(new PropertyColumn<>(of("Low"), "low"));
        list.add(new PropertyColumn<>(of("High"), "high"));

        list.add(new AbstractColumn<Trader, String>(of("")) {
            @Override
            public Component getHeader(String componentId) {
                return new NavbarAjaxLink<String>(componentId, Model.of("Add")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setResponsePage(TraderEdit.class);
                    }
                }.setIconType(GlyphIconType.plus);
            }

            @Override
            public void populateItem(final Item<ICellPopulator<Trader>> cellItem, String componentId, final IModel<Trader> rowModel) {
                cellItem.add(new NavbarAjaxLink(componentId, Model.of(rowModel.getObject().isRunning() ? "Edit" : "Run")) {
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
        table.setTableBodyCss("body_scroll");
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
                        AccountInfo accountInfo = ((AccountInfo) payload);

                        for (ExchangePair exchangePair : balanceMap.keySet()){
                            if (exchangePair.getExchangeType().equals(exchangeMessage.getExchangeType())){
                                BigDecimal balance = accountInfo.getBalance(exchangePair.getCurrency());

                                update(handler, balanceMap.get(exchangePair), balance);
                            }
                        }

                        BigDecimal estimate = ZERO;

                        for (Wallet wallet : accountInfo.getWallets()){
                            estimate = estimate.add(traderService.getEstimateBalance(exchangeMessage.getExchangeType(),
                                    wallet.getCurrency(), wallet.getBalance()));
                        }

                        switch (exchangeMessage.getExchangeType()){
                            case CEXIO:
                                if (accountInfo.getBalance("GHS").equals(ZERO)|| accountInfo.getBalance("USD").equals(ZERO)){
                                    return;
                                }

                                update(handler, cexioCoins, estimate);
                                update(handler, cexioBTC, ((AccountInfo) payload).getBalance("BTC"));
                                break;
                            case CRYPTSY:
                                update(handler, cryptsyCoins, estimate);
                                update(handler, cryptsyBTC, ((AccountInfo) payload).getBalance("BTC"));
                                break;
                            case BITTREX:
                                update(handler, bittrexCoins, estimate);
                                update(handler, bittrexBTC, ((AccountInfo) payload).getBalance("BTC"));
                                break;
                            case BTCE:
                                OpenOrders openOrders = traderService.getOpenOrders(ExchangeType.BTCE);
                                for (LimitOrder limitOrder : openOrders.getOpenOrders()){
                                    estimate = estimate.add(traderService.getEstimateVolume(
                                            TraderUtil.getPair(limitOrder.getCurrencyPair()),
                                            limitOrder.getTradableAmount()));
                                }

                                update(handler, btceCoins, estimate);
                                update(handler, btceBTC, ((AccountInfo) payload).getBalance("BTC"));
                                break;
                            case BTER:
                                update(handler, bterCoins, estimate);
                                update(handler, bterBTC, ((AccountInfo) payload).getBalance("BTC"));
                                break;
                        }

                        //update chart balance
                        if (lastChartValueMap.get(exchangeMessage.getExchangeType()).compareTo(estimate) != 0) {
                            Point point = new Point(chartIndex++, estimate);
                            JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();
                            String jsonPoint = renderer.toJson(point);
                            String javaScript = "var chartVarName = " + chart.getJavaScriptVarName() + ";\n";
                            javaScript += "var seriesIndex = " + exchangeMessage.getExchangeType().ordinal() + ";\n";
                            javaScript += "eval(chartVarName).series[seriesIndex].addPoint(" + jsonPoint + ", true, true);\n";

                            handler.appendJavaScript(javaScript);

                            lastChartValueMap.put(exchangeMessage.getExchangeType(), estimate);
                        }
                    }else if (payload instanceof BalanceHistory){
                        BalanceHistory balanceHistory = (BalanceHistory) payload;

                        if (System.currentTimeMillis() - lastChart4Time > 1000*60){
                            lastChart4Time = System.currentTimeMillis();

                            OrderVolume orderVolume = traderService.getOrderVolumeRate();

                            JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();

                            //update chart order rate
                            if (orderVolume.getVolume().compareTo(ZERO) != 0) {
//                                javaScript = "var chartVarName = " + chart2.getJavaScriptVarName() + ";";
//                                javaScript += "eval(chartVarName).series[" + 0 + "].addPoint("
//                                        + renderer.toJson(new Point(chart2Index++, orderVolume.getVolume()))
//                                        + ", true, true);";
//
//                                handler.appendJavaScript(javaScript);


                                if (orderVolume.getAskVolume().compareTo(ZERO) != 0) {
                                    String javaScript = "var chartVarName = " + chart3.getJavaScriptVarName() + ";";
                                    javaScript += "eval(chartVarName).series[" + 0 + "].addPoint("
                                            + renderer.toJson(new Point(orderVolume.getDate().getTime(), orderVolume.getAskVolume()))
                                            + ", true, true);";

                                    handler.appendJavaScript(javaScript);
                                }

                                if (orderVolume.getBidVolume().compareTo(ZERO) != 0) {
                                    String javaScript = "var chartVarName = " + chart3.getJavaScriptVarName() + ";";
                                    javaScript += "eval(chartVarName).series[" + 1 + "].addPoint("
                                            + renderer.toJson(new Point(orderVolume.getDate().getTime(), orderVolume.getBidVolume()))
                                            + ", true, true);";

                                    handler.appendJavaScript(javaScript);
                                }

                                //chart4
                                Volume volume = traderService.getVolume(balanceHistory);

                                lastChart4Value = lastChart4Value.add(volume.getVolume());

                                String javaScript = "var chartVarName = " + chart4.getJavaScriptVarName() + ";";
                                javaScript += "eval(chartVarName).series[" + 0 + "].addPoint("
                                        + renderer.toJson(new Point(System.currentTimeMillis(), lastChart4Value))
                                        + ", true, true);";

                                handler.appendJavaScript(javaScript);
                            }
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

                                //estimate
                                try {
                                    update(handler, estimateMap.get(exchangePair),
                                            traderService.getTicker(exchangePair).getLast()
                                                    .multiply(traderService.getAccountInfo(exchangeMessage.getExchangeType())
                                                            .getBalance(exchangePair.getCurrency()))
                                                    .setScale(8, BigDecimal.ROUND_HALF_UP));
                                } catch (Exception e) {
                                    //no ticker
                                }
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
            options.setChartOptions(new ChartOptions(SeriesType.SPLINE).setHeight(300));
            options.setGlobal(new Global().setUseUTC(false));

            options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
            options.setTitle(new Title(""));
            //options.setLegend(new Legend(Boolean.FALSE));

            options.setxAxis(new Axis().setType(AxisType.LINEAR));

            options.setyAxis(new Axis().setTitle(new Title("")));

            options.setPlotOptions(new PlotOptionsChoice().setSpline(new PlotOptions()
                    .setMarker(new Marker(false))
                    .setLineWidth(1)));

            for (ExchangeType exchangeType : ExchangeType.values()){
                AccountInfo accountInfo = traderService.getAccountInfo(exchangeType);

                BigDecimal value = BigDecimal.ZERO;

                if (accountInfo != null) {
                    for (Wallet wallet :accountInfo.getWallets()){
                        value = value.add(traderService.getEstimateBalance(exchangeType, wallet.getCurrency(), wallet.getBalance()));
                    }
                }

                List<Point> data = new ArrayList<>();
                for (int i = 0; i < 500; ++i) {
                    data.add(new Point(0, value));
                }

                options.addSeries(new PointSeries().setData(data).setName(exchangeType.name()));

                lastChartValueMap.put(exchangeType, value);
            }

            add(chart = new Chart("chart", options));
        }

        //Chart 2
        long startDate = System.currentTimeMillis() - 1000 * 60 * 60 * 24;
        List<OrderVolume> orderVolumes = traderService.getOrderVolumeRates(new Date(startDate));

        List<OrderVolume> filteredOrderVolumes = new ArrayList<>();
        for (int i = 0; i < orderVolumes.size(); i += orderVolumes.size()/256){
            filteredOrderVolumes.add(orderVolumes.get(i));
        }

//        {
//            Options options = new Options();
//            options.setChartOptions(new ChartOptions(SeriesType.AREASPLINE).setHeight(500).setZoomType(ZoomType.X));
//            options.setGlobal(new Global().setUseUTC(false));
//
//            options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
//            options.setTitle(new Title(""));
//            //options.setLegend(new Legend(Boolean.FALSE));
//
//            options.setxAxis(new Axis().setType(AxisType.LINEAR));
//
//            options.setyAxis(new Axis().setTitle(new Title("")));
//
//            options.setPlotOptions(new PlotOptionsChoice().setAreaspline(new PlotOptions()
//                    .setFillColor(new LinearGradient(LinearGradient.GradientDirection.VERTICAL))
//                    .setMarker(new Marker(false))
//                    .setLineWidth(1)
//                    .setTurboThreshold(20000)));
//
//            {
//                List<Point> data = new ArrayList<>();
//                for (OrderVolume orderVolume : filteredOrderVolumes){
//                    data.add(new Point(chart2Index++, orderVolume.getVolume()));
//                }
//
//                options.addSeries(new PointSeries().setData(data).setName("Заявки / час").setColor(new HighchartsColor(1)));
//            }
//
//            add(chart2 = new Chart("chart2", options));
//        }

        //Chart 3
        {
            Options options = new Options();
            options.setChartOptions(new ChartOptions(SeriesType.SPLINE).setHeight(300).setZoomType(ZoomType.X));
            options.setGlobal(new Global().setUseUTC(false));

            options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
            options.setTitle(new Title(""));
            //options.setLegend(new Legend(Boolean.FALSE));

            options.setxAxis(new Axis().setType(AxisType.DATETIME));

            options.setyAxis(new Axis().setTitle(new Title("")));

            options.setPlotOptions(new PlotOptionsChoice().setSpline(new PlotOptions()
                    .setMarker(new Marker(false))
                    .setLineWidth(1)
                    .setTurboThreshold(20000)));

            List<Point> dataAsk = new ArrayList<>();
            List<Point> dataBid = new ArrayList<>();

            for (OrderVolume orderVolume : filteredOrderVolumes){
                dataAsk.add(new Point(orderVolume.getDate().getTime(), orderVolume.getAskVolume()));
                dataBid.add(new Point(orderVolume.getDate().getTime(), orderVolume.getBidVolume()));
            }

            options.addSeries(new PointSeries().setData(dataAsk).setName("Sell / hour").setColor(new HighchartsColor(3)));
            options.addSeries(new PointSeries().setData(dataBid).setName("Buy / hour").setColor(new HighchartsColor(2)));

            add(chart3 = new Chart("chart3", options));
        }

        //Chart 4
        {
            Options options = new Options();
            options.setChartOptions(new ChartOptions(SeriesType.SPLINE).setHeight(300).setZoomType(ZoomType.X));
            options.setGlobal(new Global().setUseUTC(false));

            options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
            options.setTitle(new Title(""));
            //options.setLegend(new Legend(Boolean.FALSE));

            options.setxAxis(new Axis().setType(AxisType.DATETIME));

            options.setyAxis(new Axis().setTitle(new Title("")));

            options.setPlotOptions(new PlotOptionsChoice().setSpline(
                    new PlotOptions()
                            .setFillColor(new LinearGradient(LinearGradient.GradientDirection.VERTICAL))
                            .setMarker(new Marker(false))
                            .setLineWidth(1)
                            .setTurboThreshold(20000)));

            {
                List<Point> data = new ArrayList<>();
                List<Volume> volumes = traderService.getVolumes(new Date(startDate));

                long time = 0;
                BigDecimal volumeSum = BigDecimal.ZERO;

                for (Volume volume : volumes){
                    volumeSum = volumeSum.add(volume.getVolume());

                    if (volume.getDate().getTime() - time > 1000*60*5) {
                        time = volume.getDate().getTime();
                        data.add(new Point(time, volumeSum));
                    }
                }
                options.addSeries(new PointSeries().setData(data).setName("Profit"));

                lastChart4Value = !data.isEmpty() ? (BigDecimal) (data.get(data.size() - 1)).getY() : BigDecimal.ZERO;
            }

            add(chart4 = new Chart("chart4", options));
        }
    }

    private void update(WebSocketRequestHandler handler, Component component, BigDecimal newValue){
        if (component != null){
            String s = getConverter(BigDecimal.class).convertToString(newValue, getLocale());

            int compare = s.compareTo(component.getDefaultModelObjectAsString());

            if (compare != 0){
                String color = compare > 0 ? "'#EFFBEF'" : "'#FBEFEF'";

                handler.appendJavaScript(new JsStatement().$(component)
                        .chain("parent")
                        .chain("animate", "{backgroundColor: '#FAFAFA'}")
                        .render());

                handler.appendJavaScript(new JsStatement().$(component)
                        .chain("parent")
                        .chain("animate", "{backgroundColor:" + color + "}")
                        .render());

                component.setDefaultModelObject(s);
                handler.add(component);
            }
        }
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(CssHeaderItem.forReference(new PackageResourceReference(TraderList.class, "TraderList.css")));
    }
}
