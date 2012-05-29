package ru.inhell.aida.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import org.wicketstuff.javaee.injection.JavaEEComponentInjector;
import ru.inhell.aida.template.test.HelloMenu;
import ru.inhell.aida.web.order.OrderPage;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 25.07.11 23:09
 */
public class AidaWebApplication extends WebApplication{
    @Override
    protected void init() {
        super.init();

        getComponentInstantiationListeners().add(new JavaEEComponentInjector(this));

        mountPage("/test", HelloMenu.class);
    }

    @Override
    public Class<? extends Page> getHomePage() {
        return OrderPage.class;
    }
}
