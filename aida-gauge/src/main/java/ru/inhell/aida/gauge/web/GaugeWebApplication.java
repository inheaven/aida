package ru.inhell.aida.gauge.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 13.12.11 3:15
 */
public class GaugeWebApplication extends WebApplication{
    @Override
        protected void init() {
            super.init();

            getComponentInstantiationListeners().add(new JavaEEComponentInjector(this));
        }

        @Override
        public Class<? extends Page> getHomePage() {
            return GaugeTestPage.class;
        }
}
