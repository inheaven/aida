package ru.inheaven.aida.coin.web;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 06.01.14 15:13
 */
public class AidaCoinWebApplication extends WebApplication {
    @Override
    public Class<? extends Page> getHomePage() {
        return TraderList.class;
    }

    @Override
    protected void init() {
        super.init();

        Bootstrap.install(Application.get(), new BootstrapSettings());

        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this, (ejbName, ejbType)
                -> "java:module/" + (ejbName == null ? ejbType.getSimpleName() : ejbName)));
    }
}
