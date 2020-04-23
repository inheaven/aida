package ru.inheaven.aida.account.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;
import ru.inheaven.aida.account.web.client.AccountPage;

/**
 * inheaven on 19.04.2016.
 */
public class AidaAccountWebApplication extends WebApplication{
    @Override
    protected void init() {
        mountPage("account", AccountPage.class);
    }


    @Override
    public Class<? extends Page> getHomePage() {
        return AccountPage.class;
    }
}
