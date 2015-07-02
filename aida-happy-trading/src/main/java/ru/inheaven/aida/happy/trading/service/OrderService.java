package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import rx.Observable;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * @author inheaven on 29.06.2015 23:49.
 */
@Singleton
public class OrderService {
    private Observable<Order> orderObservable;

    private OkcoinService okcoinService;
    private AccountMapper accountMapper;

    @Inject
    public OrderService(AccountMapper accountMapper, OkcoinService okcoinService) {
        this.accountMapper = accountMapper;
        this.okcoinService = okcoinService;

        orderObservable = okcoinService.getOrderObservable()
                .mergeWith(okcoinService.getRealTradesObservable());

        accountMapper.getAccounts(ExchangeType.OKCOIN_FUTURES)
                .forEach(account -> okcoinService.realTrades(account.getApiKey(), account.getSecretKey()));
                //todo reconnect
    }

    public Observable<Order> createOrderObserver(ExchangeType exchangeType, String symbol, SymbolType symbolType){
        return orderObservable
                .filter(o -> Objects.equals(exchangeType, o.getExchangeType()))
                .filter(o -> Objects.equals(symbol, o.getSymbol()))
                .filter(o -> Objects.equals(symbolType, o.getSymbolType()));
    }
}
