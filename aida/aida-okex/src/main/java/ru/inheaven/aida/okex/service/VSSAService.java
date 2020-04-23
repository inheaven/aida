package ru.inheaven.aida.okex.service;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AtomicDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.model.Trade;
import ru.inheaven.aida.ssa.VSSABoost;

import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.lang.Math.PI;

/**
 * @author inheaven on 20.06.2016.
 */
public class VSSAService {
    private final Logger log = LoggerFactory.getLogger(VSSAService.class);

    private Deque<Trade> trades = new ConcurrentLinkedDeque<>();

    private VSSABoost vssaBoost;

    private int window;
    private int N;
    private int vssaCount;
    private int pricesFitCount;

    private AtomicBoolean loaded = new AtomicBoolean(false);

    private Random random = new SecureRandom();

    public VSSAService(List<Trade> initTrades, double threshold, int vssaCount, int trainCount, int N, int M, int window, long executeMillis, int fitMinutes) {
        this.N = N;
        this.window = window;
        this.vssaCount = vssaCount;

        this.pricesFitCount = (int) (2*PI*N);

        vssaBoost = new VSSABoost(threshold, vssaCount, trainCount, N, M);

        log.info(Thread.currentThread().getName() + ": init VSSAService ");

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                trades.addAll(initTrades);

                log.info("trades load " + initTrades.size());

                List<Double> prices = getPrices(trades, pricesFitCount);

                if (prices.size() >= 2*PI*N) {
                    vssaBoost.fit(prices);
                }

                loaded.set(true);
            } catch (Exception e) {
                log.error("error trades load", e);
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (loaded.get()) {
                    double f = execute();

                    if (f != 0){
                        forecast.set(f);
                    }

                    int size = trades.size();

                    for (int i = 0; i < size - pricesFitCount*window; ++i){
                        trades.pollFirst();
                    }
                }
            } catch (Exception e) {
                log.error("error schedule execute", e);
            }
        }, executeMillis, executeMillis, TimeUnit.MILLISECONDS);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (loaded.get()) {
                    vssaBoost.fit(new ArrayList<>(getPrices(trades, pricesFitCount)));

                    log.info("vssa fit {}", trades.peekLast().getPrice());
                }
            } catch (Exception e) {
                log.error("error schedule fit", e);
            }
        }, fitMinutes, fitMinutes, TimeUnit.MINUTES);
    }

    private AtomicDouble forecast = new AtomicDouble(0);

    public void add(Trade trade){
        trades.add(trade);
    }

    private List<Double> getPrices(Deque<Trade> trades, int limit){
        List<Double> prices = new ArrayList<>();
        List<Trade> buf = new ArrayList<>();

        for (Iterator<Trade> it = trades.descendingIterator(); it.hasNext();){
            Trade t = it.next();

            buf.add(t);

            if (buf.size() >= window){
                //noinspection ConstantConditions
                prices.add(buf.get(random.nextInt(window-1)).getPrice().doubleValue());

                buf.clear();

                if (prices.size() > limit){
                    break;
                }
            }
        }

        buf.clear();

        Collections.reverse(prices);

        return prices;
    }

    private AtomicBoolean executing = new AtomicBoolean(false);

    @SuppressWarnings("Duplicates")
    private double execute(){
        try {
            if (executing.get()){
                return 0;
            }

            executing.set(true);

            return loaded.get() ? vssaBoost.execute(Doubles.toArray(getPrices(trades, N))) : 0;
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
