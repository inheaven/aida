package ru.inheaven.aida.happy.trading.strategy;

import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.*;
import ru.inheaven.aida.happy.trading.util.OrderMap;
import rx.Observable;
import rx.Subscription;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 002 02.07.15 16:43
 */
public class BaseStrategy {
    private Logger log = LoggerFactory.getLogger(getClass());

    private AccountMapper accountMapper;
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
    private Account account;

    private boolean flying = false;

    private AtomicReference<BigDecimal> lastPrice = new AtomicReference<>();
    private AtomicLong askRefusedTime = new AtomicLong(System.currentTimeMillis());
    private AtomicLong bidRefusedTime = new AtomicLong(System.currentTimeMillis());

    private Random random = new SecureRandom();

    private AtomicReference<BigDecimal> buyPrice = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> buyVolume = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> sellPrice = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> sellVolume = new AtomicReference<>(ZERO);

    public BaseStrategy(Strategy strategy) {
        this.strategy = strategy;

        accountMapper = Module.getInjector().getInstance(AccountMapper.class);
        orderMapper = Module.getInjector().getInstance(OrderMapper.class);
        orderService = Module.getInjector().getInstance(OrderService.class);
        tradeService = Module.getInjector().getInstance(TradeService.class);
        depthService = Module.getInjector().getInstance(DepthService.class);
        xChangeService = Module.getInjector().getInstance(XChangeService.class);

        orderObservable = createOrderObservable();
        tradeObservable = createTradeObservable();
        depthObservable = createDepthObservable();

        account = accountMapper.getAccount(strategy.getAccountId());

        orderMap = new OrderMap(getScale(strategy.getSymbol()));

        orderMapper.getOpenOrders(strategy.getId()).forEach(o -> orderMap.put(o));
    }

    private Observable<Order> createOrderObservable(){
        return orderService.getOrderObservable()
                .filter(o -> Objects.equals(strategy.getAccountId(), o.getAccountId()))
                .filter(o -> Objects.equals(strategy.getSymbol(), o.getSymbol()))
                .filter(o -> Objects.equals(strategy.getSymbolType(), o.getSymbolType()));
    }

    private Observable<Trade> createTradeObservable(){
        return tradeService.getTradeObservable()
                .filter(t -> Objects.equals(account.getExchangeType(), t.getExchangeType()))
                .filter(t -> Objects.equals(strategy.getSymbol(), t.getSymbol()))
                .filter(t -> Objects.equals(strategy.getSymbolType(), t.getSymbolType()));
    }

    private Observable<Depth> createDepthObservable(){
        return depthService.getDepthObservable()
                .filter(d -> Objects.equals(account.getExchangeType(), d.getExchangeType()))
                .filter(d -> Objects.equals(strategy.getSymbol(), d.getSymbol()))
                .filter(d -> Objects.equals(strategy.getSymbolType(), d.getSymbolType()));
    }

    private static AtomicReference<OpenOrders> openOrdersCache = new AtomicReference<>();
    private static AtomicReference<Long> openOrdersTime = new AtomicReference<>(System.currentTimeMillis());

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
                    orderService.checkOrder(account, o);
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
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    PollingTradeService ts = xChangeService.getExchange(account).getPollingTradeService();

                    if (System.currentTimeMillis() - openOrdersTime.get() > 10000){
                        openOrdersTime.set(System.currentTimeMillis());
                        openOrdersCache.set(ts.getOpenOrders());
                    }

                    BigDecimal stdDev = tradeService.getStdDev(strategy.getSymbol(), "_1");
                    BigDecimal range = stdDev != null ? stdDev.multiply(BigDecimal.valueOf(3)) : new BigDecimal("10");

