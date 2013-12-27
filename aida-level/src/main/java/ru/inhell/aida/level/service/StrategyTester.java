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
        fileWriter = new FileWriter("D:\\AIDA\\import\\out.txt");

        new StrategyTester().testStrategiesSum();
    }

    public void testStrategiesSum() throws IOException {
        QuotesService quotesService = new QuotesService();
        List<Bar> bars1 = quotesService.loadBars("C:\\Users\\Anatoly\\Downloads\\SPFB.SBRF_130414_130514.txt");
        List<Bar> bars2 = quotesService.loadBars("C:\\Users\\Anatoly\\Downloads\\SPFB.Si_130414_130514.txt");

        List<Bar> bars = new ArrayList<>();

        for (int i=0; i < bars1.size(); ++i){
            Bar b1 = bars1.get(i);
            Bar b2 = bars2.get(i);

            bars.add(new Bar(b1.getDate(),
                    b1.getOpen() + b2.getOpen(),
                    b1.getHigh() + b2.getHigh(),
                    b1.getLow() + b2.getLow(),
                    b1.getClose() + b2.getClose(),
                    0));
        }

        String h = "date, delta,shift,takeprofit,count,gross,net";

        System.out.println(h);
        fileWriter.append(h).append('\n');

        for(int d = 1; d <= 11; d++){
            for (int t = 15; t <= 15; t++){
                for (int s = 0; s <= 0; s++){
                    testStrategy(bars, d, s, t, 1);
                }
            }
        }

        fileWriter.flush();
    }

    public void testStrategies() throws IOException {
        QuotesService quotesService = new QuotesService();
        List<Bar> bars = quotesService.loadBars("C:\\~\\Downloads\\SRH3_130108_130306.txt");

        String h = "date, delta,shift,takeprofit,count,gross,net";

        System.out.println(h);
        fileWriter.append(h).append('\n');

        for(int d = 12; d <= 12; d++){
            for (int t = 15; t <= 100; t++){
                for (int s = 0; s <= 0; s++){
                    testStrategy(bars, d, s, t, d);
                }
            }
        }

        fileWriter.flush();
    }

    public void testStrategy(List<Bar> bars, int delta, int shift, int takeprofit, int lot) throws IOException {
        int count = 0;
        int sum = 0;
        int days = 0;

        Map<Integer, Level> levels = new HashMap<>();

        Date startDate = new Date();

        List<Integer> buyIndex = new ArrayList<>();

        for (Bar bar : bars){
            buyIndex.clear();

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

            //log
            boolean end = bars.indexOf(bar) == bars.size()-1;

            if (!DateUtil.isSameDay(startDate, bar.getDate()) || end){
                startDate = bar.getDate();

                int gross = count*takeprofit*lot;
                int net = gross - (int) (count*lot*1.4);

                sum += gross;
                days++;

                String date = DateUtil.getDayString(end ? startDate : DateUtil.addDay(startDate, -1));

                String s = date + "," + delta + "," + shift + "," + takeprofit + "," + count +"," +  gross+ "," + net;

                fileWriter.append(s).append('\n');
                System.out.println(s);


                count = 0;
            }
        }

        System.out.println("d = " + delta + ", tp = " + takeprofit + ", avr = " + sum/days);
    }
}
