package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.BroadcastService;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import rx.Observable;
import rx.Subscription;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CLOSED;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CREATED;
import static ru.inheaven.aida.happy.trading.entity.OrderType.*;

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

                    if (order.getStatus().equals(CLOSED)) {
                        String message = "[" + o.getAvgPrice().setScale(3, HALF_UP)
                                + (OrderType.BUY_SET.contains(order.getType()) ? "↑" : "↓") + "] ";

                        Module.getInjector().getInstance(BroadcastService.class).broadcast(getClass(), "close_order_"
                                + order.getSymbolType().name().toLowerCase(), message);
                    }

                    //profit
                    profit(order);
                }

                onCloseOrder(o);
            } catch (Exception e) {
                log.error("error on close order -> ", e);
            }
        });

        tradeSubscription = tradeObservable.subscribe(t -> {
            try {
                String message = t.getPrice().setScale(3, HALF_UP).toString();
                Module.getInjector().getInstance(BroadcastService.class).broadcast(getClass(), "trade_"
                        + t.getSymbolType().name().toLowerCase(), message);

                onTrade(t);
            } catch (Exception e) {
                log.error("error on trader -> ", e);
            }
        });
        checkOrderSubscription = tradeObservable.throttleLast(30, TimeUnit.SECONDS).subscribe(o -> {
            try {
                checkOrders(o);
            } catch (Exception e) {
                log.error("error check order -> ", e);
            }
        });

        orderMap.forEach((id, o) -> orderService.orderInfo(strategy, o));

        flying = true;
    }

    protected void profit(Order order) {
        Executors.newCachedThreadPool().submit(() -> {
            Map<OrderType, OrderPosition> pos = orderMapper.getOrderPositionMap(strategy);

            BigDecimal profitLong = ZERO;
            BigDecimal equityLong = ZERO;
            if (pos.get(OPEN_LONG) != null && pos.get(CLOSE_LONG) != null) {
                profitLong = pos.get(OPEN_LONG).getAvg()
                        .multiply(pos.get(CLOSE_LONG).getPrice().subtract(pos.get(OPEN_LONG).getPrice())
                                .divide(pos.get(OPEN_LONG).getPrice(), 8, HALF_UP));

                int amount = pos.get(OPEN_LONG).getCount() - pos.get(CLOSE_LONG).getCount();
                OrderType orderType = amount > 0 ? OPEN_LONG : CLOSE_LONG;
                equityLong = pos.get(orderType).getAvg()
                        .multiply(BigDecimal.valueOf(amount))
                        .divide(BigDecimal.valueOf(pos.get(orderType).getCount()), 8, HALF_UP)
                        .multiply(order.getAvgPrice().subtract(pos.get(orderType).getPrice())
                                .divide(pos.get(orderType).getPrice(), 8, HALF_UP));
            }

            BigDecimal profitShort = ZERO;
            BigDecimal equityShort = ZERO;

            if (pos.get(OPEN_SHORT) != null && pos.get(CLOSE_SHORT) != null) {
                profitShort = pos.get(OPEN_SHORT).getAvg()
                        .multiply(pos.get(OPEN_SHORT).getPrice().subtract(pos.get(CLOSE_SHORT).getPrice())
                                .divide(pos.get(OPEN_SHORT).getPrice(), 8, HALF_UP));

                int amount = pos.get(CLOSE_SHORT).getCount() - pos.get(OPEN_SHORT).getCount();
                OrderType orderType = amount > 0 ? CLOSE_SHORT : OPEN_SHORT;
                equityLong = pos.get(orderType).getAvg()
                        .multiply(BigDecimal.valueOf(amount))
                        .divide(BigDecimal.valueOf(pos.get(orderType).getCount()), 8, HALF_UP)
                        .multiply(pos.get(orderType).getPrice().subtract(order.getAvgPrice())
                                .divide(pos.get(orderType).getPrice(), 8, HALF_UP));
            }

            String message = "{" +
                    profitLong.add(equityLong).add(profitShort).add(equityShort).setScale(3, HALF_UP) + " " +
                    profitLong.setScale(3, HALF_UP) + " " +
                    equityLong.setScale(3, HALF_UP) + " " +
                    profitShort.setScale(3, HALF_UP) + " " +
                    equityShort.setScale(3, HALF_UP) +
                    "}\t";

            Module.getInjector().getInstance(BroadcastService.class).broadcast(getClass(), "profit_" +
                    order.getSymbolType().name().toLowerCase(), message);
        });
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
                .filter(o -> (BUY_SET.contains(o.getType()) && o.getPrice().compareTo(trade.getPrice()) > 1) ||
                        (SELL_SET.contains(o.getType()) && o.getPrice().compareTo(trade.getPrice()) < 1) ||
                        o.getPrice().subtract(trade.getPrice()).abs()
                                .compareTo(strategy.getLevelSpread().multiply(BigDecimal.valueOf(2))) < 0)
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

            String message = "(" + order.getPrice().setScale(3, HALF_UP) +
                    (OrderType.BUY_SET.contains(order.getType()) ? "↑":"↓") + ") ";

            Module.getInjector().getInstance(BroadcastService.class).broadcast(getClass(), "create_order_" +
                    order.getSymbolType().name().toLowerCase() , message);
        } finally {
            orderMap.remove(createdOrderId);
        }
    }

    protected Future<Order> createOrderAsync(Order order){
        return Executors.newCachedThreadPool().submit(() -> {
            createOrder(order);

            return order;
        });
    }

    public Map<String, Order> getOrderMap() {
        return orderMap;
    }

    public Observable<Order> getOrderObservable() {
        return orderObservable;
    }
}
