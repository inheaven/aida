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
import ru.inheaven.aida.happy.trading.service.UserInfoService;
import ru.inheaven.aida.happy.trading.strategy.BaseStrategy;
import ru.inheaven.aida.happy.trading.web.BasePage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import javax.json.Json;
import javax.json.JsonArrayBuilder;
import java.util.Comparator;
import java.util.Date;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 19.07.2015 17:55.
 */
public class AccountInfoPage extends BasePage{
    private Long accountId;

    public AccountInfoPage(PageParameters pageParameters) {
        accountId = pageParameters.get("a").toLong(7);

        add(new Behavior(){
            @Override
            public void renderHead(Component component, IHeaderResponse response) {
                JsonArrayBuilder times = Json.createArrayBuilder();
                Module.getInjector().getInstance(OrderMapper.class).getLast100OrderTimes()
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

                handler.appendJavaScript(info.getCurrency().toLowerCase() + "_equity_chart.series[0].addPoint(" +
                        Json.createArrayBuilder()
                                .add(info.getCreated().getTime())
                                .add((info.getAccountRights().setScale(3, HALF_UP))).build().toString() + ", true, true, false);");

                handler.appendJavaScript(info.getCurrency().toLowerCase() + "_profit_chart.series[0].addPoint(" +
                        Json.createArrayBuilder()
                                .add(info.getCreated().getTime())
                                .add((info.getProfitReal().setScale(3, HALF_UP))).build().toString() + ", true, true, false);");

                handler.appendJavaScript(info.getCurrency().toLowerCase() + "_margin_chart.series[0].addPoint(" +
                        Json.createArrayBuilder()
                                .add(info.getCreated().getTime())
                                .add((info.getKeepDeposit().setScale(3, HALF_UP))).build().toString() + ", true, true, false);");
            }
        });

        add(new BroadcastBehavior(BaseStrategy.class) {
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                if (key.contains("profit")) {
                    handler.appendJavaScript("all_order_rate_chart.series[0].addPoint([" +
                            System.currentTimeMillis() + "," + 1 + "], true, true, false);");
                }
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forUrl("/highstock/highstock.js"));
        response.render(JavaScriptHeaderItem.forUrl("/highstock/themes/dark-unica-mod.js"));
        response.render(JavaScriptHeaderItem.forUrl("/js/account.js"));
    }

}
