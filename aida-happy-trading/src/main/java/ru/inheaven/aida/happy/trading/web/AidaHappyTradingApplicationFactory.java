package ru.inheaven.aida.happy.trading.web;

import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.OkcoinService;
import ru.inheaven.aida.happy.trading.service.StrategyService;

/**
 * @author inheaven on 17.06.2015 23:13.
 */
public class AidaHappyTradingApplicationFactory implements IWebApplicationFactory {
    @Override
    public WebApplication createApplication(WicketFilter filter) {
        Module.getInjector().getInstance(StrategyService.class);

        WebApplication application = Module.getInjector().getInstance(AidaHappyTradingApplication.class);
        application.getComponentInstantiationListeners().add(new GuiceComponentInjector(application, Module.getInjector()));

        return application;
    }

    @Override
    public void destroy(WicketFilter filter) {
        Module.getInjector().getInstance(OkcoinService.class).destroy();
    }
}
