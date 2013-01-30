package ru.inhell.aida.common.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.03.11 17:00
 */
public class AidaInjector {
    private static final Injector INJECTOR = Guice.createInjector(new AidaModule());

    public static <T> T getInstance(Class<T> tClass){
        return INJECTOR.getInstance(tClass);
    }
}
