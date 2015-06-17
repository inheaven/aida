package ru.inheaven.aida.happy.trading.web.admin;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.LoadableDetachableModel;
import ru.inheaven.aida.happy.trading.service.StatusService;
import ru.inheaven.aida.happy.trading.service.TestService;

import javax.inject.Inject;

/**
 * @author inheaven on 16.06.2015 19:45.
 */
public class ServiceStatusPage extends WebPage{
    @Inject
    private StatusService statusService;

    public ServiceStatusPage() {
        Label marketDataEndpointOpen = new Label("marketDataEndpointOpen", new LoadableDetachableModel<Boolean>() {
            @Override
            protected Boolean load() {
                return statusService.isMarketDataEndpointOpen();
            }
        });

        add(marketDataEndpointOpen);

        Label tradingEndpointOpen = new Label("tradingEndpointOpen", statusService.isTradingEndpointOpen());
        add(tradingEndpointOpen);


        add(new Label("testLabel", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return testService.getCurrentHelloWorld();
            }
        }));
    }
}
