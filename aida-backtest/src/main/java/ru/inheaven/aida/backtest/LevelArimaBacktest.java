package ru.inheaven.aida.backtest;

import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inhell.aida.algo.arima.ArimaFitter;
import ru.inhell.aida.algo.arima.ArimaProcess;
import ru.inhell.aida.algo.arima.DefaultArimaForecaster;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.DoubleStream;

import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;
import static ru.inhell.aida.algo.arima.ArimaFitter.fit;

/**
 * @author inheaven on 12.06.2016.
 */
public class LevelArimaBacktest extends LevelBacktest<LevelArimaBacktestParameters>{
    private LevelArimaBacktest(double initialTotal, long cancelDelay, double cancelRange, int spreadSize, double spreadFixed,
                               double spreadDiv, double amountLot, int amountRange, int balanceDelay, double balanceValue,
                               long tradeDelay, long metricDelay, boolean slip, int p, int d, int q, int arimaSize,
                               int arimaNext, int arimaFilter, ArimaProcess arimaProcess, double arimaCoef, OrderType orderType) {
        super(initialTotal, cancelDelay, cancelRange, spreadSize, spreadFixed, spreadDiv, amountLot, amountRange, balanceDelay,
                balanceValue, tradeDelay, metricDelay, slip, arimaSize, new LevelArimaBacktestParameters(p, d, q, arimaNext,
                        arimaFilter, arimaCoef, arimaProcess), orderType);

    }

    public static void main(String[] args){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 11, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 12, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        for (int i3 = 1; i3 <= 1; ++i3){
            for (int i2 = 1; i2 <= 10; ++i2) {
                for (int i1 = 10; i1 <= 20; ++i1) {
                    for (int i0 = 2; i0 <= 20; ++i0) {
                        LevelArimaBacktest bid = new LevelArimaBacktest(10100, 60000, 20, 5000, 0, Math.sqrt(2*Math.PI), 0.011, 0, 1000, 1, 150, 300000, false, i1, i2, i0, 10000, 1, 2, null, 1, BID);

                        executorService.submit(() -> {
                            trades.forEach(bid::action);

                            System.out.println(bid.getStringRow());

                        });

                        LevelArimaBacktest ask = new LevelArimaBacktest(10100, 60000, 20, 5000, 0, Math.sqrt(2*Math.PI), 0.011, 0, 1000, 1, 150, 300000, false, i1, i2, i0, 10000, 1, 2, null, 1, ASK);

                        executorService.submit(() -> {
                            trades.forEach(ask::action);

                            System.out.println(ask.getStringRow());
                        });
                    }
                }
            }
        }
    }


    private Deque<ArimaTest.Train> trains = new ConcurrentLinkedDeque<>();

    @Override
    protected double getForecast(double[] prices, LevelArimaBacktestParameters parameters) {
        double forecast;

        if (parameters.getArimaFilter() == 3){
            int trainSize = (int) (prices.length*2/Math.PI);
            int trainStart = prices.length - trainSize - 3;

            double[] train = new double[trainSize];

            for (int j = 0; j < trainSize; ++j){
                train[j] = prices[trainStart + j + 1]/prices[trainStart + j] - 1;
            }

            ArimaTest.Train t = new ArimaTest.Train();
            t.process = ArimaFitter.fit(train, parameters.getP(), parameters.getD(), parameters.getQ());

            double check = prices[prices.length - 1]/prices[prices.length - 2] - 1;
            forecast =  new DefaultArimaForecaster(t.process, train).next();
            t.check = check*forecast > 0 && Math.abs(forecast) > Math.abs(check);

            if (t.check) {
                trains.add(t);

                if (trains.size() > 11){
                    trains.removeFirst();
                }
            }

            double[] predictsDelta = new double[trainSize];
            trainStart = prices.length - trainSize - 2;
            for (int i = 0; i < trainSize; ++i){
                predictsDelta[i] = prices[trainStart + i + 1]/prices[trainStart + i] - 1;
            }

            trains.forEach(t1 -> t1.forecast = new DefaultArimaForecaster(t1.process, predictsDelta).next());

            double f = trains.parallelStream().mapToDouble(t1 -> t1.forecast).average().orElse(0);

            return prices[prices.length -1] * (f + 1);
        }else {
            if (parameters.getArimaFilter() == 1) {
                prices = DoubleStream.of(prices).map(Math::log).toArray();
            } else if (parameters.getArimaFilter() == 2) {
                double[] pricesDelta = new double[prices.length - 1];

                for (int i = 0; i < prices.length - 1; ++i) {
                    pricesDelta[i] = prices[i+1]/prices[i] - 1;
                }

                prices = pricesDelta;
            }

            forecast = new DefaultArimaForecaster(parameters.getArimaProcess() != null
                    ? parameters.getArimaProcess()
                    : fit(prices, parameters.getP(), parameters.getD(), parameters.getQ()), prices)
                    .next(parameters.getArimaNext())[parameters.getArimaNext() - 1];

            if (parameters.getArimaFilter() == 1){

                return Math.exp(forecast);
            }else if (parameters.getArimaFilter() == 2){

                return prices[prices.length - 1] * (forecast + 1);
            }
        }

        return super.getForecast(prices, parameters);
    }
}
