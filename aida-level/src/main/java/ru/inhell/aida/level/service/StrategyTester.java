package ru.inhell.aida.level.service;

import ru.inhell.aida.common.entity.Bar;
import ru.inhell.aida.common.service.QuotesService;
import ru.inhell.aida.level.entity.Level;

import javax.ejb.Stateless;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 23.12.12 0:17
 */
@Stateless
public class StrategyTester {
    public static void main(String... args){

    }

    private void testStrategies(){
        QuotesService quotesService = new QuotesService();
        List<Bar> bars = quotesService.loadBars("D:\\Java\\aida\\quotes\\SPFB.SBRF_130108_130116.txt");

        testStrategy(bars, 10, 0, 10, 1);
    }

    private void testStrategy(List<Bar> bars, int delta, int shift, int profit, int lot){
        int orders = 0;

        List<Level> levels = new ArrayList<>();

        for (Bar bar : bars){
            //buy
            for (int i = (int) ((bar.getLow()-shift)/delta); i <= (bar.getHigh()-shift)/delta; ++i){
                Level level = levels.get(i);

                if (level == null){
                    level = new Level(i);
                    level.setActiveLot(lot);

                    levels.add(level);
                }
            }

            //sell
            for (int i = (int) ((bar.getLow()-shift)/delta); i <= (bar.getHigh()-shift)/delta; ++i){
                Level level = levels.get(i);


            }
        }



        System.out.println("Processed: " + bars.size());
    }
}
