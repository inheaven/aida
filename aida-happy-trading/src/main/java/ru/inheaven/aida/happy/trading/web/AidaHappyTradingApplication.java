package ru.inheaven.aida.happy.trading.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import ru.inheaven.aida.happy.trading.web.admin.ServiceStatusPage;

import javax.inject.Singleton;

/**
 * @author inheaven on 16.06.2015 19:42.
 */
@Singleton
public class AidaHappyTradingApplication extends WebApplication{
    @Override
    protected void init() {

    }

    @Override
    public Class<? extends Page> getHomePage() {
        return ServiceStatusPage.class;
    }
}
