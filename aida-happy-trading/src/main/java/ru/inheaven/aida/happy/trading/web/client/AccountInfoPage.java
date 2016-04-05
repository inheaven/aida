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
                        .getUserInfoTotals(1L, new Date(System.currentTimeMillis() - 24*60*60*1000))
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
            private BigDecimal usd_spot;
            private BigDecimal ltcSpot;
            private BigDecimal btcSpot;
            private BigDecimal cny_spot;
            private BigDecimal ltcCnSpot;
            private BigDecimal btcCnSpot;

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                UserInfo info = (UserInfo) payload;

                BigDecimal volume = info.getAccountRights().add(info.getKeepDeposit());

                handler.appendJavaScript("chart_8" + "_" + info.getCurrency().toLowerCase() +
                        ".series[0].addPoint(" + Json.createArrayBuilder()
                        .add(info.getCreated().getTime())
                        .add(volume.setScale(3, HALF_UP))
                        .build().toString() + ", true, true)");

                switch (info.getCurrency()){
                    case "USD_SPOT":
                        usd_spot = volume;
                        break;
                    case "CNY_SPOT":
                        cny_spot = volume;
                        break;
                    case "LTC_SPOT":
                        if (info.getAccountId() == 7){
                            ltcSpot = volume;
                        }else if (info.getAccountId() == 1){
                            ltcCnSpot = volume;
                        }
                        break;
                    case "BTC_SPOT":
                        if (info.getAccountId() == 7){
                            btcSpot = volume;
                        }else if (info.getAccountId() == 1){
                            btcCnSpot = volume;
                        }
                }
            }
        });

        add(new BroadcastBehavior(UserInfoTotal.class){
            Long lastProfit = System.currentTimeMillis();

                @Override
                protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                    UserInfoTotal u = (UserInfoTotal) payload;

                    if (u.getAccountId() == 1){
                        handler.appendJavaScript("chart_7_btc_price.series[0].addPoint([" +
                                u.getCreated().getTime() + "," + u.getSpotTotal().setScale(4, HALF_UP) + "])");
                        handler.appendJavaScript("chart_7_btc_price.series[1].addPoint([" +
                                u.getCreated().getTime() + "," + u.getBtcPrice().setScale(4, HALF_UP) + "])");
                    }

                    BigDecimal total = u.getFuturesTotal().add(u.getSpotTotal());

                    handler.appendJavaScript("chart_8_total.series[0].addPoint([" +
                            u.getCreated().getTime() + "," + total.divide(u.getBtcPrice(),4, HALF_UP) + "])");

                    BigDecimal volume = u.getFuturesVolume().add(u.getSpotVolume()).setScale(3, HALF_UP);

                    if (volume.compareTo(ZERO) > 0) {
                        handler.appendJavaScript("all_order_rate_chart.series[1].addPoint([" +
                                u.getCreated().getTime() + "," +  volume + "]);");
                    }
                }
            }
        );
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highstock.js"));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highcharts-3d.js"));
//        response.render(JavaScriptHeaderItem.forUrl("./highstock/highcharts-more.js"));

        response.render(JavaScriptHeaderItem.forUrl("./js/dark-unica-mod.js"));
        response.render(JavaScriptHeaderItem.forUrl("./js/account.js"));
    }

}
