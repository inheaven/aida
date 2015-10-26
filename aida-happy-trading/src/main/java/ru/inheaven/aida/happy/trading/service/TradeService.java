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
            stdDevMap.put("BTC/CNY", tradeMapper.getTradeStdDev("BTC/CNY", 10));
        }, 0, 1, TimeUnit.SECONDS);
    }

    public ConnectableObservable<Trade> getTradeObservable() {
        return tradeObservable;
    }

    public BigDecimal getStdDev(String symbol){
        return stdDevMap.get(symbol);
    }
}
