package ru.inhell.aida.charts.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 26.05.12 0:15
 */
public class ChartsWebApplication extends WebApplication {
    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }
}
