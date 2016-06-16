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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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

//        UJMPSettings ujmpSettings = UJMPSettings.getInstance();
//
//        ujmpSettings.put(USEJBLAS, false);
//        ujmpSettings.put(USEEJML, false);
//        ujmpSettings.put(USEOJALGO, true);
//        ujmpSettings.put(USEPARALLELCOLT, false);
//        ujmpSettings.put(USEMTJ, false);
//        ujmpSettings.put(USECOMMONSMATH, false);
//
//        ujmpSettings.setNumberOfThreads(2);

        vssa = new VSSA(rangeLength, windowLength, eigenfunctionsCount, predictionPointCount);
    }

    @SuppressWarnings("Duplicates")
    public static void main(String... args){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 17, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 17, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);

        System.out.println(trades.size());

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        Map<Double, String> results = new LinkedTreeMap<>(Comparator.reverseOrder());

        for (int i3 = 0; i3 <= 0; i3 += 1){
            for (int i2 = 8; i2 <= 8; i2 += 8) {
                for (int i1 = 2; i1 <= 10; i1 += 1) {
                    for (int i0 = 7; i0 <= 7; i0 += 1) {
                        int l = (int) Math.pow(2, i0);
                        int n = 2*l - 1;

                        LevelSSABacktest bid = new LevelSSABacktest(33400, 60000, 20, 5000, 0, Math.sqrt(2*Math.PI), 0.01983, 0, 1000, 1, 500, 300000, false, n+1, n, l, i1, l/8, BID);

                        Future bidFuture = executorService.submit(() -> {
                            trades.forEach(bid::action);
                            //bid.vssa.clear();

                            results.put(bid.total, bid.getStringRow());
                            System.out.println(bid.getStringRow());

                        });

                        LevelSSABacktest ask = new LevelSSABacktest(33400, 60000, 20, 5000, 0, Math.sqrt(2*Math.PI), 0.01983, 0, 1000, 1, 500, 300000, false, n+1, n, l, i1, l/8, ASK);

                        Future askFuture = executorService.submit(() -> {
                            trades.forEach(ask::action);
                            //ask.vssa.clear();

                            results.put(ask.total, ask.getStringRow());
                            System.out.println(ask.getStringRow());
                        });
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
//                float[] train = new float[parameters.getRangeLength()];
//
//                int start = prices.length - train.length;
//
//                for (int i = 0; i < train.length; ++i){
//                    train[i] = (float) prices[i + start];
//                }
//
//                float[] forecast = new float[parameters.getPredictionPointCount()];
//
//                int[] pp = new int[parameters.getEigenfunctionsCount()];
//
//                for (int i = 0; i < pp.length; i++){
//                    pp[i] = i;
//                }
//
//                ACML.jni().vssa(parameters.getRangeLength(), parameters.getWindowLength(), pp.length, pp,
//                        parameters.getPredictionPointCount(), train, forecast, 0);
//
//                return forecast[forecast.length - 1];

                double[] train = new double[parameters.getRangeLength()];

                for (int j = 0; j < train.length; ++j){
                    train[j] = prices[j + 1]/prices[j] - 1;
                }

                double f = vssa.execute(train)[parameters.getPredictionPointCount()-1];

                return prices[prices.length - 1] * (f + 1);
            }  catch (Exception e) {
                throw e;
            }
        }

        return prices[prices.length - 1];
    }
}
