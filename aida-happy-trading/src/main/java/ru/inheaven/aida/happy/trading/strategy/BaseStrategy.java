package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Depth;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.exception.OrderInfoException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import rx.Observable;
import rx.Subscription;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.RoundingMode.HALF_UP;
import static java.util.concurrent.TimeUnit.MINUTES;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;
import static ru.inheaven.aida.happy.trading.entity.OrderType.*;

/**
 * @author inheaven on 002 02.07.15 16:43
 */
public class BaseStrategy {
    private Logger log = LoggerFactory.getLogger(getClass());

    private OrderService orderService;
    private OrderMapper orderMapper;
    private TradeService tradeService;
    private DepthService depthService;

    private Observable<Order> orderObservable;
    private Observable<Trade> tradeObservable;
    private Observable<Trade> allTradeObservable;
    private Observable<Depth> depthObservable;

    private Subscription orderSubscription;
    private Subscription tradeSubscription;
    private Subscription checkOrderSubscription;
    private Subscription depthSubscription;
    private Subscription realTradeSubscription;

    private ConcurrentHashMap<String, Order> orderMap = new ConcurrentHashMap<>();

    private Strategy strategy;

    private boolean flying = false;

    private int errorCount = 0;
    private long errorTime = 0;

    private AtomicReference<BigDecimal> lastPrice = new AtomicReference<>();
    private AtomicLong refusedTime = new AtomicLong(System.currentTimeMillis());


