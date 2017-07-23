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
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ZERO;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CLOSED;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.OPEN;
import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 29.06.2015 23:49.
 */
@Singleton
public class OrderService {
    private ConnectableObservable<Order> orderObservable;
    private final Logger log = LoggerFactory.getLogger(getClass());

    private FixService fixService;
    private XChangeService xChangeService;
    private BroadcastService broadcastService;

    private PublishSubject<Order> localClosedOrderPublishSubject = PublishSubject.create();
    private ConnectableObservable<Order> localClosedOrderObservable;

    @Inject
    public OrderService(FixService fixService,XChangeService xChangeService, AccountMapper accountMapper,
                        BroadcastService broadcastService, InfluxService influxService) {

        this.fixService = fixService;
        this.xChangeService = xChangeService;
        this.broadcastService = broadcastService;

        orderObservable = fixService.getOrderObservable()
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .publish();
        orderObservable.connect();

        localClosedOrderObservable = localClosedOrderPublishSubject
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        localClosedOrderObservable.connect();

        //order metric
        orderObservable
                .filter(o -> o.getSymbol().equals("BTC/USD"))
                .buffer(1, TimeUnit.SECONDS)
                .subscribe(orders -> {
                    try {
                        BigDecimal openAskPrice = ZERO;
                        BigDecimal openAskVolume = ZERO;
                        Integer openAskCount = 0;

                        BigDecimal openBidPrice = ZERO;
                        BigDecimal openBidVolume = ZERO;
                        Integer openBidCount = 0;

                        BigDecimal closedAskPrice = ZERO;
                        BigDecimal closedAskVolume = ZERO;
                        Integer closedAskCount = 0;

                        BigDecimal closedBidPrice = ZERO;
                        BigDecimal closedBidVolume = ZERO;
                        Integer closedBidCount = 0;

                        for (Order order : orders){
                            if (order.getStatus().equals(OPEN) && order.getPrice() != null){
                                if (order.getType().equals(ASK)){
                                    openAskCount++;
                                    openAskVolume = openAskVolume.add(order.getAmount());
                                    openAskPrice = openAskPrice.add(order.getPrice().multiply(order.getAmount()));
                                }else if (order.getType().equals(BID)){
                                    openBidCount++;
                                    openBidVolume = openBidVolume.add(order.getAmount());
                                    openBidPrice = openBidPrice.add(order.getPrice().multiply(order.getAmount()));
                                }
                            }else if (order.getStatus().equals(CLOSED)){
                                if (order.getType().equals(ASK)){
                                    closedAskCount++;
                                    closedAskVolume = closedAskVolume.add(order.getAmount());
                                    closedAskPrice = closedAskPrice.add(order.getAvgPrice().multiply(order.getAmount()));
                                }else if (order.getType().equals(BID)){
                                    closedBidCount++;
                                    closedBidVolume = closedBidVolume.add(order.getAmount());
                                    closedBidPrice = closedBidPrice.add(order.getAvgPrice().multiply(order.getAmount()));
                                }
                            }
                        }

                        openAskPrice = openAskVolume.compareTo(ZERO) > 0 ? openAskPrice.divide(openAskVolume, 8, RoundingMode.HALF_EVEN) : null;
                        openBidPrice = openBidVolume.compareTo(ZERO) > 0 ? openBidPrice.divide(openBidVolume, 8, RoundingMode.HALF_EVEN) : null;

                        closedAskPrice = closedAskVolume.compareTo(ZERO) > 0 ? closedAskPrice.divide(closedAskVolume, 8, RoundingMode.HALF_EVEN) : null;
                        closedBidPrice = closedBidVolume.compareTo(ZERO) > 0 ? closedBidPrice.divide(closedBidVolume, 8, RoundingMode.HALF_EVEN) : null;

                        influxService.addOrderMetric(1L, openAskPrice, openAskVolume, openAskCount,
                                openBidPrice, openBidVolume, openBidCount,
                                closedAskPrice, closedAskVolume, closedAskCount,
                                closedBidPrice, closedBidVolume, closedBidCount);
                    } catch (Exception e) {
                        log.error("error add order metric", e);
                    }
                });

    }

    public ConnectableObservable<Order> getOrderObservable() {
        return orderObservable;
    }

    public void createOrder(Account account, Order order) throws CreateOrderException {
        switch (order.getExchangeType()){
            case OKCOIN:
                if (order.getSymbolType() == null){
                    fixService.placeLimitOrder(account.getId(), order);
                }else{
                    xChangeService.placeLimitOrder(account, order);
                }

                break;
            case OKCOIN_CN:
                if (order.getSymbolType() == null){
                    fixService.placeLimitOrder(account.getId(), order);
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
                    fixService.orderInfo(account.getId(), order);
                }else{
                    //okcoinService.orderFutureInfo(account.getApiKey(), account.getSecretKey(), order);
                }

                break;
            case OKCOIN_CN:
                if (order.getSymbolType() == null){
                    fixService.orderInfo(account.getId(), order);
                }else{
//                    okcoinService.orderFutureInfo(account.getApiKey(), account.getSecretKey(), order);
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
                    fixService.cancelOrder(account.getId(), order);
                }else{
                    xChangeService.cancelOrder(account, order);
                }

                break;
            case OKCOIN_CN:
                if (order.getSymbolType() == null){
                    try {
                        fixService.cancelOrder(account.getId(), order);
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
