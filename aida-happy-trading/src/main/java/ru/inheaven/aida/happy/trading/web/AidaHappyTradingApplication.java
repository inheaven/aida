package ru.inheaven.aida.happy.trading.web;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchTheme;
import de.agilecoders.wicket.themes.markup.html.bootswatch.BootswatchThemeProvider;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import ru.inheaven.aida.happy.trading.web.admin.ClientEditPage;
import ru.inheaven.aida.happy.trading.web.admin.ClientListPage;
import ru.inheaven.aida.happy.trading.web.admin.OrderStreamPage;
import ru.inheaven.aida.happy.trading.web.admin.ServiceStatusPage;
import ru.inheaven.aida.happy.trading.web.client.AccountInfoPage;

import javax.inject.Singleton;

/**
 * @author inheaven on 16.06.2015 19:42.
 */
@Singleton
public class AidaHappyTradingApplication extends WebApplication{
    @Override
    protected void init() {
        Bootstrap.install(Application.get(), new BootstrapSettings()
                .setThemeProvider(new BootswatchThemeProvider(BootswatchTheme.Readable)));

        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        mountPage("status", ServiceStatusPage.class);
        mountPage("client", ClientListPage.class);
        mountPage("client_edit", ClientEditPage.class);
        mountPage("order_stream", OrderStreamPage.class);
        mountPage("account", AccountInfoPage.class);

        getDebugSettings().setDevelopmentUtilitiesEnabled(true);
        getDebugSettings().setComponentUseCheck(true);

    }

    @Override
    public Class<? extends Page> getHomePage() {
        return OrderStreamPage.class;
    }
}
