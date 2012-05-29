package ru.inhell.aida.light.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * @author  Anatoly Ivanov java@inheaven.ru
 * Date: 02.05.12 23:54
 */
public class LightWebApplication extends WebApplication{
    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }
}
