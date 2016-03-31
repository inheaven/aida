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
    public TradeService(OkcoinCnFixService okcoinCnFixService,
                        TradeMapper tradeMapper, BroadcastService broadcastService) {
        tradeObservable = okcoinCnFixService.getTradeObservable()
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        tradeObservable.connect();

        tradeObservable.subscribe(tradeMapper::asyncSave);

        tradeObservable.subscribe(t -> broadcastService.broadcast(getClass(), "trade", t));

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            //stdDevMap.put("BTC/CNY_3", tradeMapper.getTradeStdDevPt("BTC/CNY", 11000));
            stdDevMap.put("BTC/CNY_2", tradeMapper.getTradeStdDevPt("BTC/CNY", 10000));
            //stdDevMap.put("BTC/CNY_1", tradeMapper.getTradeStdDevPt("BTC/CNY", 9000));
            //stdDevMap.put("BTC/CNY_0", tradeMapper.getTradeStdDevPt("BTC/CNY", 8000));

            stdDevMap.put("BTC/CNY", stdDevMap.get("BTC/CNY_2"));
        }, 0, 4, TimeUnit.SECONDS);
    }

    public ConnectableObservable<Trade> getTradeObservable() {
        return tradeObservable;
    }

    public BigDecimal getStdDev(String symbol){
        return stdDevMap.get(symbol);
    }
}
