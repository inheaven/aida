package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * @author inheaven on 002 02.07.15 16:45
 */
@Singleton
public class TradeService {
    private ConnectableObservable<Trade> tradeObservable;

    @Inject
    public TradeService(OkcoinService okcoinService, TradeMapper tradeMapper) {
        tradeObservable = okcoinService.createFutureTradeObservable()
                .mergeWith(okcoinService.createSpotTradeObservable())
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        tradeObservable.connect();

        tradeObservable.subscribe(tradeMapper::asyncSave);
    }

    public Observable<Trade> createTradeObserver(Strategy strategy){
        return tradeObservable
                .filter(t -> Objects.equals(strategy.getAccount().getExchangeType(), t.getExchangeType()))
                .filter(t -> Objects.equals(strategy.getSymbol(), t.getSymbol()))
                .filter(t -> Objects.equals(strategy.getSymbolType(), t.getSymbolType()));
    }

    public Observable<Trade> getTradeObservable() {
        return tradeObservable;
    }
}
