package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.Component;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.markup.head.OnDomReadyHeaderItem;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.resource.JQueryResourceReference;
import ru.inheaven.aida.happy.trading.entity.UserInfo;
import ru.inheaven.aida.happy.trading.entity.UserInfoTotal;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.mapper.UserInfoTotalMapper;
import ru.inheaven.aida.happy.trading.service.Module;
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
    private static final List<String> SPOT = Arrays.asList("BTC_SPOT", "LTC_SPOT", "USD_SPOT", "CNY_SPOT");
    private static final List<String> FUTURES = Arrays.asList("BTC", "LTC");

    public AccountInfoPage() {
        add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                JsonArrayBuilder volumes = Json.createArrayBuilder();
                Module.getInjector().getInstance(UserInfoTotalMapper.class)
                        .getUserInfoTotals(7L, new Date(System.currentTimeMillis() - 24*60*60*1000))
                        .forEach(i -> {
                            BigDecimal volume = i.getFuturesVolume().add(i.getSpotVolume()).setScale(3, HALF_UP);
                            if (volume.compareTo(ZERO) > 0) {
                                volumes.add(Json.createArrayBuilder().add(i.getCreated().getTime()).add(volume));
                            }
                        });
                response.render(OnDomReadyHeaderItem.forScript("all_order_rate_chart.series[0].setData(" +
                        volumes.build().toString() + ");"));

                Module.getInjector().getInstance(UserInfoTotalMapper.class)
                        .getUserInfoTotals(8L, new Date(System.currentTimeMillis() - 24*60*60*1000))
                        .forEach(i -> {
                            BigDecimal volume = i.getFuturesVolume().add(i.getSpotVolume()).setScale(3, HALF_UP);
                            if (volume.compareTo(ZERO) > 0) {
                                volumes.add(Json.createArrayBuilder().add(i.getCreated().getTime()).add(volume));
                            }
                        });
                response.render(OnDomReadyHeaderItem.forScript("all_order_rate_chart.series[1].setData(" +
                        volumes.build().toString() + ");"));
            }
        });

        add(new BroadcastBehavior(UserInfoService.class){

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                UserInfo info = (UserInfo) payload;

                if (info.getAccountId() == 7 || info.getAccountId() == 8) {
                    if (FUTURES.contains(info.getCurrency())) {
                        handler.appendJavaScript("chart_" + info.getAccountId() + "_" + info.getCurrency().toLowerCase() +
                                "_equity.series[0].addPoint(" + Json.createArrayBuilder()
                                .add(info.getCreated().getTime())
                                .add((info.getAccountRights().setScale(3, HALF_UP)))
                                .build().toString() + ", true)");
                    }else if (SPOT.contains(info.getCurrency())) {
                        handler.appendJavaScript("chart_" + info.getAccountId() + "_" + info.getCurrency().toLowerCase() +
                                ".series[0].addPoint(" + Json.createArrayBuilder()
                                .add(info.getCreated().getTime())
                                .add((info.getAccountRights().add(info.getKeepDeposit()).setScale(3, HALF_UP)))
                                .build().toString() + ", true)");
                    }
                }
            }
        });

        add(new BroadcastBehavior(UserInfoTotal.class){
                @Override
                protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                    UserInfoTotal u = (UserInfoTotal) payload;

                    if (u.getAccountId() == 7){
                        handler.appendJavaScript("chart_" + u.getAccountId() + "_btc_price.series[0].addPoint([" +
                                u.getCreated().getTime() + "," + u.getBtcPrice().setScale(2, HALF_UP) + "])");
                        handler.appendJavaScript("chart_" + u.getAccountId() + "_ltc_price.series[0].addPoint([" +
                                u.getCreated().getTime() + "," + u.getLtcPrice().setScale(3, HALF_UP) + "])");
                    }

                    if (u.getAccountId() == 7 || u.getAccountId() == 8) {
                        BigDecimal total = u.getFuturesTotal().add(u.getSpotTotal());

                        handler.appendJavaScript("chart_" + u.getAccountId() + "_total.series[0].addPoint([" +
                                u.getCreated().getTime() + "," + total.divide(u.getBtcPrice(), 3, HALF_UP) + "])");

                        BigDecimal volume = u.getFuturesVolume().add(u.getSpotVolume()).setScale(3, HALF_UP);

                        if (volume.compareTo(ZERO) > 0) {
                            if (u.getAccountId() == 7) {
                                handler.appendJavaScript("all_order_rate_chart.series[0].addPoint([" +
                                        u.getCreated().getTime() + "," +  volume + "]);");
                            }else if (u.getAccountId() == 8){
                                handler.appendJavaScript("all_order_rate_chart.series[1].addPoint([" +
                                        u.getCreated().getTime() + "," +  volume + "]);");
                            }
                        }

                        BigDecimal profit = Module.getInjector().getInstance(OrderMapper.class)
                                .getMinTradeProfit(u.getAccountId(), null, null, null);

                        if (profit != null) {
                            if (u.getAccountId() == 7){
                                BigDecimal valuationProfit = total.add(profit)
                                        .subtract(BigDecimal.valueOf(8408.8))
                                        .divide(BigDecimal.valueOf(100), 8, HALF_UP);

                                handler.appendJavaScript("chart_" + u.getAccountId() + "_total.setTitle({text: '" +
                                        "USD Total " +  profit.setScale(2, HALF_UP) +
                                        " " + valuationProfit.setScale(2, HALF_UP) + "%'});");

                            }else if (u.getAccountId() == 8){
                                BigDecimal valuationProfit = total.add(profit)
                                        .subtract(BigDecimal.valueOf(10159.0))
                                        .divide(BigDecimal.valueOf(100), 8, HALF_UP);

                                handler.appendJavaScript("chart_" + u.getAccountId() + "_total.setTitle({text: '" +
                                        "CNY Total " +  profit.setScale(2, HALF_UP) +
                                        " " + valuationProfit.setScale(2, HALF_UP) + "%'});");
                            }

                        }
                    }
                }
            }
        );
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
