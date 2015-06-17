package ru.inheaven.aida.happy.trading.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.mybatis.guice.XMLMyBatisModule;

/**
 * @author inheaven on 16.06.2015 19:49.
 */
public class Module extends XMLMyBatisModule{
    private static Injector injector = Guice.createInjector(new Module());

    @Override
    protected void initialize() {
        bind(StatusService.class);
        bind(OkcoinService.class);

    }

    public static Injector getInjector() {
        return injector;
    }
}
