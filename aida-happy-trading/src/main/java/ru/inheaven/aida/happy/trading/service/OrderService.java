package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.exception.OrderInfoException;
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
    private XChangeService xChangeService;
    private BroadcastService broadcastService;

    private PublishSubject<Order> localClosedOrderPublishSubject = PublishSubject.create();
    private ConnectableObservable<Order> localClosedOrderObservable;

    @Inject
    public OrderService(OkcoinService okcoinService, XChangeService xChangeService, BroadcastService broadcastService) {
        this.okcoinService = okcoinService;
        this.xChangeService = xChangeService;
        this.broadcastService = broadcastService;

        orderObservable = okcoinService.createFutureOrderObservable()
                .mergeWith(okcoinService.createSpotOrderObservable())
                .mergeWith(okcoinService.createFutureRealTrades())
                .mergeWith(okcoinService.createSpotRealTrades())
                .observeOn(Schedulers.io())
                .onBackpressureBuffer()
                .publish();
        orderObservable.connect();

        localClosedOrderObservable = localClosedOrderPublishSubject
                .observeOn(Schedulers.io())
                .onBackpressureBuffer()
                .publish();
        localClosedOrderObservable.connect();

        okcoinService.realFutureTrades("00dff9d7-7d99-45f9-bd41-23d08d4665ce", "41A8FBFE7CD7D079D7FD64B79D64BBE2");
        okcoinService.realSpotTrades("00dff9d7-7d99-45f9-bd41-23d08d4665ce", "41A8FBFE7CD7D079D7FD64B79D64BBE2");
    }

    public Observable<Order> createOrderObserver(Strategy strategy){
        return orderObservable
                .filter(o -> Objects.equals(strategy.getAccount().getExchangeType(), o.getExchangeType()))
                .filter(o -> Objects.equals(strategy.getSymbol(), o.getSymbol()))
                .filter(o -> Objects.equals(strategy.getSymbolType(), o.getSymbolType()));
    }

    public void createOrder(Account account, Order order) throws CreateOrderException {
        switch (order.getExchangeType()){
            case OKCOIN:
                xChangeService.placeLimitOrder(account, order);
                break;
        }
    }

    public void orderInfo(Strategy strategy, Order order){
        if (order.getExchangeType().equals(OKCOIN)){
            if (order.getSymbolType() != null){
                okcoinService.orderFutureInfo(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey(), order);
            }else {
                okcoinService.orderSpotInfo(strategy.getAccount().getApiKey(), strategy.getAccount().getSecretKey(), order);
            }
        }
    }

    public void checkOrder(Account account, Order order) throws OrderInfoException {
        switch (order.getExchangeType()){
            case OKCOIN:
                xChangeService.checkOrder(account, order);
                break;
        }
    }

    public void onCreateOrder(Order order){
        String key = "";

        switch (order.getSymbol()){
            case "BTC/USD":
                key = "btc";
                break;
            case "LTC/USD":
                key = "ltc";
                break;
        }

        String message = "(" + order.getPrice().setScale(3, HALF_UP) +
                (OrderType.BUY_SET.contains(order.getType()) ? "↑":"↓") + ") ";

        Module.getInjector().getInstance(BroadcastService.class).broadcast(getClass(), "create_order_" +
                key + "_" + Objects.toString(order.getSymbolType(), ""), message);
    }

    public void onCloseOrder(Order order){
        localClosedOrderPublishSubject.onNext(order);

        if (order.getAvgPrice() != null) {
            String message = "[" + order.getAvgPrice().setScale(3, HALF_UP)
                    + (OrderType.BUY_SET.contains(order.getType()) ? "↑" : "↓") + "] ";

            broadcastService.broadcast(getClass(), "close_order_"
                    + order.getSymbol() + "_" + Objects.toString(order.getSymbolType(), ""), message);
        }
    }

    public Observable<Order> getClosedOrderObservable(){
        return localClosedOrderObservable;
    }
}
