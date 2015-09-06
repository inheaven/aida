package ru.inheaven.aida.happy.trading.web;

import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.protocol.http.IWebApplicationFactory;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WicketFilter;
import ru.inheaven.aida.happy.trading.service.*;

/**
 * @author inheaven on 17.06.2015 23:13.
 */
public class AidaHappyTradingApplicationFactory implements IWebApplicationFactory {
    @Override
    public WebApplication createApplication(WicketFilter filter) {
        Module.getInjector().getInstance(StrategyService.class);
        Module.getInjector().getInstance(UserInfoService.class);
        Module.getInjector().getInstance(OkcoinFixService.class);
        Module.getInjector().getInstance(OkcoinCnFixService.class);
        Module.getInjector().getInstance(FuturesPositionService.class);

        WebApplication application = Module.getInjector().getInstance(AidaHappyTradingApplication.class);
        application.getComponentInstantiationListeners().add(new GuiceComponentInjector(application, Module.getInjector()));

        return application;
    }

    @Override
    public void destroy(WicketFilter filter) {
        Module.getInjector().getInstance(OkcoinService.class).destroy();
    }
}
