package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.resource.JQueryResourceReference;
import ru.inheaven.aida.happy.trading.entity.UserInfo;
import ru.inheaven.aida.happy.trading.entity.UserInfoTotal;
import ru.inheaven.aida.happy.trading.mapper.UserInfoTotalMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.UserInfoService;
import ru.inheaven.aida.happy.trading.web.BasePage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 19.07.2015 17:55.
 */
public class AccountInfoPage extends BasePage{
    private Long accountId = 7L;

    private static final List<String> SPOT = Arrays.asList("BTC_SPOT", "LTC_SPOT", "USD_SPOT");
    private static final List<String> FUTURES = Arrays.asList("BTC", "LTC");

    private BigDecimal usdTotal = ZERO;
    private BigDecimal btcPrice = ZERO;

    public AccountInfoPage(PageParameters pageParameters) {
        accountId = pageParameters.get("a").toLong(7);

        add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                JsonArrayBuilder volumes = Json.createArrayBuilder();
                Module.getInjector().getInstance(UserInfoTotalMapper.class)
                        .getUserInfoTotals(accountId, new Date(System.currentTimeMillis() - 24*60*60*1000))
                        .forEach(i -> {
                            BigDecimal volume = i.getFuturesVolume().add(i.getSpotVolume()).setScale(3, HALF_UP);
                            if (volume.compareTo(ZERO) > 0) {
                                volumes.add(Json.createArrayBuilder().add(i.getCreated().getTime()).add(volume));
                            }
                        });
                response.render(OnDomReadyHeaderItem.forScript("all_order_rate_chart.series[0].setData(" +
                        volumes.build().toString() + ");"));

//                JsonArrayBuilder times = Json.createArrayBuilder();
//                Module.getInjector().getInstance(OrderMapper.class).getLast6HourOrderTimes()
//                        .stream()
//                        .sorted(Comparator.<Date>naturalOrder())
//                        .forEach(d -> times.add(Json.createArrayBuilder().add(d.getTime()).add(tradeVolume)));
//
//                response.render(OnDomReadyHeaderItem.forScript("all_order_rate_chart.series[1].setData(" +
//                        times.build().toString() + ");"));
            }
        });

        add(new BroadcastBehavior(UserInfoService.class){

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                UserInfo info = (UserInfo) payload;

                if (FUTURES.contains(info.getCurrency())) {
                    handler.appendJavaScript(info.getCurrency().toLowerCase() + "_equity_chart.series[0].addPoint(" +
                            Json.createArrayBuilder()
                                    .add(info.getCreated().getTime())
                                    .add((info.getAccountRights().setScale(3, HALF_UP)))
                                    .build().toString() + ", true)");

                    handler.appendJavaScript(info.getCurrency().toLowerCase() + "_profit_chart.series[0].addPoint(" +
                            Json.createArrayBuilder()
                                    .add(info.getCreated().getTime())
                                    .add((info.getProfitReal().add(info.getProfitUnreal()).setScale(3, HALF_UP)))
                                    .build().toString() + ", true);");

                    handler.appendJavaScript(info.getCurrency().toLowerCase() + "_margin_chart.series[0].addPoint(" +
                            Json.createArrayBuilder()
                                    .add(info.getCreated().getTime())
                                    .add((info.getKeepDeposit().setScale(3, HALF_UP)))
                                    .build().toString() + ", true);");

                    if (info.getRiskRate().compareTo(BigDecimal.valueOf(5)) < 0){
                        handler.appendJavaScript("" +
                                "var msg = new SpeechSynthesisUtterance('уровень маржи предельный');\n" +
                                "msg.lang='ru-RU'; msg.pitch=1.2;\n" +
                                "window.speechSynthesis.speak(msg);");
                    }
                }else if (SPOT.contains(info.getCurrency())) {
                    handler.appendJavaScript(info.getCurrency().toLowerCase() + "_chart.series[0].addPoint(" +
                            Json.createArrayBuilder()
                                    .add(info.getCreated().getTime())
                                    .add((info.getAccountRights().add(info.getKeepDeposit()).setScale(3, HALF_UP)))
                                    .build().toString() + ", true)");
                }
            }
        });

        add(new BroadcastBehavior(UserInfoTotal.class){
                @Override
                protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                    UserInfoTotal total = (UserInfoTotal) payload;

                    usdTotal = total.getFuturesTotal().add(total.getSpotTotal());
                    btcPrice = total.getBtcPrice();

                    handler.appendJavaScript("usd_total_chart.series[0].addPoint([" +
                            total.getCreated().getTime() + "," + usdTotal.divide(total.getBtcPrice(), 3, HALF_UP) + "])");
                    handler.appendJavaScript("btc_price_chart.series[0].addPoint([" +
                            total.getCreated().getTime() + "," + total.getBtcPrice().setScale(2, HALF_UP) + "])");
                    handler.appendJavaScript("ltc_price_chart.series[0].addPoint([" +
                            total.getCreated().getTime() + "," + total.getLtcPrice().setScale(3, HALF_UP) + "])");

                    BigDecimal volume = total.getFuturesVolume().add(total.getSpotVolume()).setScale(3, HALF_UP);
                    if (volume.compareTo(ZERO) > 0) {
                        handler.appendJavaScript("all_order_rate_chart.series[0].addPoint([" +
                                total.getCreated().getTime() + "," +  volume + "]);");
                    }
                }
            }
        );

        add(new BroadcastBehavior(OrderService.class) {
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                if (key.contains("trade_profit") && usdTotal.compareTo(ZERO) > 0 && btcPrice.compareTo(ZERO) > 0) {
                    BigDecimal tradeProfit = ((BigDecimal)payload);
                    BigDecimal valuationProfit = usdTotal.add(tradeProfit)
                            .subtract(BigDecimal.valueOf(10000))
                            .divide(BigDecimal.valueOf(100), 8, HALF_UP);

                    handler.appendJavaScript("all_order_rate_chart.setTitle({text: " +
                            "'Trade Profit: " +  tradeProfit.setScale(2, HALF_UP) +
                            ", Valuation Profit: " + valuationProfit.setScale(2, HALF_UP) + "%'});");
                }
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highstock.js"));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highcharts-more.js"));
        response.render(JavaScriptHeaderItem.forUrl("./js/dark-unica-mod.js"));
        response.render(JavaScriptHeaderItem.forUrl("./js/account.js"));
    }

}
