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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author inheaven on 20.06.2016.
 */
public class VSSAService {
    private final Logger log = LoggerFactory.getLogger(VSSAService.class);

    private Deque<Double> prices = new ConcurrentLinkedDeque<>();

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

                long start = (System.currentTimeMillis() - 10*1000*(N + M));
                long end = System.currentTimeMillis();

                Deque<Trade> trades = new LinkedList<>();

                for (long t = start; t < end; t += 1000){
                    trades.addAll(tradeMapper.getLightTrades(symbol, orderType, new Date(t), new Date(t + 1000)));
                }

                double[] prices = getPrices(trades);

                for (double price : prices){
                    this.prices.add(price);
                }

                log.info("trades load " + trades.size());

                vssaBoost.fit(prices);

                loaded.set(true);
            } catch (Exception e) {
                log.error("error trades load", e);
            }
        });
    }

    private AtomicLong index = new AtomicLong(0);

    private AtomicDouble forecast = new AtomicDouble(0);

    private AtomicLong lastExecute = new AtomicLong(System.currentTimeMillis());

    private Semaphore semaphore = new Semaphore(1);

    private Executor executor = Executors.newWorkStealingPool();

    public void add(Trade trade){
        try {
            //lock
            semaphore.acquire();

            tradesBuffer.add(trade);

            if (tradesBuffer.size() >= window){
                double[] prices = getPrices(tradesBuffer);

                for (double price : prices){
                    this.prices.add(price);
                }

                tradesBuffer.clear();

                if (loaded.get() && System.currentTimeMillis() - lastExecute.get() > execute && this.prices.size() > N) {
                    lastExecute.set(System.currentTimeMillis());

                    executor.execute(() -> {
                        double f = execute();

                        if (f != 0){
                            forecast.set(f);
                        }
                    });
                }
            }

            if (prices.size() > 10*N){
                prices.removeFirst();
            }

            //release
            semaphore.release();
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
                    double tradeVolume = 1; //trade.getAmount().doubleValue();

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
            vssaBoost.fit(Doubles.toArray(prices));
        }
    }

    private AtomicBoolean executing = new AtomicBoolean(false);

    private double execute(){
        try {
            if (executing.get()){
                return 0;
            }

            executing.set(true);

            return loaded.get() ? vssaBoost.execute(Doubles.toArray(prices)) : 0;
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

    public static void main(String[] args){
        ConcurrentLinkedDeque<Double> test = new ConcurrentLinkedDeque<>();

        test.add(1.0);
        test.add(2.0);
        test.add(3.0);

        System.out.println(test);
        System.out.println(test.getFirst());
        System.out.println(test.getLast());

        for (Iterator<Double> it = test.descendingIterator(); it.hasNext();){
            System.out.println(it.next());
        }

    }
}
