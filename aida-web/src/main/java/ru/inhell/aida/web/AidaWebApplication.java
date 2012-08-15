package ru.inhell.aida.web;

import org.apache.wicket.Page;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.protocol.http.WebApplication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;
import ru.inhell.aida.common.osgi.OsgiClassResolver;
import ru.inhell.aida.common.service.OsgiJndiNamingStrategy;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 25.07.11 23:09
 */
public class AidaWebApplication extends WebApplication{
    private final static Logger log = LoggerFactory.getLogger(AidaWebApplication.class);

    private EventBus eventBus;

    @Override
    protected void init() {
        super.init();

        this.eventBus = new EventBus(this);

        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this, new OsgiJndiNamingStrategy()));
        getApplicationSettings().setClassResolver(new OsgiClassResolver());

        getDebugSettings().setDevelopmentUtilitiesEnabled(true);

        log.info("AidaWebApplication STARTED");
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
