package ru.inheaven.aida.happy.trading.strategy;

import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import ru.inheaven.aida.happy.trading.service.XChangeService;
import ru.inheaven.aida.happy.trading.util.OrderMap;
import rx.Observable;
import rx.Subscription;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;

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
    private Observable<Depth> depthObservable;

    private Subscription orderSubscription;
    private Subscription tradeSubscription;
    private Subscription depthSubscription;
    private Subscription realTradeSubscription;

    private XChangeService xChangeService;

    private OrderMap orderMap;

    private Strategy strategy;

    private boolean flying = false;

    private AtomicReference<BigDecimal> lastPrice = new AtomicReference<>();
    private AtomicLong askRefusedTime = new AtomicLong(System.currentTimeMillis());
    private AtomicLong bidRefusedTime = new AtomicLong(System.currentTimeMillis());


    public BaseStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                        DepthService depthService,  XChangeService xChangeService) {
        this.strategy = strategy;
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.tradeService = tradeService;
        this.depthService = depthService;
        this.xChangeService = xChangeService;

        orderObservable = createOrderObservable();
        tradeObservable = createTradeObservable();
        depthObservable = createDepthObservable();

        orderMap = new OrderMap(getScale(strategy.getSymbol()));

        orderMapper.getOpenOrders(strategy.getId()).forEach(o -> orderMap.put(o));
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

    protected Observable<Depth> createDepthObservable(){
        return depthService.createDepthObservable(strategy);
    }

    public void start(){
        if (flying){
            return;
        }

        tradeSubscription = tradeObservable
                .subscribe(t -> {
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
                .filter(o -> orderMap.contains(o.getOrderId()) ||
                        (o.getInternalId() != null && orderMap.contains(o.getInternalId())))
                .subscribe(this::onOrder);

        realTradeSubscription = orderObservable
                .subscribe(o -> {
                    try {
                        onRealTrade(o);
                    } catch (Exception e) {
                        log.error("error on real trade -> ", e);
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
                if (o.getStatus().equals(OPEN)) {
                    orderService.checkOrder(strategy.getAccount(), o);
                }else if ((o.getStatus().equals(CANCELED) || o.getStatus().equals(CLOSED)) && (o.getClosed() == null ||
                        System.currentTimeMillis() - o.getClosed().getTime() > 60000)) {
                    onOrder(o);
                    log.info("{} CLOSED by schedule {}", o.getStrategyId(), scale(o.getPrice()));
                }else if (o.getStatus().equals(CREATED) &&
                        System.currentTimeMillis() - o.getCreated().getTime() > 10000){
                    o.setStatus(CLOSED);
                    o.setClosed(new Date());
                    log.info("{} CLOSED by created {}", o.getStrategyId(), scale(o.getPrice()));
                }
            } catch (Exception e) {
                log.error("error check order -> ", e);
            }

        }), 0, 1, MINUTES);

        if (strategy.getSymbol().equals("BTC/CNY") || strategy.getSymbol().equals("LTC/CNY")){
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                try {
                    PollingTradeService ts = xChangeService.getExchange(strategy.getAccount()).getPollingTradeService();

                    BigDecimal stdDev = tradeService.getStdDev(strategy.getSymbol());
                    BigDecimal range = stdDev != null ? stdDev.multiply(BigDecimal.valueOf(5)) : new BigDecimal("10");

                    OpenOrders openOrders = ts.getOpenOrders();
                    openOrders.getOpenOrders().forEach(l -> {
                        if (l.getCurrencyPair().toString().equals(strategy.getSymbol()) &&
                                lastPrice.get().subtract(l.getLimitPrice()).abs().compareTo(range) > 0){
                            try {
                                ts.cancelOrder(l.getId());

                                Order order = orderMap.get(l.getId());

                                if (order != null){
                                    order.setStatus(CANCELED);
                                }

                                log.info("schedule cancel order {}", l);
                            } catch (IOException e) {
                                log.error("error schedule cancel order -> ", e);
                            }
                        }
                    });

                } catch (Exception e) {
                    log.error("error schedule cancel order -> ", e);
                }

            }, 0, 1, MINUTES);
        }

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(
                () -> orderMap.forEach((id, o) -> {
                    try {
                        if (lastPrice != null && lastPrice.get() != null && o.getStatus().equals(OPEN)) {
                            boolean cancel = false;

                            switch (o.getSymbol()) {
//                                case "BTC/CNY":
//                                    if (o.getPrice().subtract(lastPrice.get()).abs().compareTo(new BigDecimal("10"))  > 0) {
//                                        cancel = true;
//                                    }
//                                    break;
//                                case "LTC/CNY'":
//                                    if (o.getPrice().subtract(lastPrice.get()).abs().compareTo(new BigDecimal("0.5")) > 0) {
//                                        cancel = true;
//                                    }
//                                    break;
                                case "BTC/USD":
                                    if (o.getPrice().subtract(lastPrice.get()).abs().compareTo(new BigDecimal("1"))  > 0) {
                                        cancel = true;
                                    }
                                    break;
                                case "LTC/USD":
                                    if (o.getPrice().subtract(lastPrice.get()).abs().compareTo(new BigDecimal("0.3")) > 0) {
                                        cancel = true;
                                    }
                                    break;

                                default:
                                    cancel = lastPrice.get().subtract(o.getPrice()).abs().divide(o.getPrice(), 8, HALF_EVEN)
                                            .compareTo(new BigDecimal("0.1")) > 0;
                            }

                            if (cancel) {
                                orderService.cancelOrder(strategy.getAccount(), o);

                                if (strategy.getSymbol().equals("BTC/CNY")){
                                    o.setStatus(CANCELED);
                                }else {
                                    o.setStatus(WAIT);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("error cancel order -> {}", o, e);
                    }

                }), 0, 10, SECONDS);

        flying = true;
    }

    public void stop(){
        orderSubscription.unsubscribe();
        tradeSubscription.unsubscribe();
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

    private ExecutorService executorService = Executors.newCachedThreadPool();

    private static AtomicLong internalId = new AtomicLong(System.nanoTime());

    @SuppressWarnings("Duplicates")
    protected Future<Order> createOrderAsync(Order order){
        order.setInternalId(String.valueOf(internalId.incrementAndGet()));
        order.setOrderId(order.getInternalId());
        order.setStatus(CREATED);
        order.setCreated(new Date());

        orderMap.put(order);

        return executorService.submit(() -> {
            try {
                orderService.createOrder(strategy.getAccount(), order);

                if (order.getStatus().equals(OPEN)){
                    orderMap.update(order);
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

    @SuppressWarnings("Duplicates")
    protected void createOrderSync(Order order){
        order.setInternalId(String.valueOf(internalId.incrementAndGet()));
        order.setOrderId(order.getInternalId());
        order.setStatus(CREATED);
        order.setCreated(new Date());

        orderMap.put(order);

        try {
            orderService.createOrder(strategy.getAccount(), order);

            if (order.getStatus().equals(OPEN)){
                orderMap.update(order);
                orderMapper.asyncSave(order);
            }

            logOrder(order);
        } catch (Exception e) {
            orderMap.remove(order.getInternalId());

            log.error("error create order -> {}", order, e);
        }
    }

    protected void createWaitOrder(Order order){
        order.setInternalId(String.valueOf(internalId.incrementAndGet()));
        order.setOrderId(order.getInternalId());
        order.setStatus(WAIT);
        order.setCreated(new Date());

        orderMap.put(order);
        orderMapper.asyncSave(order);

        logOrder(order);
    }


    @SuppressWarnings("Duplicates")
    protected void pushWaitOrder(Order order){
        if (order.getStatus().equals(CREATED)){

            log.warn("CREATED already");
            return;
        }

        order.setStatus(CREATED);
        order.setCreated(new Date());

        try {
            orderService.createOrder(strategy.getAccount(), order);

            if (order.getStatus().equals(OPEN)){
                orderMap.update(order);
                orderMapper.asyncSave(order);
            }

            logOrder(order);
        } catch (Exception e) {
            orderMap.remove(order.getInternalId());

            log.error("error create order -> {}", order, e);
        }
    }

    protected void onOrder(Order o){
        if (o.getOrderId() != null && o.getOrderId().contains("refused")){
            if (o.getType().equals(OrderType.ASK)){
                askRefusedTime.set(System.currentTimeMillis());
            }else{
                bidRefusedTime.set(System.currentTimeMillis());
            }

            o.setOrderId(o.getInternalId());

//            Order order = orderMap.get(o.getInternalId());
//
//            if (order != null && !strategy.getSymbol().equals("BTC/CNY")){
//                order.setStatus(WAIT);
//                orderMapper.asyncSave(order);
//                logOrder(order);
//
//                return;
//            }
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
                    if (order.getStatus().equals(WAIT) && o.getStatus().equals(CANCELED)){
                        String removeOrderId = order.getOrderId();

                        order.setInternalId(String.valueOf(internalId.incrementAndGet()));
                        order.setOrderId(order.getInternalId());

                        orderMap.update(order, removeOrderId);
                        orderMapper.asyncSave(order);

                        logOrder(order);

                        return;
                    }

                    order.setAccountId(strategy.getAccount().getId());
                    order.setOrderId(o.getOrderId());
                    order.close(o);

                    orderMap.remove(order.getInternalId());
                    orderMap.remove(order.getOrderId());

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

                    orderMap.update(order);

                    orderMapper.asyncSave(order);

                    logOrder(order);
                }
            }
        } catch (Exception e) {
            log.error("error on order -> ", e);
        }
    }

    private void logOrder(Order o){
        log.info("{} {} {} {} {} {} {} {}",
                o.getStrategyId(),o.getStatus(), o.getSymbol(),
                scale(o.getAvgPrice() != null ? o.getAvgPrice() : o.getPrice()),
                o.getAvgPrice() != null
                        ? scale(o.getType().equals(OrderType.ASK)
                        ? o.getAvgPrice().subtract(o.getPrice()) : o.getPrice().subtract(o.getAvgPrice()))
                        : scale(ZERO),
                o.getAmount().setScale(3, HALF_EVEN), o.getType(), Objects.toString(o.getSymbolType(), ""));
    }

    public static final BigDecimal STEP_0_01 = new BigDecimal("0.01");
    public static final BigDecimal STEP_0_02 = new BigDecimal("0.02");
    public static final BigDecimal STEP_0_001 = new BigDecimal("0.001");

    public BigDecimal getStep(){
        switch (strategy.getSymbol()){
            case "BTC/USD":
            case "BTC/CNY":
            case "LTC/CNY":
                return STEP_0_02;
            case "LTC/USD":
                return STEP_0_001;
        }

        return ZERO;
    }

    public static int getScale(String symbol){
        switch (symbol){
            case "BTC/USD":
            case "BTC/CNY":
            case "LTC/CNY":
                return 2;
            case "LTC/USD":
                return 3;
        }

        return 8;
    }

    public BigDecimal scale(BigDecimal value){
        if (value == null){
            return null;
        }

        return value.setScale(getScale(strategy.getSymbol()), HALF_EVEN);
    }

    public int compare(BigDecimal v1, BigDecimal v2){
        return scale(v1).compareTo(scale(v2));
    }

    public OrderMap getOrderMap() {
        return orderMap;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public boolean isBidRefused(){
        return System.currentTimeMillis() - bidRefusedTime.get() < 10000;
    }

    public boolean isAskRefused(){
        return System.currentTimeMillis() - askRefusedTime.get() < 10000;
    }
}
