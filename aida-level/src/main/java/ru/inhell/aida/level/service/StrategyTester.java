package ru.inhell.aida.level.service;

import ru.inhell.aida.common.entity.Bar;
import ru.inhell.aida.common.service.QuotesService;
import ru.inhell.aida.common.util.DateUtil;
import ru.inhell.aida.level.entity.Level;

import javax.ejb.Stateless;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 23.12.12 0:17
 */
@Stateless
public class StrategyTester {
    private static FileWriter fileWriter;

    public static void main(String... args) throws IOException {
        fileWriter = new FileWriter("C:\\Anatoly\\Java\\aida\\out\\out.txt");

        new StrategyTester().testStrategies();
    }

    public void testStrategies() throws IOException {
        QuotesService quotesService = new QuotesService();
        List<Bar> bars = quotesService.loadBars("C:\\Anatoly\\Java\\aida\\import\\SRH3_130109_130130.txt");

        String h = "delta,shift,takeprofit,count,gross,net";

        System.out.println(h);
        fileWriter.append(h).append('\n');

        for(int d = 5; d <= 10; d++){
            for (int t = 10; t <= 10; t++){
                for (int s = 0; s <= 0; s++){
                    testStrategy(bars, d, s, t, 1);
                }
            }
        }


        fileWriter.flush();
    }

    public void testStrategy(List<Bar> bars, int delta, int shift, int takeprofit, int lot) throws IOException {
        int count = 0;

        Map<Integer, Level> levels = new HashMap<>();

        Date startDate = DateUtil.getDate(2013, 1, 29);

        List<Integer> buyIndex = new ArrayList<>();

        for (Bar bar : bars){
            buyIndex.clear();

//            if (!DateUtil.isSameDay(startDate, bar.getDate())){
//                continue;
//            }

            //buy
            for (int i = (int) ((bar.getLow()-shift)/delta) + 1; i <= (bar.getHigh()-shift)/delta; ++i){
                Level level = levels.get(i);

                if (level == null){
                    level = new Level(i);

                    levels.put(i, level);
                }

                if (level.getActiveLot() == 0) {
                    level.setActiveLot(lot);
                    level.setBuyPrice(bar.getClose());

                    buyIndex.add(i);
                }
            }

            //sell
            for (int i = (int) ((bar.getLow()-shift-takeprofit)/delta) + 1; i <= (bar.getHigh()-shift-takeprofit)/delta; ++i){
                Level level = levels.get(i);

                if (level != null && level.getActiveLot() > 0 && !buyIndex.contains(i)){
                    level.setActiveLot(0);

                    count++;
                }
            }
        }

        int gross = count*takeprofit*lot;
        int net = gross - (int) (count*lot*1.4);

        String s = delta + "," + shift + "," + takeprofit + "," + count +"," +  gross+ "," + net;

        fileWriter.append(s).append('\n');
        System.out.println(s);
    }
}
