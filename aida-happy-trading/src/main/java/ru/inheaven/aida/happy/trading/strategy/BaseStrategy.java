package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import rx.Observable;
import rx.Subscription;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CREATED;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BUY_SET;
import static ru.inheaven.aida.happy.trading.entity.OrderType.SELL_SET;

/**
 * @author inheaven on 002 02.07.15 16:43
 */
public class BaseStrategy {
    private Logger log = LoggerFactory.getLogger(getClass());

    private OrderService orderService;
    private OrderMapper orderMapper;

    private Observable<Order> orderObservable;
    private Observable<Order> closedOrderObservable;

    private Observable<Trade> tradeObservable;

    private Subscription closeOrderSubscription;
    private Subscription tradeSubscription;
    private Subscription checkOrderSubscription;

    private Map<String, Order> orderMap = new ConcurrentHashMap<>();

    private Strategy strategy;

    private boolean flying = false;

    public BaseStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService) {
        this.strategy = strategy;
        this.orderService = orderService;
        this.orderMapper = orderMapper;

        orderMapper.getOpenOrders(strategy.getId())
                .forEach(o -> orderMap.put(o.getOrderId(), o));

        orderObservable = orderService.createOrderObserver(strategy);

        closedOrderObservable = orderObservable
                .filter(o -> orderMap.containsKey(o.getOrderId()))
                .filter(o -> o.getStatus().equals(OrderStatus.CLOSED) || o.getStatus().equals(OrderStatus.CANCELED));

        tradeObservable = tradeService.createTradeObserver(strategy);
    }

    public void start(){
        if (flying){
            return;
        }

        closeOrderSubscription = closedOrderObservable.subscribe(o -> {
            try {
                Order order = orderMap.get(o.getOrderId());

                if (order != null) {
                    order.close(o);
                    orderMap.remove(o.getOrderId());
                    orderMapper.save(order);
                }

                onCloseOrder(o);
            } catch (Exception e) {
                log.error("error on close order -> ", e);
            }
        });

        tradeSubscription = tradeObservable.subscribe(t -> {
            try {
                onTrade(t);
            } catch (Exception e) {
                log.error("error on trader -> ", e);
            }
        });
        checkOrderSubscription = tradeObservable.throttleLast(1, TimeUnit.MINUTES).subscribe(o -> {
            try {
                checkOrders(o);
            } catch (Exception e) {
                log.error("error check order -> ", e);
            }
        });

        orderMap.forEach((id, o) -> orderService.orderInfo(strategy, o));

        flying = true;
    }

    public void stop(){
        closeOrderSubscription.unsubscribe();
        tradeSubscription.unsubscribe();
        checkOrderSubscription.unsubscribe();

        flying = false;
    }

    protected void onCloseOrder(Order o){

    }

    protected void onTrade(Trade trade){
    }

    protected void checkOrders(Trade trade){
        orderMap.values().parallelStream()
                .filter(o -> (BUY_SET.contains(o.getType()) && o.getPrice().compareTo(trade.getPrice()) > 1)
                                || (SELL_SET.contains(o.getType()) && o.getPrice().compareTo(trade.getPrice()) < 1))
                .forEach(o -> orderService.orderInfo(strategy, o));
    }

    protected void createOrder(Order order) throws CreateOrderException {
        order.setCreated(new Date());
        order.setStatus(CREATED);
        order.setPrice(order.getPrice().setScale(8, HALF_UP));

        String createdOrderId = "CREATED->" + System.nanoTime();
        order.setOrderId(createdOrderId);
        orderMap.put(createdOrderId, order);

        try {
            orderService.createOrder(strategy.getAccount(), order);
            orderMap.put(order.getOrderId(), order);
            orderMapper.save(order);
        } finally {
            orderMap.remove(createdOrderId);
        }
    }

    public Map<String, Order> getOrderMap() {
        return orderMap;
    }

    public Observable<Order> getOrderObservable() {
        return orderObservable;
    }
}
