package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.Order;
import ru.inheaven.aida.coin.entity.OrderStatus;
import ru.inheaven.aida.coin.entity.OrderType;
import ru.inheaven.aida.coin.entity.Trade;
import rx.Observer;
import rx.subjects.PublishSubject;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

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
        orderBean.getOrders(OrderStatus.OPEN).forEach(this::putOpenOrderCache);

        //update status
        orderSubject
                .filter(o -> !o.getStatus().equals(OrderStatus.OPEN))
                .subscribe(o -> {
                    openOrderCache.forEach((i, m) -> m.remove(o.getOrderId()));

                    Order order = orderBean.getOrder(o.getOrderId());

                    if (order != null) {
                        order.setName(o.getName());
                        order.setCreated(o.getCreated());
                        order.setFilledAmount(o.getFilledAmount());
                        order.setAvgPrice(o.getAvgPrice());
                        order.setStatus(o.getStatus());
                        orderBean.save(order);
                    }
                });

        //close order by trade price
        tradeService.getTradeSubject()
                .groupBy(Trade::getSymbol)
                .map(o -> o.sample(1, TimeUnit.MINUTES))
                .subscribe(observable -> {
                    observable.subscribe(t -> {
                        openOrderCache.forEach((i, m) -> {
                            m.values().stream()
                                    .filter(o -> o.getExchangeType() == t.getExchangeType())
                                    .filter(o -> o.getSymbol().equals(t.getSymbol()))
                                    .filter(o -> o.getPrice().compareTo(t.getPrice()) > 0 && OrderType.BUY.contains(o.getType()))
                                    .filter(o -> o.getPrice().compareTo(t.getPrice()) < 0 && OrderType.SELL.contains(o.getType()))
                                    .forEach(o -> {
                                        m.remove(o.getOrderId());

                                        o.setStatus(OrderStatus.CLOSED);
                                        o.setClosed(new Date());
                                        orderBean.save(o);
                                    });

                        });
                    });
                });
    }

    public Observer<Order> getOrderObserver(){
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
