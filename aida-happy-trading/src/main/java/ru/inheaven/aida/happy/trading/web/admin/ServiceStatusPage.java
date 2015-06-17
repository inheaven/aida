package ru.inheaven.aida.happy.trading.web.admin;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;
import ru.inheaven.aida.happy.trading.entity.BroadcastPayload;
import ru.inheaven.aida.happy.trading.service.StatusService;
import ru.inhell.aida.common.wicket.AjaxLabel;

import javax.inject.Inject;

/**
 * @author inheaven on 16.06.2015 19:45.
 */
public class ServiceStatusPage extends WebPage{
    @Inject
    private StatusService statusService;

    public ServiceStatusPage() {
        Label marketDataEndpointOpen = new AjaxLabel<>("marketDataEndpointOpen", statusService::isMarketDataEndpointOpen);
        add(marketDataEndpointOpen);

        Label tradingEndpointOpen = new AjaxLabel<>("tradingEndpointOpen", statusService::isTradingEndpointOpen);
        add(tradingEndpointOpen);

        Label tradeCount = new AjaxLabel<>("tradeCount", statusService::getTradeCount);
        add(tradeCount);

        Label depthCount = new AjaxLabel<>("depthCount", statusService::getDepthCount);
        add(depthCount);

        Label orderCount = new AjaxLabel<>("orderCount", statusService::getOrderCount);
        add(orderCount);

        add(new WebSocketBehavior(){
            @Override
            protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message) {
                if (message instanceof BroadcastPayload){
                    if (((BroadcastPayload) message).getProducer().equals(StatusService.class)){
                        switch ((String)((BroadcastPayload) message).getPayload()){
                            case "UPDATE_TRADE": handler.add(tradeCount); break;
                            case "UPDATE_DEPTH": handler.add(depthCount); break;
                            case "UPDATE_ORDER": handler.add(orderCount); break;
                        }
                    }
                }
            }
        });
    }
}

