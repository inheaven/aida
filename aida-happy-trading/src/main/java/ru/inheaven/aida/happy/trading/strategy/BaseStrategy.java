package ru.inheaven.aida.happy.trading.strategy;

import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.*;
import ru.inheaven.aida.happy.trading.util.OrderDoubleMap;
import rx.Observable;
import rx.Subscription;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
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

    private OrderDoubleMap orderMap;

    private Strategy strategy;

    private boolean flying = false;

    private AtomicReference<BigDecimal> lastPrice = new AtomicReference<>();
    private AtomicLong askRefusedTime = new AtomicLong(System.currentTimeMillis());
    private AtomicLong bidRefusedTime = new AtomicLong(System.currentTimeMillis());

    private AtomicReference<BigDecimal> buyPrice = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> buyVolume = new AtomicReference<>(ZERO);

    private AtomicReference<BigDecimal> sellPrice = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> sellVolume = new AtomicReference<>(ZERO);

    private final boolean vol;
    private final String volSuffix;

    private UserInfoService userInfoService;

    public BaseStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                        DepthService depthService,  XChangeService xChangeService) {
        this.strategy = strategy;
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.tradeService = tradeService;
        this.depthService = depthService;
        this.xChangeService = xChangeService;

        userInfoService = Module.getInjector().getInstance(UserInfoService.class);

        orderObservable = createOrderObservable();
        tradeObservable = createTradeObservable();
        depthObservable = createDepthObservable();

        orderMap = new OrderDoubleMap();

        orderMapper.getOpenOrders(strategy.getId()).forEach(o -> orderMap.put(o));

        vol = strategy.getName().contains("vol");

        if (strategy.getName().contains("vol_0")){
            volSuffix = "_0";
        }else if (strategy.getName().contains("vol_1")){
            volSuffix = "_1";
        }else if (strategy.getName().contains("vol_2")){
            volSuffix = "_2";
        }else if (strategy.getName().contains("vol_3")){
            volSuffix = "_3";
        }else if (strategy.getName().contains("vol_4")){
            volSuffix = "_4";
        }else if (strategy.getName().contains("vol_5")){
            volSuffix = "_5";
        }else if (strategy.getName().contains("vol_6")){
            volSuffix = "_6";
        }else if (strategy.getName().contains("vol_7")){
            volSuffix = "_7";
        }else{
            volSuffix = "";
        }
    }

    public boolean isVol() {
        return vol;
    }

    public String getVolSuffix() {
        return volSuffix;
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

    private static AtomicReference<OpenOrders> openOrdersCache = new AtomicReference<>();

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
                        log.error("error onTrade", e);
                    }
                });

        orderSubscription = orderObservable
                .filter(o -> o.getOrderId() != null && orderMap.contains(o.getOrderId()) ||
                        (o.getInternalId() != null && orderMap.contains(o.getInternalId())))
                .subscribe(o -> {
                    try {
                        onOrder(o);
                    } catch (Exception e) {
                        log.error("error onOrder");
                    }
                });

        realTradeSubscription = orderObservable
                .subscribe(o -> {
                    try {
                        if (o.getPrice() != null) {
                            lastPrice.set(o.getPrice());
                        }

                        onRealTrade(o);
                    } catch (Exception e) {
                        log.error("error onRealTrade", e);
                    }
                });

        depthSubscription = depthObservable.subscribe(d -> {
            try {
                if (d.getBid() != null) {
                    lastPrice.set(d.getBid());
                }

                onDepth(d);
            } catch (Exception e) {
                log.error("error onDepth", e);
            }
        });

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> orderMap.forEach((id, o) -> {
            try {
                if (o.getStatus().equals(OPEN)) {
                    orderService.checkOrder(strategy.getAccount(), o);
                }else if ((o.getStatus().equals(CANCELED) || o.getStatus().equals(CLOSED)) && (o.getClosed() == null ||
                        System.currentTimeMillis() - o.getClosed().getTime() > 60000)) {
                    onOrder(o);
                    log.info("{} CLOSE by schedule {}", o.getStrategyId(), scale(o.getPrice()));
                }else if (o.getStatus().equals(CREATED) &&
                        System.currentTimeMillis() - o.getCreated().getTime() > 10000){
                    o.setStatus(CLOSED);
                    o.setClosed(new Date());
                    log.info("{} CLOSE by created {}", o.getStrategyId(), scale(o.getPrice()));
                }
            } catch (Exception e) {
                log.error("error check order -> ", e);
            }

        }), 0, 1, MINUTES);

        if (strategy.getSymbol().equals("BTC/CNY") || strategy.getSymbol().equals("LTC/CNY")){
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    PollingTradeService ts = xChangeService.getExchange(strategy.getAccount()).getPollingTradeService();

                    List<LimitOrder> openOrders = ts.getOpenOrders().getOpenOrders();

                    if (openOrders.size() > 45){
                        openOrders.sort((o1, o2) -> o1.getTimestamp().compareTo(o2.getTimestamp()));

                        for (int i=0; i<=5; ++i){
                            try {
                                LimitOrder l = openOrders.get(i);

                                ts.cancelOrder(l.getId());

                                Order order = orderMap.get(l.getId());

                                if (order != null){
                                    order.setStatus(CANCELED);

                                    log.info("schedule 50 cancel order {} {}", l.getLimitPrice(), l.getTradableAmount());
                                }
                            } catch (IOException e) {
                                log.error("error schedule 50 cancel order -> ", e);
                            }
                        }
                    }

                    BigDecimal range = getSpread(lastPrice.get()).multiply(BigDecimal.valueOf(20));

                    openOrders.forEach(l -> {
                        if (lastPrice.get() != null && l.getCurrencyPair().toString().equals(strategy.getSymbol()) &&
                                lastPrice.get().subtract(l.getLimitPrice()).abs().compareTo(range) > 0){
                            try {
                                ts.cancelOrder(l.getId());

                                Order order = orderMap.get(l.getId());

                                if (order != null){
                                    order.setStatus(CANCELED);
                                }

                                log.info("schedule cancel order {} {} {}", lastPrice.get(), l.getLimitPrice(), l.getTradableAmount());
                            } catch (IOException e) {
                                log.error("error schedule cancel order -> ", e);
                            }
                        }
                    });
                } catch (Exception e) {
                    log.error("error schedule cancel order -> ", e);
                }

            }, 0, 15, SECONDS);
        }

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(
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

    protected BigDecimal getSpread(BigDecimal price){
        return null;
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
                    order.setText(o.getText());
                    order.close(o);

                    orderMap.remove(order.getInternalId());
                    orderMap.remove(order.getOrderId());

                    orderMapper.asyncSave(order);

                    calculateAverage(order);
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

    private AtomicInteger window = new AtomicInteger(0);

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
            //order.setSpotBalance(getSpotBalance());

//            if (window.incrementAndGet() > 100){
//                window.set(0);
//
//                buyPrice.set(ZERO);
//                sellPrice.set(ZERO);
//                buyVolume.set(ZERO);
//                sellVolume.set(ZERO);
//
//                sessionProfit.set(profit);
//            }
        }
    }

    public BigDecimal getProfit(){
        return sellVolume.get().min(buyVolume.get()).multiply(sellPrice.get().subtract(buyPrice.get()))
                .add(buyVolume.get().subtract(sellVolume.get()).abs()
                        .multiply(buyVolume.get().compareTo(sellVolume.get()) > 0
                                ? lastPrice.get().subtract(buyPrice.get())
                                : sellPrice.get().subtract(lastPrice.get())));
    }

    protected boolean getSpotBalance(){
        return false;
    }

    protected double getForecast(){
        return 0;
    }

    protected BigDecimal getAvgPrice(){
        return ZERO;
    }

    protected Long getWindow(){
        return 0L;
    }

    protected BigDecimal getStdDev(){
        return ZERO;
    }

    private final static BigDecimal BD_SQRT_TWO_PI = new BigDecimal("2.506628274631000502415765284811");

    private AtomicLong index = new AtomicLong(0);

    private void logOrder(Order o){
        try {


            if (o.getStatus().equals(CLOSED)){
                index.incrementAndGet();
            }

            log.info("{} {} {} {} {} {}  {} {} {} {} {} {}  {} {}  {} {} {} {} {} {} {} {} {}",
                    index.get(),
                    o.getStrategyId(),
                    o.getStatus(),
                    scale(o.getPrice()),
                    o.getAmount().setScale(3, HALF_EVEN),
                    o.getType(),

                    scale(getAvgPrice()),
                    scale(buyPrice.get()),
                    scale(sellPrice.get().subtract(buyPrice.get())),
                    scale(buyVolume.get()),
                    scale(sellVolume.get().subtract(buyVolume.get())),
                    scale(sellVolume.get().multiply(sellPrice.get()).subtract(buyVolume.get().multiply(buyPrice.get()))),

                    getStdDev().setScale(3, HALF_UP),
                    scale(o.getSpread()),

                    getWindow(),
                    scale(userInfoService.getVolume("subtotal", strategy.getAccount().getId(), "BTC")),
                    scale(userInfoService.getVolume("subtotal", strategy.getAccount().getId(), "CNY")),
                    userInfoService.getVolume("total", strategy.getAccount().getId(), null),
                    getProfit().setScale(3, HALF_EVEN),
                    BigDecimal.valueOf(o.getForecast()).setScale(3, HALF_EVEN),
                    o.isBalance() ? "↑" : "↓",
                    Objects.toString(o.getText(), ""),
                    Objects.toString(o.getSymbolType(), ""));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static final BigDecimal STEP_0_01 = new BigDecimal("0.01");
    public static final BigDecimal STEP_0_02 = new BigDecimal("0.02");
    public static final BigDecimal STEP_0_001 = new BigDecimal("0.001");

    public BigDecimal getStep(){
        switch (strategy.getSymbol()){
            case "BTC/USD":
            case "BTC/CNY":
            case "LTC/CNY":
                return STEP_0_01;
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

    public OrderDoubleMap getOrderMap() {
        return orderMap;
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

    public AtomicReference<BigDecimal> getSellVolume() {
        return sellVolume;
    }

    public AtomicReference<BigDecimal> getBuyVolume() {
        return buyVolume;
    }
}
