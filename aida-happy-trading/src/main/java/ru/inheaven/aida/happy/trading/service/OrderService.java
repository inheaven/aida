package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.exception.OrderInfoException;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;
import rx.subjects.PublishSubject;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author inheaven on 29.06.2015 23:49.
 */
@Singleton
public class OrderService {
    private ConnectableObservable<Order> orderObservable;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private OkcoinService okcoinService;
    private BaseOkcoinFixService okcoinFixUsService;
    private BaseOkcoinFixService okcoinFixCnService;
    private XChangeService xChangeService;
    private BroadcastService broadcastService;

    private PublishSubject<Order> localClosedOrderPublishSubject = PublishSubject.create();
    private ConnectableObservable<Order> localClosedOrderObservable;

    @Inject
    public OrderService(OkcoinService okcoinService, OkcoinFixService okcoinFixService,
                        OkcoinCnFixService okcoinCnFixService, XChangeService xChangeService,
                        AccountMapper accountMapper, BroadcastService broadcastService) {
        this.okcoinService = okcoinService;
        this.okcoinFixUsService = okcoinFixService;
        this.okcoinFixCnService = okcoinCnFixService;
        this.xChangeService = xChangeService;
        this.broadcastService = broadcastService;


        orderObservable = okcoinCnFixService.getOrderObservable()
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        orderObservable.connect();

        localClosedOrderObservable = localClosedOrderPublishSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        localClosedOrderObservable.connect();

//        accountMapper.getAccounts(OKCOIN).forEach(a -> okcoinService.realFutureTrades(a.getApiKey(), a.getSecretKey()));
    }

    public ConnectableObservable<Order> getOrderObservable() {
        return orderObservable;
    }

    public void createOrder(Account account, Order order) throws CreateOrderException {
        switch (order.getExchangeType()){
            case OKCOIN:
                if (order.getSymbolType() == null){
                    okcoinFixUsService.placeLimitOrder(account, order);
                }else{
                    xChangeService.placeLimitOrder(account, order);
                }

                break;
            case OKCOIN_CN:
                if (order.getSymbolType() == null){
                    okcoinFixCnService.placeLimitOrder(account, order);
                }else{
                    xChangeService.placeLimitOrder(account, order);
                }

                break;
        }
    }

    public void orderInfo(Account account, Order order){
        switch (order.getExchangeType()){
            case OKCOIN:
                if (order.getSymbolType() == null){
                    okcoinFixUsService.orderInfo(order);
                }else{
                    okcoinService.orderFutureInfo(account.getApiKey(), account.getSecretKey(), order);
                }

                break;
            case OKCOIN_CN:
                if (order.getSymbolType() == null){
                    okcoinFixCnService.orderInfo(order);
                }else{
                    okcoinService.orderFutureInfo(account.getApiKey(), account.getSecretKey(), order);
                }

                break;
        }
    }

    public void checkOrder(Account account, Order order) throws OrderInfoException {
        switch (order.getExchangeType()){
            case OKCOIN:
            case OKCOIN_CN:
                xChangeService.checkOrder(account, order);
                break;
        }
    }

    public void cancelOrder(Account account, Order order) throws OrderInfoException {
        switch (account.getExchangeType()){
            case OKCOIN:
                if (order.getSymbolType() == null){
                    okcoinFixUsService.cancelOrder(account, order);
                }else{
                    xChangeService.cancelOrder(account, order);
                }

                break;
            case OKCOIN_CN:
                if (order.getSymbolType() == null){
                    try {
                        okcoinFixCnService.cancelOrder(account, order);
                    } catch (Exception e) {
                        log.error("cancel order error{}", order, e);
                    }
                }else{
                    xChangeService.cancelOrder(account, order);
                }

                break;
        }
    }

    public void onCloseOrder(Order order){
        localClosedOrderPublishSubject.onNext(order);
        broadcastService.broadcast(getClass(), "close", order);
    }

    public Observable<Order> getClosedOrderObservable(){
        return localClosedOrderObservable;
    }
}
