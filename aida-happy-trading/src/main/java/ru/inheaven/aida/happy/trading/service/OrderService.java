package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.exception.OrderInfoException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN;

/**
 * @author inheaven on 29.06.2015 23:49.
 */
@Singleton
public class OrderService {
    private ConnectableObservable<Order> orderObservable;

    private OkcoinService okcoinService;
    private OkcoinFixService okcoinFixService;
    private XChangeService xChangeService;
    private BroadcastService broadcastService;
    private OrderMapper orderMapper;

    private PublishSubject<Order> localClosedOrderPublishSubject = PublishSubject.create();
    private ConnectableObservable<Order> localClosedOrderObservable;

    @Inject
    public OrderService(OkcoinService okcoinService, OkcoinFixService okcoinFixService, XChangeService xChangeService,
                        BroadcastService broadcastService, OrderMapper orderMapper) {
        this.okcoinService = okcoinService;
        this.okcoinFixService = okcoinFixService;
        this.xChangeService = xChangeService;
        this.broadcastService = broadcastService;
        this.orderMapper = orderMapper;

        orderObservable = okcoinService.createFutureOrderObservable()
                .mergeWith(okcoinService.createSpotOrderObservable())
                .mergeWith(okcoinService.createFutureRealTrades())
                .mergeWith(okcoinService.createSpotRealTrades())
                .mergeWith(okcoinFixService.getOrderObservable())
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        orderObservable.connect();

        localClosedOrderObservable = localClosedOrderPublishSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        localClosedOrderObservable.connect();

        okcoinService.realFutureTrades("832a335b-e627-49ca-b95d-bceafe6c3815", "8FAF74E300D67DCFA080A6425182C8B7");
    }

    public ConnectableObservable<Order> getOrderObservable() {
        return orderObservable;
    }

    public void createOrder(Account account, Order order) throws CreateOrderException {
        switch (order.getExchangeType()){
            case OKCOIN:
                if (order.getSymbolType() == null){
                    okcoinFixService.placeLimitOrder(account, order);
                }else{
                    xChangeService.placeLimitOrder(account, order);
                }

                break;
        }
    }

    public void orderInfo(Strategy strategy, Order order){
        if (order.getOrderId() != null){
            if (order.getExchangeType().equals(OKCOIN)){
                if (order.getSymbolType() != null){
                    okcoinService.orderFutureInfo(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey(), order);
                }else {
                    okcoinService.orderSpotInfo(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey(), order);
                }
            }
        }
    }

    public void checkOrder(Account account, Order order) throws OrderInfoException {
        if (order.getOrderId().contains("CREATED")){
            return;
        }

        switch (order.getExchangeType()){
            case OKCOIN:
                xChangeService.checkOrder(account, order);
                break;
        }
    }

    private long lastOnCloseOrder = System.currentTimeMillis();

    public void onCloseOrder(Order order){
        localClosedOrderPublishSubject.onNext(order);

        if (order.getAvgPrice() != null) {
            String message = "[" + order.getAvgPrice().setScale(3, HALF_UP)
                    + (OrderType.BUY_SET.contains(order.getType()) ? "↑" : "↓") + "] ";

            broadcastService.broadcast(getClass(), "close_order_"
                    + order.getSymbol() + "_" + Objects.toString(order.getSymbolType(), ""), message);
        }

        if (System.currentTimeMillis() - lastOnCloseOrder > 1000) {
            broadcastService.broadcast(getClass(), "trade_profit", orderMapper.getMinTradeProfit());

            lastOnCloseOrder = System.currentTimeMillis();
        }
    }

    public Observable<Order> getClosedOrderObservable(){
        return localClosedOrderObservable;
    }
}
