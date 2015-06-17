package ru.inheaven.aida.happy.trading.web.admin;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
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
    }
}
