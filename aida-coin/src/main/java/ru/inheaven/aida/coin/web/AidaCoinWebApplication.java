package ru.inheaven.aida.coin.web;

import de.agilecoders.wicket.core.Bootstrap;
import de.agilecoders.wicket.core.settings.BootstrapSettings;
import org.apache.wicket.Application;
import org.apache.wicket.Page;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.atmosphere.config.AtmosphereLogLevel;
import org.apache.wicket.atmosphere.config.AtmosphereTransport;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;

import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

        EventBus eventBus = new EventBus(this);
        eventBus.getParameters().setTransport(AtmosphereTransport.JSONP);
        eventBus.getParameters().setLogLevel(AtmosphereLogLevel.DEBUG);

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        final Runnable beeper = new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    eventBus.post(new Date());
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        };
        scheduler.scheduleWithFixedDelay(beeper, 2, 2, TimeUnit.SECONDS);

        Bootstrap.install(Application.get(), new BootstrapSettings());

        getMarkupSettings().setCompressWhitespace(true);
        getMarkupSettings().setDefaultMarkupEncoding("UTF-8");

        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this, (ejbName, ejbType)
                -> "java:module/" + (ejbName == null ? ejbType.getSimpleName() : ejbName)));
    }
}
