package ru.inhell.aida.test;

import org.testng.annotations.Test;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.trader.AlphaTraderService;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.03.11 16:47
 */
public class AlphaTraderServiceTest {
    public static void main(String... args){
        AidaInjector.getInstance(AlphaTraderService.class).process(1L);
    }
}