                    openOrdersCache.get().getOpenOrders().forEach(l -> {
                        if (lastPrice.get() != null && l.getCurrencyPair().toString().equals(strategy.getSymbol()) &&
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

            }, random.nextInt(10), 10, SECONDS);
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
                                orderService.cancelOrder(account, o);

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
                orderService.createOrder(account, order);

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
            orderService.createOrder(account, order);

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
            orderService.createOrder(account, order);

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

    private void onOrder(Order o){
        if (o.getOrderId() != null && o.getOrderId().contains("refused")){
            if (o.getType().equals(OrderType.ASK)){
                askRefusedTime.set(System.currentTimeMillis());
            }else{
                bidRefusedTime.set(System.currentTimeMillis());
            }

            o.setOrderId(o.getInternalId());
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

                    order.setAccountId(strategy.getAccountId());
                    order.setOrderId(o.getOrderId());
                    order.setText(o.getText());
                    order.close(o);

                    orderMap.remove(order.getInternalId());
                    orderMap.remove(order.getOrderId());

                    orderMapper.asyncSave(order);

                    logOrder(order);
                    calculateAverage(order);
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

    private void calculateAverage(Order order){
        if (CLOSED.equals(order.getStatus())){
            BigDecimal price = order.getAvgPrice() != null ? order.getAvgPrice() : order.getPrice();

            if (BID.equals(order.getType())){
                BigDecimal bv = buyVolume.get();
                buyVolume.set(order.getAmount().add(bv));
                buyPrice.set(buyPrice.get().multiply(bv).add(price.multiply(order.getAmount()))
                        .divide(order.getAmount().add(bv), 8, HALF_EVEN));
            } else{
                BigDecimal sv = sellVolume.get();
                sellVolume.set(order.getAmount().add(sv));
                sellPrice.set(sellPrice.get().multiply(sv).add(price.multiply(order.getAmount()))
                        .divide(order.getAmount().add(sv), 8, HALF_EVEN));
            }

            order.setBuyPrice(buyPrice.get());
            order.setSellPrice(sellPrice.get());
            order.setBuyVolume(buyVolume.get());
            order.setSellVolume(sellVolume.get());
            order.setSpotBalance(getBalance());
        }
    }

    protected BigDecimal getBalance(){
        return ZERO;
    }

    private AtomicLong index = new AtomicLong(0);

    private void logOrder(Order o){
        try {
            BigDecimal profit = sellVolume.get().min(buyVolume.get()).multiply(sellPrice.get().subtract(buyPrice.get()))
                    .add(buyVolume.get().subtract(sellVolume.get().abs())
                            .multiply(buyVolume.get().compareTo(sellVolume.get()) > 0
                                    ? lastPrice.get().subtract(buyPrice.get())
                                    : sellPrice.get().subtract(lastPrice.get())));

            if (o.getStatus().equals(CLOSED)){
                index.incrementAndGet();
            }

            log.info("{} {} {} {} {} {} {} {} {} {} {} {} {} {} {}",
                    o.getStrategyId(),
                    index.get(),
                    o.getStatus(),
                    o.getSymbol(),
                    scale(o.getAvgPrice() != null ? o.getAvgPrice() : o.getPrice()),
                    o.getAmount().setScale(3, HALF_EVEN),
                    o.getType(),
                    scale(buyPrice.get()),
                    profit.setScale(3, HALF_EVEN),
                    buyVolume.get().add(sellVolume.get()).setScale(3, HALF_EVEN),
                    tradeService.getStdDev(strategy.getSymbol(), "_1").setScale(3, HALF_UP),
                    tradeService.getAvgAmount(strategy.getSymbol(), "_1").setScale(3, HALF_UP),
                    getBalance().setScale(3, HALF_UP),
                    Objects.toString(o.getText(), ""),
                    Objects.toString(o.getSymbolType(), ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static final BigDecimal STEP_0_02 = new BigDecimal("0.02");
    private static final BigDecimal STEP_0_001 = new BigDecimal("0.001");

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

    private int getScale(String symbol){
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

    public TradeService getTradeService() {
        return tradeService;
    }

    public OrderMap getOrderMap() {
        return orderMap;
    }

    public Account getAccount() {
        return account;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public boolean isBidRefused(){
        return System.currentTimeMillis() - bidRefusedTime.get() < 1000;
    }

    public boolean isAskRefused(){
        return System.currentTimeMillis() - askRefusedTime.get() < 1000;
    }

    public AtomicReference<BigDecimal> getSellPrice() {
        return sellPrice;
    }

    public AtomicReference<BigDecimal> getBuyPrice() {
        return buyPrice;
    }
}
