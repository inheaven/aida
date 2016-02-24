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

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            stdDevMap.put("BTC/CNY_1", tradeMapper.getTradeStdDevPt("BTC/CNY", 9500)); //44
            stdDevMap.put("BTC/CNY_2", tradeMapper.getTradeStdDevPt("BTC/CNY", 10000)); //45
            stdDevMap.put("BTC/CNY_3", tradeMapper.getTradeStdDevPt("BTC/CNY", 10500)); //46
//            stdDevMap.put("BTC/CNY_4", tradeMapper.getTradeStdDevPt("BTC/CNY", 23000)); //47
//            stdDevMap.put("BTC/CNY_5", tradeMapper.getTradeStdDevPt("BTC/CNY", 29000)); //48
//            stdDevMap.put("BTC/CNY_6", tradeMapper.getTradeStdDevPt("BTC/CNY", 31000)); //49
//            stdDevMap.put("BTC/CNY_7", tradeMapper.getTradeStdDevPt("BTC/CNY", 37000)); //50

            stdDevMap.put("av_BTC/CNY_1", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 9500));
            stdDevMap.put("av_BTC/CNY_2", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 10000));
            stdDevMap.put("av_BTC/CNY_3", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 10500));
//            stdDevMap.put("av_BTC/CNY_4", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 23000));
//            stdDevMap.put("av_BTC/CNY_5", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 29000));
//            stdDevMap.put("av_BTC/CNY_6", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 31000));
//            stdDevMap.put("av_BTC/CNY_7", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 37000));

            stdDevMap.put("BTC/CNY", stdDevMap.get("BTC/CNY_1"));
            stdDevMap.put("av_BTC/CNY", stdDevMap.get("av_BTC/CNY_1"));
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
