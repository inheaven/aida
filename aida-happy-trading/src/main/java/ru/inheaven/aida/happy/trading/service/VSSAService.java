package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inhell.aida.ssa.VSSABoost;

import java.util.*;
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

        trades.addAll(tradeMapper.getLightTrades(symbol, orderType, new Date(System.currentTimeMillis() - 2*N*delay), new Date()));

        log.info("trades load " + trades.size());

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

                    priceSum = trade.getPrice().doubleValue()*tradeVolume;

                    volumeSum += tradeVolume;
                }

                if (priceSum > 0 && volumeSum > 0) {
                    pricesD.add(0, priceSum/volumeSum);
                }

                time = t.getCreated().getTime();
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
