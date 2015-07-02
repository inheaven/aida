package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Strategy;
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
    }

    public Observable<Order> createOrderObserver(Strategy strategy){
        Account account = accountMapper.getAccount(strategy.getAccountId());
        okcoinService.realTrades(account.getApiKey(), account.getSecretKey());

        return orderObservable
                .filter(o -> Objects.equals(strategy.getExchangeType(), o.getExchangeType()))
                .filter(o -> Objects.equals(strategy.getSymbol(), o.getSymbol()))
                .filter(o -> Objects.equals(strategy.getSymbolType(), o.getSymbolType()));
    }

    public void createOrder(Order order){
        if (order.getExchangeType().equals(ExchangeType.OKCOIN_FUTURES)){
            okcoinService.createOrder(order);
        }
    }
}
