package ru.inheaven.aida.backtest;

import com.google.gson.internal.LinkedTreeMap;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inhell.stock.core.VSSA;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.DoubleStream;

import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 12.06.2016.
 */
public class LevelSSABacktest extends LevelBacktest<LevelSSABacktestParameters>{
    private VSSA vssa;

    protected LevelSSABacktest(double initialTotal, long cancelDelay, double cancelRange, int spreadSize,
                               double spreadFixed, double spreadDiv, double amountLot, int amountRange,
                               int balanceDelay, double balanceValue, long tradeDelay, long metricDelay,
                               boolean slip, int forecastSize, int rangeLength, int windowLength, int eigenfunctionsCount,
                               int predictionPointCount, OrderType orderType) {
        super(initialTotal, cancelDelay, cancelRange, spreadSize, spreadFixed, spreadDiv, amountLot, amountRange,
                balanceDelay, balanceValue, tradeDelay, metricDelay, slip, forecastSize,
                new LevelSSABacktestParameters(rangeLength, windowLength, eigenfunctionsCount, predictionPointCount), orderType);

        vssa = new VSSA(rangeLength, windowLength, eigenfunctionsCount, predictionPointCount);
    }

    @SuppressWarnings("Duplicates")
    public static void main(String... args){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 13, 6, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 13, 7, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Map<Double, String> results = new LinkedTreeMap<>(Comparator.reverseOrder());

        for (int i3 = 0; i3 <= 0; i3 += 0){
            for (int i2 = 0; i2 <= 0; i2 += 0) {
                for (int i1 = 0; i1 <= 0; i1 += 0) {
                    for (int i0 = 1; i0 <= 100; i0 += 1) {
                        LevelSSABacktest bid = new LevelSSABacktest(10800, 60000, 20, 5000, 0, Math.sqrt(2*Math.PI), 0.01 + 0.001*i0, 0, 1000, 1, 150, 300000, false, 256, 256, 128, 8, 8, BID);

                        Future bidFuture = executorService.submit(() -> {
                            trades.forEach(bid::action);
                            bid.vssa.clear();

                            results.put(bid.total, bid.getStringRow());
                            System.out.println(bid.getStringRow());

                        });

                        LevelSSABacktest ask = new LevelSSABacktest(10800, 60000, 20, 5000, 0, Math.sqrt(2*Math.PI), 0.01 + 0.001*i0, 0, 1000, 1, 150, 300000, false, 256, 256, 128, 8, 8, ASK);

                        Future askFuture = executorService.submit(() -> {
                            trades.forEach(ask::action);
                            ask.vssa.clear();

                            results.put(ask.total, ask.getStringRow());
                            System.out.println(ask.getStringRow());
                        });

                        try {
                            bidFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }

                        try {
                            askFuture.get();
                        } catch (InterruptedException | ExecutionException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }

        System.out.println(results);
    }

    @Override
    protected double getForecast(double[] prices, LevelSSABacktestParameters parameters) {
        if (prices.length >= parameters.getRangeLength()) {
            try {
                prices = DoubleStream.of(prices).limit(parameters.getRangeLength()).toArray();

                return vssa.execute(prices)[parameters.getPredictionPointCount() - 1];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return prices[prices.length - 1];
    }
}