    public BaseStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                        DepthService depthService) {
        this.strategy = strategy;
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.tradeService = tradeService;
        this.depthService = depthService;

        orderObservable = createOrderObservable();
        tradeObservable = createTradeObservable();
        allTradeObservable = createAllTradeObservable();
        depthObservable = createDepthObservable();

        orderMapper.getOpenOrders(strategy.getId()).forEach(o -> orderMap.put(o.getOrderId(), o));
    }

    protected Observable<Order> createOrderObservable(){
        return orderService.getOrderObservable()
                .filter(o -> Objects.equals(strategy.getAccount().getExchangeType(), o.getExchangeType()))
                .filter(o -> Objects.equals(strategy.getSymbol(), o.getSymbol()))
                .filter(o -> Objects.equals(strategy.getSymbolType(), o.getSymbolType()));
    }

    protected Observable<Trade> createTradeObservable(){
        return tradeService.getTradeObservable()
                .filter(t -> Objects.equals(strategy.getAccount().getExchangeType(), t.getExchangeType()))
                .filter(t -> Objects.equals(strategy.getSymbol(), t.getSymbol()))
                .filter(t -> Objects.equals(strategy.getSymbolType(), t.getSymbolType()));
    }

    protected Observable<Trade> createAllTradeObservable(){
        return tradeService.getTradeObservable()
                .filter(t -> Objects.equals(strategy.getAccount().getExchangeType(), t.getExchangeType()))
                .filter(t -> Objects.equals(strategy.getSymbol(), t.getSymbol()))
                .filter(t -> Objects.equals(strategy.getSymbolType(), t.getSymbolType()));
    }

    protected Observable<Depth> createDepthObservable(){
        return depthService.createDepthObservable(strategy);
    }

    public void start(){
        if (flying){
            return;
        }

        tradeSubscription = allTradeObservable.subscribe(t -> {
            try {
                if (t.getPrice() != null) {
                    lastPrice.set(t.getPrice());
                }

                onTrade(t);
            } catch (Exception e) {
                log.error("error on trader -> ", e);
            }
        });

        orderSubscription = orderObservable
                .filter(o -> orderMap.containsKey(o.getOrderId()) ||
                        (o.getInternalId() != null && orderMap.containsKey(o.getInternalId())))
                .subscribe(this::onOrder);

        realTradeSubscription = orderObservable.subscribe(o -> {
            try {
                onRealTrade(o);
            } catch (Exception e) {
                log.error("error on real trade -> ", e);
            }
        });

        checkOrderSubscription = tradeObservable.throttleLast(1, TimeUnit.MINUTES).subscribe(t -> {
            try {
                checkOrders(t);
            } catch (Exception e) {
                log.error("error check order -> ", e);
            }
        });

        depthSubscription = depthObservable.subscribe(d -> {
            try {
                onDepth(d);
            } catch (Exception e) {
                log.error("error on depth -> ", e);
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> orderMap.forEach((id, o) -> {
            try {
                orderService.checkOrder(strategy.getAccount(), o);

                if (o.getStatus().equals(CANCELED) || o.getStatus().equals(CLOSED)) {
                    onOrder(o);
                    log.info("schedule close order -> {} {} {} {}", o.getOrderId(), o.getSymbol(),
                            Objects.toString(o.getSymbolType(), ""), o.getStatus());
                }
            } catch (OrderInfoException e) {
                log.error("error check order -> ", e);
            }

        }), 0, 1, MINUTES);


        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> orderMap.forEach((id, o) -> {
                    try {
                        if (lastPrice != null && o.getStatus().equals(OPEN)) {
                            boolean cancel = false;

                            switch (o.getSymbol()) {
                                case "BTC/CNY":
                                    if (o.getPrice().subtract(lastPrice.get()).abs().compareTo(new BigDecimal("5"))  > 0) {
                                        cancel = true;
                                    }
                                    break;
                                case "LTC/CNY'":
                                    if (o.getPrice().subtract(lastPrice.get()).abs().compareTo(new BigDecimal("1")) > 0) {
                                        cancel = true;
                                    }
                                    break;
                                default:
                                    cancel = lastPrice.get().subtract(o.getPrice()).abs().divide(o.getPrice(), 8, HALF_UP)
                                            .compareTo(new BigDecimal("0.05")) > 0;
                            }

                            if (cancel && !o.getStatus().equals(CANCELED)) {
                                orderService.cancelOrder(strategy.getAccount(), o);
                                o.setStatus(CANCELED);
                                onOrder(o);
                                //log.info("cancel order -> {} {}", lastPrice, o);
                            }
                        }
                    } catch (Exception e) {
                        log.error("error cancel order -> {}", o, e);
                    }

                }), 0, 1, MINUTES);

        flying = true;
    }

    public void stop(){
        orderSubscription.unsubscribe();
        tradeSubscription.unsubscribe();
        checkOrderSubscription.unsubscribe();
        depthSubscription.unsubscribe();
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
    }

    protected void closeOnCheck(BigDecimal bid, BigDecimal ask){
        orderMap.forEach(64, (k, o) ->{
            if ((o.getStatus().equals(OPEN) || o.getStatus().equals(CREATED)) &&
                    ((BUY_SET.contains(o.getType()) && o.getPrice().compareTo(ask) > 0) ||
                            (SELL_SET.contains(o.getType()) && o.getPrice().compareTo(bid) < 0))){
                o.setStatus(CLOSED);
            }
        });
    }

    private ExecutorService executorService = Executors.newWorkStealingPool();

    private static AtomicLong internalId = new AtomicLong(System.nanoTime());

    protected Future<Order> createOrderAsync(Order order){
        order.setInternalId(String.valueOf(internalId.incrementAndGet()));
        order.setPrice(order.getPrice().setScale(8, HALF_UP));
        order.setStatus(CREATED);
        order.setCreated(new Date());

        orderMap.put(order.getInternalId(), order);

        return executorService.submit(() -> {
            try {
                orderService.createOrder(strategy.getAccount(), order);

                if (order.getStatus().equals(OPEN)){
                    orderMap.put(order.getOrderId(), order);
                    orderMap.remove(order.getInternalId());

                    orderMapper.asyncSave(order);
                }

                logOrder(order);
            } catch (Exception e) {
                orderMap.remove(order.getInternalId());

                log.error("error create order -> {}", order, e);
            }

            return order;
        });
    }

    protected void onOrder(Order o){
        if ("refused".equals(o.getOrderId())){
            try {
                Long positionId = System.nanoTime();

                if (o.getType().equals(BID)){
                    createOrderAsync(new Order(strategy, positionId, ASK, lastPrice.get().subtract(strategy.getLevelSpread()),
                            strategy.getLevelLot()));
                }else{
                    createOrderAsync(new Order(strategy, positionId, BID, lastPrice.get().add(strategy.getLevelSpread()),
                            strategy.getLevelLot()));
                }

                refusedTime.set(System.currentTimeMillis());
            } catch (Exception e) {
                log.error("refused {}", o);
            }
        }

        try {
            if (o.getStatus().equals(CANCELED) || o.getStatus().equals(CLOSED)){
                Order order = null;

                if (o.getOrderId() != null) {
                    order = orderMap.get(o.getOrderId());
                }

                if (order == null && o.getInternalId() != null){
                    order = orderMap.get(o.getInternalId());
                }

                if (order != null) {
                    order.setAccountId(strategy.getAccount().getId());
                    order.setOrderId(o.getOrderId());
                    order.close(o);

                    orderMap.remove(o.getOrderId());

                    if (o.getInternalId() != null) {
                        orderMap.remove(o.getInternalId());
                    }

                    orderMapper.asyncSave(order);

                    logOrder(order);
                    onCloseOrder(order);
                    orderService.onCloseOrder(order);
                }
            }else if (o.getInternalId() != null && o.getStatus().equals(OPEN)){
                Order order = orderMap.get(o.getInternalId());

                if (order != null){
                    order.setOrderId(o.getOrderId());
                    order.setStatus(OPEN);
                    order.setOpen(o.getOpen());

                    orderMap.put(o.getOrderId(), order);
                    orderMap.remove(o.getInternalId());

                    orderMapper.asyncSave(order);

                    logOrder(order);
                }
            }
        } catch (Exception e) {
            log.error("error on order -> ", e);
        }
    }

    private void logOrder(Order o){
        log.info("{} {} {} {} {} {} {} {} {}", o.getStrategyId(),
                Objects.toString(o.getPositionId(), "->"), Objects.toString(o.getOrderId(), "->"), o.getStatus(),
                o.getSymbol(), o.getPrice().setScale(3, HALF_UP),
                o.getAmount().setScale(3, HALF_UP), o.getType(), Objects.toString(o.getSymbolType(), ""));
    }

    public ConcurrentHashMap<String, Order> getOrderMap() {
        return orderMap;
    }

    public void incrementErrorCount(){
        errorCount++;
    }

    public void decrementErrorCount(){
        errorCount--;
    }

    public int getErrorCount() {
        return errorCount;
    }

    public void setErrorCount(int errorCount) {
        this.errorCount = errorCount;
    }

    public long getErrorTime() {
        return errorTime;
    }

    public void setErrorTime(long errorTime) {
        this.errorTime = errorTime;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public long getRefusedTime() {
        return refusedTime.get();
    }
}
