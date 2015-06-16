package ru.inheaven.aida.happy.trading.web;

import org.apache.wicket.Page;
import org.apache.wicket.guice.GuiceComponentInjector;
import org.apache.wicket.protocol.http.WebApplication;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.web.admin.ServiceStatusPage;

/**
 * @author inheaven on 16.06.2015 19:42.
 */
public class AidaHappyTradingApplication extends WebApplication{

    @Override
    protected void init() {
        getComponentInstantiationListeners().add(new GuiceComponentInjector(this, Module.getInjector()));
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return ServiceStatusPage.class;
    }
}
