package ru.inheaven.aida.bittrex;

import com.google.inject.AbstractModule;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.util.concurrent.CountDownLatch;

/**
 * @author Anatoly A. Ivanov
 * 15.12.2017 22:21
 */
public class Module extends AbstractModule {
    private static final Injector INJECTOR = Guice.createInjector(new Module());

    public static Injector getInjector() {
        return INJECTOR;
    }

    @Override
    protected void configure() {

    }

    public static void main(String[] args) {
        getInjector().getInstance(SimpleBittrexBitcoin.class);
//        getInjector().getInstance(SimpleBittrexEthereum.class);
//        getInjector().getInstance(SimpleBittrexUsdt.class);

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
