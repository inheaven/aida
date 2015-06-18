package ru.inheaven.aida.happy.trading.web.admin;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import ru.inheaven.aida.happy.trading.service.StatusService;
import ru.inhell.aida.common.wicket.BroadcastBehavior;

import javax.inject.Inject;

/**
 * @author inheaven on 16.06.2015 19:45.
 */
public class ServiceStatusPage extends WebPage{
    @Inject
    private StatusService statusService;

    public ServiceStatusPage() {
        Component marketDataEndpointOpen = new Label("marketDataEndpointOpen", statusService.isMarketDataEndpointOpen()).setOutputMarkupId(true);
        add(marketDataEndpointOpen);

        Component tradingEndpointOpen = new Label("tradingEndpointOpen", statusService.isTradingEndpointOpen()).setOutputMarkupId(true);
        add(tradingEndpointOpen);

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
                    case "UPDATE_TRADE":
                        handler.add(tradeCount.setDefaultModelObject(payload));
                        break;
                    case "UPDATE_DEPTH":
                        handler.add(depthCount.setDefaultModelObject(payload));
                        break;
                    case "UPDATE_ORDER":
                        handler.add(orderCount.setDefaultModelObject(payload));
                        break;
                }
            }
        });
    }
}

