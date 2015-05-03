package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.Order;
import rx.subjects.PublishSubject;
import rx.subjects.Subject;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static ru.inheaven.aida.coin.entity.OrderStatus.CLOSED;
import static ru.inheaven.aida.coin.entity.OrderStatus.OPEN;
import static ru.inheaven.aida.coin.entity.OrderType.BUY;
import static ru.inheaven.aida.coin.entity.OrderType.SELL;

/**
 * @author inheaven on 12.02.2015 21:06.
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class OrderService extends AbstractService{
    @EJB
    private OrderBean orderBean;

    @EJB
    private XChangeService xChangeService;

    @EJB
    private TradeService tradeService;

    private PublishSubject<Order> orderSubject = PublishSubject.create();

    private Map<Long, Map<String, Order>> openOrderCache = new ConcurrentHashMap<>();

    @PostConstruct
    public void start(){
        //init cache
        orderBean.getOrders(OPEN).forEach(this::putOpenOrderCache);

        //update status
        orderSubject
                .filter(o -> !o.getStatus().equals(OPEN))
                .subscribe(o -> {
                    openOrderCache.forEach((i, m) -> {
                        Order order = m.remove(o.getOrderId());

                        if (order != null) {
                            order.setName(o.getName());
                            order.setCreated(o.getCreated());
                            o.setClosed(new Date());
                            order.setFilledAmount(o.getFilledAmount());
                            order.setAvgPrice(o.getAvgPrice());
                            order.setStatus(o.getStatus());

                            orderBean.save(order);
                        }
                    });
                });

        //close by trade price
        tradeService.getTradeSubject()
                .groupBy(t -> t.getExchangeType().name() + ":" + t.getSymbol())
                .map(o -> o.sample(1, TimeUnit.MINUTES))
                .subscribe(observable -> {
                    observable.subscribe(t -> {
                        openOrderCache.forEach((i, m) -> m.values().stream()
                                .filter(o -> o.getExchangeType() == t.getExchangeType())
                                .filter(o -> o.getSymbol().equals(t.getSymbol()))
                                .filter(o -> o.getPrice().compareTo(t.getPrice()) > 0 && BUY.contains(o.getType()))
                                .filter(o -> o.getPrice().compareTo(t.getPrice()) < 0 && SELL.contains(o.getType()))
                                .forEach(o -> {
                                    o.setStatus(CLOSED);

                                    orderSubject.onNext(o);
                                }));
                    });
                });

        //close by real trade

    }

    //todo ejb async
    public Subject<Order, Order> getOrderSubject(){
        return orderSubject;
    }

    public Collection<Order> getOpenOrders(Long strategyId){
        return Collections.unmodifiableCollection(getOpenOrderMap(strategyId).values());
    }

    public void placeLimitOrder(Order order) throws IOException {
        xChangeService.placeLimitOrder(order);

        putOpenOrderCache(order);

        orderBean.save(order);
    }

    public void cancelLimitOrder(Order order) throws IOException {
        xChangeService.cancelLimitOrder(order);

        getOpenOrderMap(order.getStrategy().getId()).remove(order.getOrderId());
    }

    private Map<String, Order> getOpenOrderMap(Long strategyId){
        Map<String, Order> map = openOrderCache.get(strategyId);

        if (map == null){
            map = new ConcurrentHashMap<>();
            openOrderCache.put(strategyId, map);
        }

        return map;
    }

    private void putOpenOrderCache(Order order){
        getOpenOrderMap(order.getStrategy().getId()).put(order.getOrderId(), order);
    }
}
