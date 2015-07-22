package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
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
    private XChangeService xChangeService;

    @Inject
    public OrderService(OkcoinService okcoinService, XChangeService xChangeService) {
        this.okcoinService = okcoinService;
        this.xChangeService = xChangeService;

        orderObservable = okcoinService.createFutureOrderObservable()
                .mergeWith(okcoinService.createSpotOrderObservable())
                .mergeWith(okcoinService.createFutureRealTrades())
                .mergeWith(okcoinService.createSpotRealTrades());
    }

    public Observable<Order> createOrderObserver(Strategy strategy){
        switch (strategy.getAccount().getExchangeType()){
            case OKCOIN_FUTURES:
                okcoinService.realFutureTrades(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey());
                break;
            case OKCOIN_SPOT:
                okcoinService.realSpotTrades(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey());
                break;
        }

        return orderObservable
                .filter(o -> Objects.equals(strategy.getAccount().getExchangeType(), o.getExchangeType()))
                .filter(o -> Objects.equals(strategy.getSymbol(), o.getSymbol()));
    }

    public void createOrder(Account account, Order order) throws CreateOrderException {
        switch (order.getExchangeType()){
            case OKCOIN_FUTURES:
            case OKCOIN_SPOT:
                xChangeService.placeLimitOrder(account, order);
                break;
        }
    }

    public void orderInfo(Strategy strategy, Order order){
        switch (order.getExchangeType()){
            case OKCOIN_FUTURES:
                okcoinService.orderFutureInfo(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey(), order);
                break;
            case OKCOIN_SPOT:
                okcoinService.orderSpotInfo(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey(), order);
                break;
        }
    }
}
