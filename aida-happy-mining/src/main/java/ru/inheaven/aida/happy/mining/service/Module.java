package ru.inheaven.aida.happy.mining.service;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Anatoly A. Ivanov
 *         Date: 08.01.2017.
 */
public class Module extends AbstractModule {
    private static Injector INJECTOR = Guice.createInjector(new Module());

    public static Injector getInjector() {
        return INJECTOR;
    }

    @Override
    protected void configure() {

    }
}
