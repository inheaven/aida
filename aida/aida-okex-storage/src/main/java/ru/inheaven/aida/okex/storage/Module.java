package ru.inheaven.aida.okex.storage;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;
import ru.inheaven.aida.okex.storage.service.ClusterService;
import ru.inheaven.aida.okex.storage.websocket.OkexWsExchangeStorageNew;

import java.util.concurrent.CountDownLatch;

/**
 * @author Anatoly A. Ivanov
 * 31.08.2017 17:28
 */
public class Module extends AbstractModule {
    private static final Injector INJECTOR = Guice.createInjector(new Module());

    private static Injector getInjector() {
        return INJECTOR;
    }

    @Override
    protected void configure() {

    }

    public static void main(String[] args) {
        getInjector().getInstance(ClusterService.class);
        getInjector().getInstance(OkexWsExchangeStorageNew.class);

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
