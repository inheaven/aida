package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import rx.Observable;
import rx.Subscription;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

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
    private Observable<Trade> tradeObservable;
    private Observable<Depth> depthObservable;

    private Subscription closeOrderSubscription;
    private Subscription tradeSubscription;
    private Subscription checkOrderSubscription;
    private Subscription checkAllOrderSubscription;
    private Subscription depthSubscription;
    private Subscription realTradeSubscription;

    private Map<String, Order> orderMap = new ConcurrentHashMap<>();

    private Strategy strategy;

    private boolean flying = false;

    public BaseStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                        DepthService depthService) {
        this.strategy = strategy;
        this.orderService = orderService;
        this.orderMapper = orderMapper;

        orderMapper.getOpenOrders(strategy.getId())
                .forEach(o -> orderMap.put(o.getOrderId(), o));

        orderObservable = orderService.createOrderObserver(strategy);

        tradeObservable = tradeService.createTradeObserver(strategy);

        depthObservable = depthService.createDepthObservable(strategy);
    }

    public void start(){
        if (flying){
            return;
        }

        closeOrderSubscription = orderObservable
                .filter(o -> orderMap.containsKey(o.getOrderId()))
                .filter(o -> o.getStatus().equals(OrderStatus.CLOSED) || o.getStatus().equals(OrderStatus.CANCELED))
                .subscribe(this::closeOrder);

        realTradeSubscription = orderObservable.subscribe(o -> {
            try {
                onRealTrade(o);
            }catch (Exception e){
                log.error("error on real trade -> ", e);
            }
        });

        tradeSubscription = tradeObservable.subscribe(t -> {
            try {
                onTrade(t);
            } catch (Exception e) {
                log.error("error on trader -> ", e);
            }
        });

        checkOrderSubscription = tradeObservable.throttleLast(1, TimeUnit.MINUTES).subscribe(t -> {
            try {
                checkOrders(t);
            } catch (Exception e) {
                log.error("error check order -> ", e);
            }
        });

        checkAllOrderSubscription = tradeObservable.throttleLast(1, TimeUnit.HOURS).subscribe(t -> {
            try {
                orderMap.forEach((id, o) -> orderService.orderInfo(strategy, o));
            } catch (Exception e) {
                log.error("error check all order -> ", e);
            }
        });

        depthSubscription = depthObservable.subscribe(d -> {
            try {
                onDepth(d);
            } catch (Exception e) {
                log.error("error on depth -> ", e);
            }
        });

        orderMap.forEach((id, o) -> orderService.orderInfo(strategy, o));

        flying = true;
    }

    public void stop(){
        closeOrderSubscription.unsubscribe();
        tradeSubscription.unsubscribe();
        checkOrderSubscription.unsubscribe();
        depthSubscription.unsubscribe();
        checkAllOrderSubscription.unsubscribe();
        realTradeSubscription.unsubscribe();

        flying = false;
    }

    protected void onCloseOrder(Order order){
    }

    protected void onRealTrade(Order order){
    }

    protected void onTrade(Trade trade){
    }

    protected void onDepth(Depth depth){
    }

    protected void checkOrders(Trade trade){
        orderMap.values().parallelStream()
                .filter(o -> o.getPrice().subtract(trade.getPrice()).abs().compareTo(strategy.getLevelSpread()
                        .multiply(BigDecimal.valueOf(2))) < 0)
                .forEach(o -> orderService.orderInfo(strategy, o));

        orderMap.values().parallelStream()
                .filter(o -> (BUY_SET.contains(o.getType()) && o.getPrice().compareTo(trade.getPrice()) > 0) ||
                        (SELL_SET.contains(o.getType()) && o.getPrice().compareTo(trade.getPrice()) < 0))
                .forEach(this::closeOrder);
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
            orderMapper.asyncSave(order);

            orderService.onCreateOrder(order);
        } finally {
            orderMap.remove(createdOrderId);
        }
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(2);

    protected Future<Order> createOrderAsync(Order order){
        return executorService.submit(() -> {
            createOrder(order);

            return order;
        });
    }

    protected void closeOrder(Order o){
        try {
            Order order = orderMap.get(o.getOrderId());

            if (order != null) {
                orderMap.remove(o.getOrderId());

                order.close(o);
                orderMapper.asyncSave(order);

                order.setAccountId(strategy.getAccount().getId());
                orderService.onCloseOrder(order);
            }

            onCloseOrder(o);
        } catch (Exception e) {
            log.error("error on close order -> ", e);
        }
    }

    public Map<String, Order> getOrderMap() {
        return orderMap;
    }
}
