package ru.inheaven.aida.happy.trading.web.admin;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import ru.inheaven.aida.happy.trading.service.StatusService;
import ru.inhell.aida.common.util.DateUtil;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import javax.inject.Inject;
import java.util.Date;

/**
 * @author inheaven on 16.06.2015 19:45.
 */
public class ServiceStatusPage extends WebPage{
    @Inject
    private StatusService statusService;

    public ServiceStatusPage() {
        Component marketOpen = new Label("marketOpen", 0).setOutputMarkupId(true);
        add(marketOpen);

        Component tradingOpen = new Label("tradingOpen", 0).setOutputMarkupId(true);
        add(tradingOpen);

        Component tradeCount = new Label("tradeCount", statusService.getTradeCount()).setOutputMarkupId(true);
        add(tradeCount);

        Component depthCount = new Label("depthCount", statusService.getDepthCount()).setOutputMarkupId(true);
        add(depthCount);

        Component orderCount = new Label("orderCount", statusService.getOrderCount()).setOutputMarkupId(true);
        add(orderCount);

        add(new BroadcastBehavior(StatusService.class) {
            @Override
            protected void onBroadcast(WebSocketRequestHandler handler, String key, Object payload) {
                switch (key){
                    case "update_market":
                        handler.add(marketOpen.setDefaultModelObject(DateUtil.getTimeString(new Date((Long) payload))));
                        break;
                    case "update_trading":
                        handler.add(tradingOpen.setDefaultModelObject(DateUtil.getTimeString(new Date((Long)payload))));
                        break;
                    case "update_trade":
                        handler.add(tradeCount.setDefaultModelObject(payload));
                        break;
                    case "update_depth":
                        handler.add(depthCount.setDefaultModelObject(payload));
                        break;
                    case "update_order":
                        handler.add(orderCount.setDefaultModelObject(payload));
                        break;
                }
            }
        });
    }
}

