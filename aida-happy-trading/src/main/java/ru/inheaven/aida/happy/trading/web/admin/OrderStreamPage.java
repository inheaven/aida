package ru.inheaven.aida.happy.trading.web.admin;

import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import ru.inheaven.aida.happy.trading.strategy.BaseStrategy;
import ru.inheaven.aida.happy.trading.web.BasePage;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import java.time.LocalTime;

/**
 * @author inheaven on 10.07.2015 23:13.
 */
public class OrderStreamPage extends BasePage{
    public OrderStreamPage(PageParameters pageParameters) {
        super();

        String param = pageParameters.get("t").toString("this_week");

        add(new BroadcastBehavior(BaseStrategy.class) {
            private int count = 0;

            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                if (key.contains(param)) {

                    String br = key.contains("profit") ?  "<br/><br/>" + count++ + ". " + LocalTime.now().toString() + " ": "";

                    handler.appendJavaScript("$('#order_stream').append(' "+ br + payload +"');");

                    if (key.contains("profit")){
                        handler.appendJavaScript("$('html, body').animate({scrollTop:$('#order_stream').height()}, 'slow');");
                    }
                }
            }
        });
    }
}
