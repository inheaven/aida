package ru.inheaven.aida.okex.backtest;

import com.google.common.collect.Lists;
import ru.inheaven.aida.okex.mapper.TradeMapper;
import ru.inheaven.aida.okex.model.Trade;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Anatoly A. Ivanov
 * 30.09.2017 19:56
 */
@Singleton
public class BacktestService {
    @Inject
    private TradeMapper tradeMapper;

    public static class Level {
       double spread;
       int sum;
       int count;
       double volume;
       double profit;
       double risk;

        public double getSpread() {
            return spread;
        }

        @Override
        public String toString() {
            return spread + " " + sum + " " + profit + " " + risk + " " + (profit/Math.abs(risk));
        }
    }

    @Inject
    public void test() {
        test0();
    }

    private void test0(){
        System.out.println("Magic Number Search");
//        scheduleLevelSpreadSum(tradeMapper, "this_week");
        scheduleLevelSpreadSum(tradeMapper, "quarter");
    }

    private void test1(){
        List<Trade> trades = Lists.reverse(tradeMapper.getLastIntervalTrades("quarter", "btc_usd", "7 DAY"));

        IntStream.range(1, 1000).forEach(i -> {
            System.out.println(actionCount(trades, (double) 5 + i*0.01));
        });
    }

    public void test2(){
        List<Trade> trades = Lists.reverse(tradeMapper.getLastIntervalTrades("quarter", "btc_usd", "6 DAY"));

        List<Level> levels = new ArrayList<>();

        IntStream.rangeClosed(100, 1000)
                .filter(i -> IntStream.rangeClosed(2, (int)Math.sqrt(i)).allMatch(j -> i%j != 0))
                .forEach(p -> levels.add(actionCount(trades, (double) p/100)));

        levels.forEach(l -> System.out.println(l.spread + " " + (l.risk - l.profit)));
    }

    private void scheduleLevelSpreadSum(TradeMapper tradeMapper, String symbol) {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                List<Level> levels = new ArrayList<>();

                List<Trade> trades = tradeMapper.getLastIntervalTrades(symbol, "btc_usd", "30 DAY");

                IntStream.rangeClosed(1000, 10000)
                        .filter(i -> IntStream.rangeClosed(2, (int)Math.sqrt(i)).allMatch(j -> i%j != 0))
                        .forEach(p -> levels.add(actionCount(trades, (double) p/100)));

                levels.sort(Comparator.comparing(l -> l.spread, Comparator.naturalOrder()));

                levels.forEach(System.out::println);

//                System.out.println(new Date() + " " + symbol.substring(0, 4) + " " + levels.subList(0, 10));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }, 0, 1, TimeUnit.HOURS);
    }

    private void test3(){
        List<Trade> trades = Lists.reverse(tradeMapper.getLastTrades("quarter", "btc_usd", 100000));

        List<Level> levels = new ArrayList<>();

        IntStream.range(1, 5000).forEach(i -> levels.add(actionCount(trades, (double) 5 + i*0.01)));

        levels.forEach(l -> System.out.println(l.spread + " " + l.profit));
    }

    public List<Level> getLevels(List<Double> prices, int minSpread, int maxSpread){
        List<Level> levels = new ArrayList<>();

        IntStream.rangeClosed(minSpread*100, maxSpread*100)
                .filter(i -> IntStream.rangeClosed(2, (int)Math.sqrt(i)).allMatch(j -> i%j != 0))
                .forEach(p -> levels.add(actionCountPrices(prices, (double) p/100)));

        levels.sort(Comparator.comparing(l -> l.profit/Math.abs(l.risk), Comparator.reverseOrder()));

        return levels;
    }

    public Level actionCount(List<Trade> trades, double spread) {
        return actionCountPrices(trades.stream().map(t -> t.getPrice().doubleValue()).collect(Collectors.toList()), spread);
    }

    public Level actionCountPrices(List<Double> prices, double spread) {
        int count = 0;
        double lastAction = 0;

        Map<Long, Integer> levels = new HashMap<>();

        for (Double price : prices){
            if (Math.abs(price - lastAction) > spread){
                count++;
                lastAction = price;

                levels.putIfAbsent(Math.round(price/spread), 0);
                levels.computeIfPresent(Math.round(price/spread), (k, v) -> ++v);
            }
        }

        Level level = new Level();
        level.spread = spread;
        level.count = count;
        level.sum = levels.values().stream().mapToInt(l -> l).sum();
        level.volume = level.sum* level.spread;
        level.profit = levels.entrySet().stream()
                .mapToDouble(e -> {
                    double p = e.getKey()*spread;

                    return (100/p - 100/(p+spread) - 0.03/p)*e.getValue()/2;
                })
                .sum();

        double pMax = prices.stream().mapToDouble(d -> d).max().getAsDouble();
        double pMin = prices.stream().mapToDouble(d -> d).min().getAsDouble();
        double delta = pMax - pMin;
        long qMax = Math.round(delta/spread);
        level.risk = (100/pMax - 100/(pMax-delta/2))*qMax;

        return level;
    }
}
