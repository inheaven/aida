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
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import ru.inheaven.aida.happy.trading.service.UserInfoService;
import ru.inheaven.aida.happy.trading.web.BasePage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 19.07.2015 17:55.
 */
public class AccountInfoPage extends BasePage{
    private Long futureAccountId = 7L;
    private Long spotAccountId = 8L;

    private BigDecimal btcPrice = ZERO;
    private BigDecimal ltcPrice = ZERO;
    private BigDecimal ltcEquity = ZERO;
    private BigDecimal btcEquity = ZERO;

    private static final List<String> SPOT = Arrays.asList("BTC_SPOT", "LTC_SPOT", "USD_SPOT");

    public AccountInfoPage(PageParameters pageParameters) {
        futureAccountId = pageParameters.get("a").toLong(7);

        add(new Behavior() {
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                JsonArrayBuilder times = Json.createArrayBuilder();
                Module.getInjector().getInstance(OrderMapper.class).getLast6HourOrderTimes()
                        .stream()
                        .sorted(Comparator.<Date>naturalOrder())
                        .forEach(d -> times.add(Json.createArrayBuilder().add(d.getTime()).add(1).add(1)));

                response.render(OnDomReadyHeaderItem.forScript("all_order_rate_chart.series[0].setData(" +
                        times.build().toString() + ");"));
            }
        });

        add(new BroadcastBehavior(UserInfoService.class){

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                UserInfo info = (UserInfo) payload;

                if (info.getAccountId().equals(futureAccountId)) {
                    handler.appendJavaScript(info.getCurrency().toLowerCase() + "_equity_chart.series[0].addPoint(" +
                            Json.createArrayBuilder()
                                    .add(info.getCreated().getTime())
                                    .add((info.getAccountRights().setScale(3, HALF_UP)))
                                    .build().toString() + ", true)");

                    handler.appendJavaScript(info.getCurrency().toLowerCase() + "_profit_chart.series[0].addPoint(" +
                            Json.createArrayBuilder()
                                    .add(info.getCreated().getTime())
                                    .add((info.getProfitReal().setScale(3, HALF_UP)))
                                    .build().toString() + ", true);");

                    handler.appendJavaScript(info.getCurrency().toLowerCase() + "_margin_chart.series[0].addPoint(" +
                            Json.createArrayBuilder()
                                    .add(info.getCreated().getTime())
                                    .add((info.getKeepDeposit().setScale(3, HALF_UP)))
                                    .build().toString() + ", true);");

                    if ("BTC".equals(info.getCurrency())){
                        btcEquity = info.getAccountRights();
                    }else if ("LTC".equals(info.getCurrency())){
                        ltcEquity = info.getAccountRights();
                    }
                }else if (info.getAccountId().equals(spotAccountId)){
                    if (SPOT.contains(info.getCurrency())) {
                        handler.appendJavaScript(info.getCurrency().toLowerCase() + "_chart.series[0].addPoint(" +
                                Json.createArrayBuilder()
                                        .add(info.getCreated().getTime())
                                        .add((info.getAccountRights().add(info.getKeepDeposit()).setScale(3, HALF_UP)))
                                        .build().toString() + ", true)");
                    }else if ("ASSET".equals(info.getCurrency()) && ltcPrice.compareTo(ZERO) > 0 && btcPrice.compareTo(ZERO) > 0 &&
                            ltcEquity.compareTo(ZERO) > 0 && btcEquity.compareTo(ZERO) > 0){
                        handler.appendJavaScript("all_order_rate_chart.setTitle({text: 'Usd Total: " +
                                info.getAccountRights().add(btcEquity.multiply(btcPrice)).add(ltcEquity.multiply(ltcPrice))
                                        .setScale(2, HALF_UP) + "'});");
                    }
                }
            }
        });

        add(new BroadcastBehavior(OrderService.class) {
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                if (key.contains("close_order")) {
                    handler.appendJavaScript("all_order_rate_chart.series[0].addPoint([" +
                            System.currentTimeMillis() + "," + 1 + "]);");
                }
            }
        });

        add(new BroadcastBehavior(TradeService.class) {
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                if ("trade_btc_".equals(key)){
                    btcPrice = (BigDecimal) payload;
                }else if ("trade_ltc_".equals(key)){
                    ltcPrice = (BigDecimal) payload;
                }
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highstock.js"));
        response.render(JavaScriptHeaderItem.forUrl("./js/dark-unica-mod.js"));
        response.render(JavaScriptHeaderItem.forUrl("./js/account.js"));
    }

}
