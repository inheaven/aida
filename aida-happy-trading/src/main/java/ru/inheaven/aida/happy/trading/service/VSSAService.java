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

    private int vssaCount;

    private AtomicBoolean loaded = new AtomicBoolean(false);

    public VSSAService(String symbol, OrderType orderType, double threshold, int vssaCount, int trainCount, int N, int M, int window, long execute) {
        this.N = N;
        this.window = window;
        this.execute = execute;
        this.vssaCount = vssaCount;

        vssaBoost = new VSSABoost(threshold, vssaCount, trainCount, N, M);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

                long start = System.currentTimeMillis() - 12*60*60*1000;
                long end = System.currentTimeMillis();

                Deque<Trade> trades = new LinkedList<>();

                for (long t = start; t < end; t += 60000){
                    List<Trade> list = tradeMapper.getLightTrades(symbol, orderType, new Date(t), new Date(t + 60000));

                    for (Trade trade : list){
                        trades.add(trade);
                    }
                }

                log.info("trades load " + trades.size());

                List<Double> prices = getPrices(trades);

                for (int i = prices.size() - N; i < prices.size(); ++i){
                    pricesExecute.add(prices.get(i));
                    pricesFit.add(prices.get(i));
                }

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
                List<Double> prices = getPrices(tradesBuffer);

                for (double price : prices){
                    pricesFit.add(price);
                    pricesExecute.add(price);
                }

                tradesBuffer.clear();

                for (int i = 0; i < pricesExecute.size() - N; ++i){
                    pricesExecute.pollFirst();
                }

                for (int i = 0; i < pricesFit.size() - 3*N; ++i){
                    pricesFit.pollFirst();
                }
            }
        } catch (Exception e) {
            log.error("error add", e);
        }
    }

    private List<Double> getPrices(Deque<Trade> trades){
        List<Double> prices = new ArrayList<>();
        List<Trade> buf = new ArrayList<>();

        for (Iterator<Trade> it = trades.descendingIterator(); it.hasNext();){
            Trade t = it.next();

            buf.add(t);

            if (buf.size() >= window){
                double priceSum = 0;
                double volumeSum = 0;

                for (Trade trade : buf){
                    priceSum += trade.getPrice().doubleValue();
                    volumeSum++;
                }

                prices.add(0, priceSum/volumeSum);

                buf.clear();
            }
        }

        buf.clear();

        return prices;
    }

    public void fit(){
        if (loaded.get()) {
            vssaBoost.fit(new ArrayList<>(pricesFit));
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

    public int getVssaCount() {
        return vssaCount;
    }
}
