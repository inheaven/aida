package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import ru.inheaven.aida.happy.trading.entity.UserInfoTotal;
import ru.inheaven.aida.happy.trading.web.HighstockPage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

/**
 * @author inheaven on 29.09.2015 2:34.
 */
public class ProfitPage extends HighstockPage {
    public ProfitPage() {
        add(new BroadcastBehavior<UserInfoTotal>(UserInfoTotal.class){
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, UserInfoTotal u) {
                if (u.getAccountId() == 7){
//                    handler.appendJavaScript("chart_usd.setTitle({text: 'USD Net Asset " +  u.getSpotTotal().add(u.getFuturesTotal()) + "'});");

                }else if (u.getAccountId() == 9){
                    handler.appendJavaScript("chart_cny.setTitle({text: 'CNY Net Asset " +  u.getSpotTotal() + "'});");
                }
            }
        });
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        super.renderHead(response);

        response.render(JavaScriptHeaderItem.forUrl("./js/profit.js"));
    }
}
