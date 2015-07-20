package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
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
    public OrderService(OkcoinService okcoinService, XChangeService xChangeService, OrderMapper orderMapper,
                        BroadcastService broadcastService) {
        this.okcoinService = okcoinService;
        this.xChangeService = xChangeService;

        orderObservable = okcoinService.getOrderObservable()
                .mergeWith(okcoinService.getRealTradesObservable());

//        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
//            broadcastService.broadcast(getClass(), "all_order_rate", orderMapper.getAllOrderRate());
//        }, 0, 1, TimeUnit.MINUTES);
    }

    public Observable<Order> createOrderObserver(Strategy strategy){
        okcoinService.realTrades(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey());

        return orderObservable
                .filter(o -> Objects.equals(strategy.getAccount().getExchangeType(), o.getExchangeType()))
                .filter(o -> Objects.equals(strategy.getSymbol(), o.getSymbol()));
    }

    public void createOrder(Account account, Order order) throws CreateOrderException {
        if (order.getExchangeType().equals(ExchangeType.OKCOIN_FUTURES)){
            xChangeService.placeLimitOrder(account,order);
        }
    }

    public void orderInfo(Strategy strategy, Order order){
        if (order.getExchangeType().equals(ExchangeType.OKCOIN_FUTURES)){
            okcoinService.orderInfo(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey(), order);
        }
    }
}
