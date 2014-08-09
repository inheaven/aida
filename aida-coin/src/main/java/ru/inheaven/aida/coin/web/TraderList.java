package ru.inheaven.aida.coin.web;

import com.googlecode.wickedcharts.highcharts.jackson.JsonRenderer;
import com.googlecode.wickedcharts.highcharts.options.*;
import com.googlecode.wickedcharts.highcharts.options.series.Point;
import com.googlecode.wickedcharts.highcharts.options.series.PointSeries;
import com.googlecode.wickedcharts.wicket6.highcharts.Chart;
import com.googlecode.wickedcharts.wicket6.highcharts.JsonRendererFactory;
import com.xeiam.xchange.dto.account.AccountInfo;
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
import ru.inheaven.aida.coin.entity.BalanceStat;
import ru.inheaven.aida.coin.entity.ExchangeMessage;
import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.service.TraderBean;
import ru.inheaven.aida.coin.service.TraderService;

import javax.ejb.EJB;
import java.math.BigDecimal;
import java.util.*;
import java.util.function.BiConsumer;

import static org.apache.wicket.model.Model.of;
import static ru.inheaven.aida.coin.entity.ExchangeName.BITTREX;

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
    private Map<ExchangePair, Component> balanceMap = new HashMap<>();
    private Map<ExchangePair, Component> buyMap = new HashMap<>();
    private Map<ExchangePair, Component> sellMap = new HashMap<>();

    private NotificationPanel notificationPanel;

    private Component bittrexBTC, bittrexCoins;

    private BigDecimal lastChartValue = new BigDecimal("0");
    private Chart chart;

    public TraderList() {
        //start service
        traderService.getBittrexExchange();

        notificationPanel = new NotificationPanel("notification");
        notificationPanel.setMaxMessages(3);
        notificationPanel.setOutputMarkupId(true);
        add(notificationPanel);

        add(bittrexBTC = new Label("bittrexBTC", Model.of("0")).setOutputMarkupId(true));
        add(bittrexCoins = new Label("bittrexCoins", Model.of("0")).setOutputMarkupId(true));

        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new PropertyColumn<>(of("Рынок"), "exchange"));
        list.add(new PropertyColumn<>(of("Монета"), "pair"));
        list.add(new PropertyColumn<>(of("Верх"), "high"));
        list.add(new PropertyColumn<>(of("Низ"), "low"));
        list.add(new PropertyColumn<>(of("Объем"), "volume"));
        list.add(new PropertyColumn<>(of("Спред"), "spread"));
        list.add(new TraderColumn(of("Цена"), lastMap));
        list.add(new TraderColumn(of("Баланс"), balanceMap));
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
        table.add(new TableBehavior().bordered());

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
                            BigDecimal balance = ((AccountInfo) payload).getBalance(exchangePair.getCurrency());

                            if (traderService.getTicker(exchangePair) != null) {
                                estimate = estimate.add(balance.multiply(traderService.getTicker(exchangePair).getLast()))
                                        .setScale(8, BigDecimal.ROUND_HALF_UP);
                            }

                            update(handler, balanceMap.get(exchangePair), balance.toString());
                        }

                        update(handler, bittrexCoins, estimate.toString());
                        update(handler, bittrexBTC, ((AccountInfo) payload).getBalance("BTC").toString());

                        //update chart
                        if (lastChartValue.compareTo(((AccountInfo) payload).getBalance("BTC")) != 0) {
                            lastChartValue = ((AccountInfo) payload).getBalance("BTC");

                            Point point = new Point(Calendar.getInstance().getTimeInMillis(), lastChartValue);
                            JsonRenderer renderer = JsonRendererFactory.getInstance().getRenderer();
                            String jsonPoint = renderer.toJson(point);
                            String javaScript = "var chartVarName = " + chart.getJavaScriptVarName() + ";\n";
                            javaScript += "var seriesIndex = " + 0 + ";\n";
                            javaScript += "eval(chartVarName).series[seriesIndex].addPoint(" + jsonPoint + ", true, true);\n";

                            handler.appendJavaScript(javaScript);
                        }
                    }else if (payload instanceof Ticker) {
                        Ticker ticker = (Ticker) exchangeMessage.getPayload();

                        Component component = lastMap.get(ExchangePair.of(exchangeMessage.getExchange(),
                                ticker.getCurrencyPair()));

                        update(handler, component, ticker.getLast().toString());

                    }else if (payload instanceof OpenOrders){
                        OpenOrders openOrders = (OpenOrders) exchangeMessage.getPayload();

                        Map<ExchangePair, BigDecimal> countBuyMap = new HashMap<>();
                        Map<ExchangePair, BigDecimal> countSellMap = new HashMap<>();

                        for (ExchangePair ep : buyMap.keySet()){
                            countBuyMap.put(ep, new BigDecimal("0"));
                            countSellMap.put(ep, new BigDecimal("0"));
                        }

                        for (LimitOrder order : openOrders.getOpenOrders()){
                            ExchangePair ep = ExchangePair.of(exchangeMessage.getExchange(), order.getCurrencyPair());

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
                                update(handler, buyMap.get(exchangePair), bigDecimal.toString());
                            }
                        });

                        countSellMap.forEach(new BiConsumer<ExchangePair, BigDecimal>() {
                            @Override
                            public void accept(ExchangePair exchangePair, BigDecimal bigDecimal) {
                                update(handler, sellMap.get(exchangePair), bigDecimal.toString());
                            }
                        });
                    }else if (payload instanceof String){
                        error((String)payload);

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
        Options options = new Options();
        options.setChartOptions(new ChartOptions(SeriesType.SPLINE));
        options.setGlobal(new Global().setUseUTC(false));

        options.setExporting(new ExportingOptions().setEnabled(Boolean.FALSE));
        options.setTitle(new Title(""));
        options.setLegend(new Legend(Boolean.FALSE));

        options.setxAxis(new Axis().setType(AxisType.DATETIME));

        options.setyAxis(new Axis().setTitle(new Title("")).setMin(0));

        options.setPlotOptions(new PlotOptionsChoice().setSpline(new PlotOptions().setMarker(new Marker(false))));

        List<Point> data = new ArrayList<>();

        List<BalanceStat> balanceStatList = traderService.getBalanceStats(BITTREX);
        if (balanceStatList != null){
            BigDecimal lastValue = new BigDecimal("0");

            for (BalanceStat balanceStat : balanceStatList){
                BigDecimal value = balanceStat.getAccountInfo().getBalance("BTC");

                if (value.compareTo(lastValue) != 0) {
                    data.add(new Point(balanceStat.getDate().getTime(), value));

                    lastValue = value;
                }
            }
        }

        int len = data.size();
        Number x = !data.isEmpty() ? data.get(0).getX() : new Date().getTime() - 60*60*1000;
        Number y = !data.isEmpty() ? data.get(0).getY() : 0;
        for (int i = 0; i < 100 - len; ++i){
            data.add(0, new Point(x, y));
        }

        options.addSeries(new PointSeries().setData(data).setName("Средства"));

        add(chart = new Chart("chart", options));
    }

    private void update(WebSocketRequestHandler handler, Component component, String newValue){
        if (component != null){
            int compare = newValue.compareTo(component.getDefaultModelObjectAsString());

            if (compare != 0){
                String color = compare > 0 ? "'#EFFBEF'" : "'#FBEFEF'";

                handler.appendJavaScript(new JsStatement().$(component)
                        .chain("parent")
                        .chain("css", "\"background-color\"", color)
                        .render());

//                handler.appendJavaScript(new JsStatement().$(component)
//                        .chain("effect", "\"highlight\"", "{color: " + color + "}")
//                        .render());

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
