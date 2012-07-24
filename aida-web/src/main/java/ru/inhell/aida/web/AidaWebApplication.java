package ru.inhell.aida.web;

import org.apache.wicket.Page;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;
import ru.inhell.aida.common.osgi.OsgiClassResolver;
import ru.inhell.aida.common.service.OsgiJndiNamingStrategy;
import ru.inhell.aida.template.test.HelloMenu;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 25.07.11 23:09
 */
public class AidaWebApplication extends WebApplication{
    @Override
    protected void init() {
        super.init();

        new EventBus(this);

        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this, new OsgiJndiNamingStrategy()));
        getApplicationSettings().setClassResolver(new OsgiClassResolver());

        mountPage("/test", HelloMenu.class);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }
}
