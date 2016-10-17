package ru.inheaven.aida.happy.trading.service;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AtomicDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inhell.aida.ssa.VSSABoost;

import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author inheaven on 20.06.2016.
 */
public class VSSAService {
    private final Logger log = LoggerFactory.getLogger(VSSAService.class);

    private Deque<Double> pricesExecute = new ConcurrentLinkedDeque<>();
    private Deque<Double> pricesFit = new ConcurrentLinkedDeque<>();

    private Deque<Trade> tradesBuffer = new ConcurrentLinkedDeque<>();

    private VSSABoost vssaBoost;

    private int window;
    private int N;

    private long execute;

    private AtomicBoolean loaded = new AtomicBoolean(false);

    public VSSAService(String symbol, OrderType orderType, double threshold, int vssaCount, int trainCount, int N, int M, int window, long execute) {
        this.N = N;
        this.window = window;
        this.execute = execute;

        vssaBoost = new VSSABoost(threshold, vssaCount, trainCount, N, M);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

                long start = (System.currentTimeMillis() - 100*1000*(N + M));
                long end = System.currentTimeMillis();

                Deque<Trade> trades = new LinkedList<>();

                for (long t = start; t < end; t += 1000){
                    List<Trade> list = tradeMapper.getLightTrades(symbol, orderType, new Date(t), new Date(t + 1000));

                    for (Trade trade : list){
                        trades.add(trade);
                    }
                }

                double[] prices = getPrices(trades);

                for (int i = prices.length - N; i < prices.length; ++i){
                    pricesExecute.add(prices[i]);
                }

                log.info("trades load " + trades.size());

                vssaBoost.fit(prices);

                loaded.set(true);
            } catch (Exception e) {
                log.error("error trades load", e);
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (loaded.get() && pricesExecute.size() >= N) {
                    double f = execute();

                    if (f != 0){
                        forecast.set(f);
                    }
                }
            } catch (Exception e) {
                log.error("error schedule execute", e);
            }
        }, 0, execute, TimeUnit.MILLISECONDS);
    }

    private AtomicDouble forecast = new AtomicDouble(0);

    public void add(Trade trade){
        try {
            tradesBuffer.add(trade);

            if (tradesBuffer.size() >= window){
                double[] prices = getPrices(tradesBuffer);

                for (double price : prices){
                    pricesFit.add(price);
                    pricesExecute.add(price);
                }

                tradesBuffer.clear();
            }

            if (pricesExecute.size() > N){
                pricesExecute.pollFirst();
            }

            if (pricesFit.size() > 100*N){
                pricesFit.pollFirst();
            }
        } catch (Exception e) {
            log.error("error add", e);
        }
    }

    private double[] getPrices(Deque<Trade> trades){
        List<Double> pricesD = new ArrayList<>();
        List<Trade> avg = new ArrayList<>();

        for (Iterator<Trade> it = trades.descendingIterator(); it.hasNext();){
            Trade t = it.next();

            avg.add(t);

            if (avg.size() >= window){
                double priceSum = 0;
                double volumeSum = 0;

                for (Trade trade : avg){
                    double tradeVolume = trade.getAmount().doubleValue();

                    priceSum += trade.getPrice().doubleValue()*tradeVolume;

                    volumeSum += tradeVolume;
                }

                if (priceSum > 0 && volumeSum > 0) {
                    pricesD.add(0, priceSum/volumeSum);
                }

                avg.clear();
            }
        }

        double[] prices = new double[pricesD.size()];

        for (int i = 0; i < prices.length; ++i){
            prices[i] = pricesD.get(i);
        }

        return prices;
    }

    public void fit(){
        if (loaded.get()) {
            vssaBoost.fit(Doubles.toArray(pricesFit));
        }
    }

    private AtomicBoolean executing = new AtomicBoolean(false);

    private double execute(){
        try {
            if (executing.get()){
                return 0;
            }

            executing.set(true);

            return loaded.get() ? vssaBoost.execute(Doubles.toArray(pricesExecute)) : 0;
        } catch (Exception e) {
            log.error("error execute", e);

            return 0;
        }finally {
            executing.set(false);
        }
    }

    public boolean isLoaded(){
        return loaded.get();
    }

    public double getForecast(){
        return forecast.get();
    }
}
