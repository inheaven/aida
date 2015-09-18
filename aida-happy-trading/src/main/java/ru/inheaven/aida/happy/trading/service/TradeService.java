package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author inheaven on 002 02.07.15 16:45
 */
@Singleton
public class TradeService {
    private ConnectableObservable<Trade> tradeObservable;

    @Inject
    public TradeService(OkcoinService okcoinService, OkcoinFixService okcoinFixService,
                        OkcoinCnFixService okcoinCnFixService,
                        TradeMapper tradeMapper, BroadcastService broadcastService) {
        tradeObservable = okcoinService.createFutureTradeObservable()
                .mergeWith(okcoinService.createSpotTradeObservable())
                .mergeWith(okcoinFixService.getTradeObservable())
                .mergeWith(okcoinCnFixService.getTradeObservable())
                .onBackpressureLatest()
                .observeOn(Schedulers.io())
                .publish();
        tradeObservable.connect();

        tradeObservable.subscribe(tradeMapper::asyncSave);

        tradeObservable.subscribe(t -> broadcastService.broadcast(getClass(), "trade", t));
    }

    public ConnectableObservable<Trade> getTradeObservable() {
        return tradeObservable;
    }
}
