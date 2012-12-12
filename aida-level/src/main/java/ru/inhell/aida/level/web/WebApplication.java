package ru.inhell.aida.level.web;

import org.apache.wicket.Page;
import org.apache.wicket.atmosphere.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;
import ru.inhell.aida.common.service.GlassfishJndiNamingStrategy;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 12.12.12 15:19
 */
public class WebApplication extends org.apache.wicket.protocol.http.WebApplication {
    private final static Logger log = LoggerFactory.getLogger(WebApplication.class);

    private EventBus eventBus;

    @Override
    public Class<? extends Page> getHomePage() {
        return StockList.class;
    }

    @Override
    protected void init() {
        super.init();

        this.eventBus = new EventBus(this);

        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this, new GlassfishJndiNamingStrategy()));

        getDebugSettings().setDevelopmentUtilitiesEnabled(true);
    }

    public EventBus getEventBus() {
        return eventBus;
    }
}
