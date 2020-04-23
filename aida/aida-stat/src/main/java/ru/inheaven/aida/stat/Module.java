package ru.inheaven.aida.stat;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.mybatis.guice.XMLMyBatisModule;

/**
 * @author inheaven on 18.12.2016.
 */
public class Module extends XMLMyBatisModule {
    private static Injector INJECTOR = Guice.createInjector(new Module());

    public static Injector getInjector() {
        return INJECTOR;
    }

    @Override
    protected void initialize() {

    }
}
