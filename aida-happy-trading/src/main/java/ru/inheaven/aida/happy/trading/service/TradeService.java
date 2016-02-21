package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author inheaven on 002 02.07.15 16:45
 */
@Singleton
public class TradeService {
    private ConnectableObservable<Trade> tradeObservable;

    private Map<String, BigDecimal> stdDevMap = new ConcurrentHashMap<>();

    @Inject
    public TradeService(OkcoinService okcoinService, OkcoinFixService okcoinFixService,
                        OkcoinCnFixService okcoinCnFixService,
                        TradeMapper tradeMapper, BroadcastService broadcastService) {
        tradeObservable = okcoinService.createFutureTradeObservable()
                .mergeWith(okcoinService.createSpotTradeObservable())
                .mergeWith(okcoinFixService.getTradeObservable())
                .mergeWith(okcoinCnFixService.getTradeObservable())
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        tradeObservable.connect();

        tradeObservable.subscribe(tradeMapper::asyncSave);

        tradeObservable.subscribe(t -> broadcastService.broadcast(getClass(), "trade", t));

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> { // 6765, 10946, 17711, 28657, 46368, 75025, 121393,
            stdDevMap.put("BTC/CNY_0", tradeMapper.getTradeStdDevPt("BTC/CNY", 10666)); //45
            stdDevMap.put("BTC/CNY_1", tradeMapper.getTradeStdDevPt("BTC/CNY", 23000)); //46
            stdDevMap.put("BTC/CNY_2", tradeMapper.getTradeStdDevPt("BTC/CNY", 29000)); //47
            stdDevMap.put("BTC/CNY_3", tradeMapper.getTradeStdDevPt("BTC/CNY", 19000)); //48
            stdDevMap.put("BTC/CNY_4", tradeMapper.getTradeStdDevPt("BTC/CNY", 23000)); //49
            stdDevMap.put("BTC/CNY_5", tradeMapper.getTradeStdDevPt("BTC/CNY", 29000)); //50

            stdDevMap.put("av_BTC/CNY_0", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 10666));
            stdDevMap.put("av_BTC/CNY_1", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 23000));
            stdDevMap.put("av_BTC/CNY_2", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 29000));
            stdDevMap.put("av_BTC/CNY_3", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 19000));
            stdDevMap.put("av_BTC/CNY_4", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 23000));
            stdDevMap.put("av_BTC/CNY_5", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 29000));

            stdDevMap.put("BTC/CNY", stdDevMap.get("BTC/CNY_0"));
            stdDevMap.put("av_BTC/CNY", stdDevMap.get("av_BTC/CNY_0"));
        }, 0, 1, TimeUnit.SECONDS);
    }

    public ConnectableObservable<Trade> getTradeObservable() {
        return tradeObservable;
    }

    public BigDecimal getStdDev(String symbol){
        return stdDevMap.get(symbol);
    }

    public BigDecimal getAvgAmount(String symbol){
        return stdDevMap.get("av_" + symbol);
    }
}
