package ru.inheaven.aida.backtest;

import com.google.gson.internal.LinkedTreeMap;
import org.ujmp.core.util.UJMPSettings;
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

import static org.ujmp.core.util.UJMPSettings.*;
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

        UJMPSettings ujmpSettings = UJMPSettings.getInstance();

        ujmpSettings.put(USEJBLAS, false);
        ujmpSettings.put(USEEJML, false);
        ujmpSettings.put(USEOJALGO, true);
        ujmpSettings.put(USEPARALLELCOLT, false);
        ujmpSettings.put(USEMTJ, false);
        ujmpSettings.put(USECOMMONSMATH, false);

        ujmpSettings.setNumberOfThreads(2);
    }

    @SuppressWarnings("Duplicates")
    public static void main(String... args){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 15, 10, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 15, 12, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);

        System.out.println(trades.size());

        ExecutorService executorService = Executors.newFixedThreadPool(1);

        Map<Double, String> results = new LinkedTreeMap<>(Comparator.reverseOrder());

        for (int i3 = 0; i3 <= 0; i3 += 1){
            for (int i2 = 8; i2 <= 8; i2 += 8) {
                for (int i1 = 1; i1 <= 10; i1 += 1) {
                    for (int i0 = 8; i0 <= 8; i0 += 1) {
                        int l = (int) Math.pow(2, i0);
                        int n = 2*l - 1;

                        LevelSSABacktest bid = new LevelSSABacktest(33400, 60000, 20, 5000, 0, Math.sqrt(2*Math.PI), 0.01983, 0, 1000, 1, 500, 300000, false, n, n, l, i1, l/2, BID);

                        Future bidFuture = executorService.submit(() -> {
                            trades.forEach(bid::action);
                            //bid.vssa.clear();

                            results.put(bid.total, bid.getStringRow());
                            System.out.println(bid.getStringRow());

                        });

                        LevelSSABacktest ask = new LevelSSABacktest(33400, 60000, 20, 5000, 0, Math.sqrt(2*Math.PI), 0.01983, 0, 1000, 1, 500, 300000, false, n, n, l, i1, l/2, ASK);

                        Future askFuture = executorService.submit(() -> {
                            trades.forEach(ask::action);
                            //ask.vssa.clear();

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
                double[] train = new double[parameters.getRangeLength()];

                int start = prices.length - train.length;

                System.arraycopy(prices, start, train, 0, train.length);

                return vssa.execute(train)[parameters.getPredictionPointCount() - 1];
            }  catch (Exception e) {
                throw e;
            }
        }

        return prices[prices.length - 1];
    }
}
