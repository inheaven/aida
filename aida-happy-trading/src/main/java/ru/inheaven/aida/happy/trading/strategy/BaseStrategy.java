package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.*;
import rx.Observable;
import rx.Subscription;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.*;

import static java.math.BigDecimal.TEN;
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
    private Observable<Depth> depthObservable;

    private Subscription closeOrderSubscription;
    private Subscription tradeSubscription;
    private Subscription checkOrderSubscription;
    private Subscription checkAllOrderSubscription;
    private Subscription depthSubscription;

    private Map<String, Order> orderMap = new ConcurrentHashMap<>();

    private Strategy strategy;

    private boolean flying = false;

    private String key;

    public BaseStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                        DepthService depthService) {
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

        depthObservable = depthService.createDepthObservable(strategy);

        switch (strategy.getSymbol()){
            case "BTC/USD":
                key = "btc";
                break;
            case "LTC/USD":
                key = "ltc";
                break;
        }
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
                                + key + "_"
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

        orderMap.forEach((id, o) -> orderService.orderInfo(strategy, o));

        depthSubscription = depthObservable.subscribe(d -> {
            try {
                onDepth(d);
            } catch (Exception e) {
                log.error("error on depth -> ", e);
            }
        });

        flying = true;
    }

    private ExecutorService profitExecutorService = Executors.newCachedThreadPool();

    protected void profit(Order order) {
        profitExecutorService.submit(() -> {
            int count = 0;

            BigDecimal profitLong = ZERO;
            BigDecimal equityLong = ZERO;

            Integer open = orderMapper.getOrderCount(strategy, OPEN_LONG);
            Integer close = orderMapper.getOrderCount(strategy, CLOSE_LONG);

            if (open != null && close != null) {
                profitLong = orderMapper.getOrderVolume(strategy, OPEN_LONG, 0, Math.min(open, close))
                        .subtract(orderMapper.getOrderVolume(strategy, CLOSE_LONG, 0, Math.min(open, close)));

                if (open > close) {
                    equityLong = orderMapper.getOrderVolume(strategy, OPEN_LONG, close, open - close)
                            .subtract(TEN.multiply(BigDecimal.valueOf(open - close)).divide(order.getAvgPrice(), 8, HALF_UP));
                } else {
                    equityLong = TEN.multiply(BigDecimal.valueOf(close - open)).divide(order.getAvgPrice(), 8, HALF_UP)
                            .subtract(orderMapper.getOrderVolume(strategy, CLOSE_LONG, open, close - open));
                }

                count += open + close;
            }

//            BigDecimal profitShort = ZERO;
//            BigDecimal equityShort = ZERO;
//
//            if (pos.get(OPEN_SHORT) != null && pos.get(CLOSE_SHORT) != null && strategy.getType().equals(StrategyType.PARAGLIDER)) {
//                profitShort = pos.get(CLOSE_SHORT).getAvg()
//                        .multiply(pos.get(OPEN_SHORT).getPrice().subtract(pos.get(CLOSE_SHORT).getPrice())
//                                .divide(pos.get(OPEN_SHORT).getPrice(), 8, HALF_UP));
//
////                int amount = pos.get(CLOSE_SHORT).getCount() - pos.get(OPEN_SHORT).getCount();
////                OrderType orderType = amount > 0 ? OPEN_SHORT : CLOSE_SHORT;
////                equityShort = pos.get(orderType).getAvg()
////                        .multiply(BigDecimal.valueOf(amount))
////                        .divide(BigDecimal.valueOf(pos.get(orderType).getCount()), 8, HALF_UP)
////                        .multiply(order.getAvgPrice().subtract(pos.get(orderType).getPrice())
////                                .divide(pos.get(orderType).getPrice(), 8, HALF_UP));
//
//                count += pos.get(OPEN_SHORT).getCount() + pos.get(CLOSE_SHORT).getCount();
//                volume = volume.add(pos.get(CLOSE_SHORT).getAvg().subtract(pos.get(OPEN_SHORT).getAvg()));
//            }


            String message = "{" +
//                    strategy.getName() + " " +
                    profitLong.add(equityLong).setScale(3, HALF_UP) + " " +
                    profitLong.setScale(3, HALF_UP) + " " +
                    equityLong.setScale(3, HALF_UP) + " " +
//                    profitShort.setScale(3, HALF_UP) + " " +
//                    equityShort.setScale(3, HALF_UP) +
                    count +
                    "}\t";

            Module.getInjector().getInstance(BroadcastService.class).broadcast(getClass(), "profit_"
                    + key + "_" +
                    order.getSymbolType().name().toLowerCase(), message);
        });
    }

    public void stop(){
        closeOrderSubscription.unsubscribe();
        tradeSubscription.unsubscribe();
        checkOrderSubscription.unsubscribe();
        depthSubscription.unsubscribe();
        checkAllOrderSubscription.unsubscribe();

        flying = false;
    }

    protected void onCloseOrder(Order order){
    }

    protected void onTrade(Trade trade){
    }

    protected void onDepth(Depth depth){
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
                    key + "_" +
                    order.getSymbolType().name().toLowerCase() , message);
        } finally {
            orderMap.remove(createdOrderId);
        }
    }

    private ExecutorService executorService = Executors.newFixedThreadPool(4);

    protected Future<Order> createOrderAsync(Order order){
        return executorService.submit(() -> {
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
