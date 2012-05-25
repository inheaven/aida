package ru.inhell.aida.plot.web;

import org.apache.wicket.Page;
import org.apache.wicket.protocol.http.WebApplication;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.05.12 16:04
 */
public class AidaPlotWebApplication extends WebApplication {
    @Override
    public Class<? extends Page> getHomePage() {
        return HomePage.class;
    }
}
