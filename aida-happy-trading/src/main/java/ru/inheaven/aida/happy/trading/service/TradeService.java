package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.Trade;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 002 02.07.15 16:45
 */
@Singleton
public class TradeService {
    private ConnectableObservable<Trade> tradeObservable;

    @Inject
    public TradeService(OkcoinService okcoinService, BroadcastService broadcastService) {
        tradeObservable = okcoinService.createFutureTradeObservable()
                .mergeWith(okcoinService.createSpotTradeObservable())
                .observeOn(Schedulers.io())
                .onBackpressureBuffer()
                .publish();
        tradeObservable.connect();

        tradeObservable.subscribe(t ->{
            String key = "";

            switch (t.getSymbol()){
                case "BTC/USD":
                    key = "btc";
                    break;
                case "LTC/USD":
                    key = "ltc";
                    break;
            }

            broadcastService.broadcast(getClass(), "trade_" + key + "_" + Objects.toString(t.getSymbolType(), ""),
                    t.getPrice().setScale(3, HALF_UP));
        });
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
