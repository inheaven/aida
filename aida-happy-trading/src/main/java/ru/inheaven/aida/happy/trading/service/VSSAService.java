package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inhell.aida.ssa.VSSABoost;

import java.util.ArrayList;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author inheaven on 20.06.2016.
 */
public class VSSAService {
    private final static int MAX_TRADES = 1000000;

    private final Logger log = LoggerFactory.getLogger(VSSAService.class);

    private Deque<Trade> trades = new ConcurrentLinkedDeque<>();

    private VSSABoost vssaBoost;

    private int delay;

    public VSSAService(String symbol, OrderType orderType, double threshold, int vssaCount, int trainCount, int N, int M, int delay) {
        this.delay = delay;

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        trades.addAll(tradeMapper.getLightTrades(symbol, orderType, new Date(System.currentTimeMillis() - N*delay), new Date()));

        vssaBoost = new VSSABoost(threshold, vssaCount, trainCount, N, M);
    }

    public void add(Trade trade){
        trades.add(trade);

        if (trades.size() > MAX_TRADES){
            trades.removeFirst();
        }
    }

    private double[] getPrices(){
        List<Double> pricesD = new ArrayList<>();
        List<Trade> avg = new ArrayList<>();

        long last = trades.peekFirst().getCreated().getTime();

        for (Trade t : trades){
            avg.add(t);

            if (t.getCreated().getTime() - last > delay){
                double avgAsk = avg.stream().mapToDouble(s -> s.getPrice().doubleValue()).average().orElse(0);

                pricesD.add(avgAsk);

                last = t.getCreated().getTime();
                avg.clear();
            }
        }

        return pricesD.stream().mapToDouble(d -> d).toArray();
    }

    public void fit(){
        vssaBoost.fit(getPrices());
    }

    public double execute(){
        return vssaBoost.execute(getPrices());
    }
}
