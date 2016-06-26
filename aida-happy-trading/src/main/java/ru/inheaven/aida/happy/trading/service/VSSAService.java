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

    private long delay;
    private int N;

    private long execute;

    private AtomicBoolean loaded = new AtomicBoolean(false);

    public VSSAService(String symbol, OrderType orderType, double threshold, int vssaCount, int trainCount, int N, int M, long delay, long execute) {
        this.delay = delay;
        this.N = N;

        this.execute = execute;

        vssaBoost = new VSSABoost(threshold, vssaCount, trainCount, N, M);

        Executors.newSingleThreadExecutor().submit(() -> {
            Thread.currentThread().setPriority(Thread.MIN_PRIORITY);

            try {
                TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

                long start = System.currentTimeMillis() - 2*delay*(N + M);
                long end = System.currentTimeMillis();

                Deque<Trade> trades = new LinkedList<>();

                for (long t = start; t < end; t += delay){
                    trades.addAll(tradeMapper.getLightTrades(symbol, orderType, new Date(t), new Date(t + delay)));
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

    private Executor executor = Executors.newSingleThreadExecutor();

    private AtomicLong index = new AtomicLong(0);

    private AtomicDouble forecast = new AtomicDouble(0);

    private AtomicLong lastExecute = new AtomicLong(System.currentTimeMillis());

    public void add(Trade trade){
        executor.execute(() -> {
            try {
                tradesBuffer.add(trade);

                if (tradesBuffer.getLast().getCreated().getTime() - tradesBuffer.getFirst().getCreated().getTime() > delay){
                    double[] prices = getPrices(tradesBuffer);

                    for (double price : prices){
                        this.prices.add(price);
                    }

                    tradesBuffer.clear();

                    if (loaded.get() && System.currentTimeMillis() - lastExecute.get() > execute) {
                        forecast.set(execute());

                        lastExecute.set(System.currentTimeMillis());
                    }
                }

                if (index.incrementAndGet() % 1000 == 0 && prices.size() > 3*N){
                    for (int i = 0; i < N; ++i){
                        prices.removeFirst();
                    }
                }
            } catch (Exception e) {
                log.error("error add", e);
            }
        });
    }

    private double[] getPrices(Deque<Trade> trades){
        List<Double> pricesD = new ArrayList<>();
        List<Trade> avg = new ArrayList<>();

        long time = 0;

        for (Iterator<Trade> it = trades.descendingIterator(); it.hasNext();){
            Trade t = it.next();

            if (time == 0){
                time = t.getCreated().getTime();
            }

            avg.add(t);

            if (time - t.getCreated().getTime() > delay){
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

                time = t.getCreated().getTime();
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

    public double execute(){
        try {
            return loaded.get() ? vssaBoost.execute(Doubles.toArray(prices)) : 0;
        } catch (Exception e) {
            log.error("error execute", e);

            return 0;
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
