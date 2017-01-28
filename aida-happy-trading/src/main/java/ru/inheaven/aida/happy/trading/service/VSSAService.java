package ru.inheaven.aida.happy.trading.service;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AtomicDouble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inhell.aida.ssa.VSSABoost;

import java.security.SecureRandom;
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

    private Deque<Trade> trades = new ConcurrentLinkedDeque<>();

    private VSSABoost vssaBoost;

    private int window;
    private int N;

    private int vssaCount;

    private AtomicBoolean loaded = new AtomicBoolean(false);

    private final static int MAX_TRADES_COUNT = 12*60*60*100;

    public VSSAService(String symbol, OrderType orderType, double threshold, int vssaCount, int trainCount, int N, int M, int window, long execute) {
        this.N = N;
        this.window = window;
        this.vssaCount = vssaCount;

        vssaBoost = new VSSABoost(threshold, vssaCount, trainCount, N, M);

        Executors.newSingleThreadExecutor().submit(() -> {
            try {
                TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

                long start = System.currentTimeMillis() - 24*60*60*1000;
                long end = System.currentTimeMillis();

                for (long t = start; t < end; t += 60000){
                    List<Trade> list = tradeMapper.getLightTrades(symbol, orderType, new Date(t), new Date(t + 60000));

                    for (Trade trade : list){
                        trades.add(trade);
                    }
                }

                log.info("trades load " + trades.size());

                List<Double> prices = getPrices(trades, (int) (2*Math.PI*N));

                if (prices.size() >= N) {
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

                    for (int i = 0; i < size - MAX_TRADES_COUNT; ++i){
                        trades.pollFirst();
                    }
                }
            } catch (Exception e) {
                log.error("error schedule execute", e);
            }
        }, 0, execute, TimeUnit.MILLISECONDS);
    }

    private AtomicDouble forecast = new AtomicDouble(0);

    public void add(Trade trade){
        trades.add(trade);
    }

    private Random random = new SecureRandom("vssa_service".getBytes());

    private List<Double> getPrices(Deque<Trade> trades, int limit){
        List<Double> prices = new ArrayList<>();
        List<Trade> buf = new ArrayList<>();

        for (Iterator<Trade> it = trades.descendingIterator(); it.hasNext();){
            Trade t = it.next();

            buf.add(t);

            if (buf.size() >= window){
                prices.add(buf.get(random.nextInt(buf.size())).getPrice().doubleValue());

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

    public void fit(){
        if (loaded.get()) {
            vssaBoost.fit(new ArrayList<>(getPrices(trades, (int) (2*Math.PI*N))));
        }
    }

    private AtomicBoolean executing = new AtomicBoolean(false);

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
