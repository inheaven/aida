package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.resource.JQueryResourceReference;
import ru.inheaven.aida.happy.trading.entity.UserInfoTotal;
import ru.inheaven.aida.happy.trading.web.BasePage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

/**
 * @author inheaven on 29.09.2015 2:34.
 */
public class ProfitPage extends BasePage {
    public ProfitPage() {
        add(new BroadcastBehavior<UserInfoTotal>(UserInfoTotal.class){
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, UserInfoTotal u) {
                if (u.getAccountId() == 7){
                    handler.appendJavaScript("chart_usd.setTitle({text: 'USD Net Asset " +  u.getSpotTotal().add(u.getFuturesTotal()) + "'});");

                }else if (u.getAccountId() == 8){
                    handler.appendJavaScript("chart_cny.setTitle({text: 'CNY Net Asset " +  u.getSpotTotal() + "'});");
                }
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highstock.js"));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highcharts-3d.js"));

        response.render(JavaScriptHeaderItem.forUrl("./js/dark-unica-mod.js"));
        response.render(JavaScriptHeaderItem.forUrl("./js/profit.js"));
    }
}
