package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.SymbolType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import rx.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * @author inheaven on 002 02.07.15 16:45
 */
@Singleton
public class TradeService {
    private Observable<Trade> tradeObservable;

    @Inject
    public TradeService(OkcoinService okcoinService) {
        tradeObservable = okcoinService.getTradeObservable();
    }

    public Observable<Trade> createTradeObserver(ExchangeType exchangeType, String symbol, SymbolType symbolType){
        return tradeObservable
                .filter(t -> Objects.equals(exchangeType, t.getExchangeType()))
                .filter(t -> Objects.equals(symbol, t.getSymbol()))
                .filter(t -> Objects.equals(symbolType, t.getSymbolType()));
    }
}
