package ru.inheaven.aida.happy.trading.web;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.rest.utils.mounting.PackageScanner;
import ru.inheaven.aida.happy.trading.web.admin.ClientEditPage;
import ru.inheaven.aida.happy.trading.web.admin.ClientListPage;
import ru.inheaven.aida.happy.trading.web.admin.OrderStreamPage;
import ru.inheaven.aida.happy.trading.web.admin.ServiceStatusPage;
import ru.inheaven.aida.happy.trading.web.client.AccountInfoPage;
import ru.inheaven.aida.happy.trading.web.client.ArbitragePage;
import ru.inheaven.aida.happy.trading.web.client.DepthPage;

import javax.inject.Singleton;

/**
 * @author inheaven on 16.06.2015 19:42.
 */
@Singleton
public class AidaHappyTradingApplication extends WebApplication{
    @Override
    protected void init() {
        Bootstrap.install(Application.get(), new BootstrapSettings()
                .setThemeProvider(new BootswatchThemeProvider(BootswatchTheme.Darkly)));

        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        mountPage("status", ServiceStatusPage.class);
        mountPage("client", ClientListPage.class);
        mountPage("client_edit", ClientEditPage.class);
        mountPage("order_stream", OrderStreamPage.class);
        mountPage("account", AccountInfoPage.class);
        mountPage("arbitrage", ArbitragePage.class);
        mountPage("depth", DepthPage.class);

        getDebugSettings().setDevelopmentUtilitiesEnabled(true);
        getDebugSettings().setComponentUseCheck(true);

        PackageScanner.scanPackage("ru.inheaven.aida.happy.trading.rest");
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return AccountInfoPage.class;
    }
}
