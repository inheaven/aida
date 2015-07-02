package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Strategy;
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

    @Inject
    public OrderService(OkcoinService okcoinService) {
        this.okcoinService = okcoinService;

        orderObservable = okcoinService.getOrderObservable()
                .mergeWith(okcoinService.getRealTradesObservable());
    }

    public Observable<Order> createOrderObserver(Strategy strategy){
        okcoinService.realTrades(strategy.getApiKey(), strategy.getSecretKey());

        return orderObservable
                .filter(o -> Objects.equals(strategy.getExchangeType(), o.getExchangeType()))
                .filter(o -> Objects.equals(strategy.getSymbol(), o.getSymbol()));
    }

    public void createOrder(Order order){
        if (order.getExchangeType().equals(ExchangeType.OKCOIN_FUTURES)){
            okcoinService.createOrder(order);
        }
    }

    public void orderInfo(Strategy strategy, Order order){
        if (order.getExchangeType().equals(ExchangeType.OKCOIN_FUTURES)){
            okcoinService.orderInfo(strategy.getApiKey(), strategy.getSecretKey(), order);
        }
    }
}
