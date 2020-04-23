package ru.inheaven.aida.account.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.mybatis.guice.XMLMyBatisModule;

/**
 * inheaven on 20.04.2016.
 */
public class Module extends XMLMyBatisModule {
    private static Injector injector = Guice.createInjector(new Module());

    @Override
    protected void initialize() {
    }

    public static Injector getInjector() {
        return injector;
    }
}
