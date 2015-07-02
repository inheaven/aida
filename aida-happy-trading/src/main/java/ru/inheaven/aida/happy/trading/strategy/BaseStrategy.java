package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import rx.Observable;
import rx.Subscription;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author inheaven on 002 02.07.15 16:43
 */
public class BaseStrategy {
    private OrderService orderService;
    private OrderMapper orderMapper;
    private TradeService tradeService;

    private Observable<Order> orderObservable;
    private Observable<Order> closedOrderObservable;

    private Observable<Trade> tradeObservable;

    private Subscription closeOrderSubscription;
    private Subscription tradeSubscription;

    private Map<String, Order> orderMap = new ConcurrentHashMap<>();

    private Strategy strategy;

    public BaseStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService) {
        this.strategy = strategy;
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.tradeService = tradeService;

        orderMapper.getOpenOrders(strategy.getId())
                .forEach(o -> orderMap.put(o.getOrderId(), o));

        orderObservable = orderService.createOrderObserver(strategy);

        closedOrderObservable = orderObservable
                .filter(o -> orderMap.containsKey(o.getOrderId()))
                .filter(o -> o.getStatus().equals(OrderStatus.CLOSED) || o.getStatus().equals(OrderStatus.CANCELED));

        tradeObservable = tradeService.createTradeObserver(strategy);
    }

    public void start(){
        if (strategy.isActive()){
            return;
        }

        closeOrderSubscription = closedOrderObservable.subscribe(this::onCloseOrder);
        tradeSubscription = tradeObservable.subscribe(this::onTrade);

        strategy.setActive(true);
    }

    public void stop(){
        closeOrderSubscription.unsubscribe();
        tradeSubscription.unsubscribe();

        strategy.setActive(false);
    }

    protected void onCloseOrder(Order o){
        Order order = orderMap.get(o.getOrderId());

        if (order != null) {
            order.update(o);
            orderMap.remove(order.getOrderId());
            orderMapper.save(order);
        }
    }

    protected void onTrade(Trade trade){

    }

    protected void createOrder(Order order){
        orderService.createOrder(order);
        orderMap.put(order.getOrderId(), order);

        orderMapper.save(order);
    }

    public Map<String, Order> getOrderMap() {
        return orderMap;
    }

    public Observable<Order> getOrderObservable() {
        return orderObservable;
    }
}
