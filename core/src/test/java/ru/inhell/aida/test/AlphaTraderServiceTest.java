package ru.inhell.aida.test;

import org.testng.annotations.Test;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.trader.AlphaTraderService;
import ru.inhell.aida.util.DateUtil;

import java.util.Arrays;
import java.util.Calendar;
import java.util.TimeZone;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.03.11 16:47
 */
public class AlphaTraderServiceTest {
    public static void main(String... args){
//        AidaInjector.getInstance(AlphaTraderService.class).process(1L);
        System.out.println(DateUtil.nowMsk());

    }
}
