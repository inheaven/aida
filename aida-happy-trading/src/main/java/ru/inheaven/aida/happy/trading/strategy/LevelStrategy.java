package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.*;
import ru.inheaven.aida.happy.trading.util.QuranRandom;
import rx.subjects.PublishSubject;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;
import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 08.07.2015 19:39.
 */
public class LevelStrategy extends BaseStrategy{
    private Logger log = LoggerFactory.getLogger(getClass());

    private Strategy strategy;
    private BigDecimal risk = ONE;

    private UserInfoService userInfoService;
    private TradeService tradeService;
    private OrderService orderService;

    private SecureRandom random = new SecureRandom();

    private static AtomicLong positionIdGen = new AtomicLong(System.nanoTime());

    private final static BigDecimal BD_0_1 = new BigDecimal("0.1");
    private final static BigDecimal BD_0_5 = new BigDecimal("0.5");
    private final static BigDecimal BD_0_8 = new BigDecimal("0.8");
    private final static BigDecimal BD_0_25 = new BigDecimal("0.25");
    private final static BigDecimal BD_0_33 = new BigDecimal("0.33");
    private final static BigDecimal BD_0_66 = new BigDecimal("0.66");
    private final static BigDecimal BD_0_01 = new BigDecimal("0.01");
    private final static BigDecimal BD_0_05 = new BigDecimal("0.05");
    private final static BigDecimal BD_0_001 = new BigDecimal("0.001");
    private final static BigDecimal BD_0_002 = new BigDecimal("0.002");
    private final static BigDecimal BD_1_1 = new BigDecimal("1.1");
    private final static BigDecimal BD_1_2 = new BigDecimal("1.2");
    private final static BigDecimal BD_1_3 = new BigDecimal("1.3");

    private final static BigDecimal BD_1_5 = new BigDecimal(1.5);
    private final static BigDecimal BD_1_7 = new BigDecimal(1.7);
    private final static BigDecimal BD_2 = new BigDecimal(2);
    private final static BigDecimal BD_2_3 = new BigDecimal(2.3);
    private final static BigDecimal BD_2_5 = new BigDecimal(2.5);
    private final static BigDecimal BD_3 = new BigDecimal(3);
    private final static BigDecimal BD_3_5 = new BigDecimal(3.5);
    private final static BigDecimal BD_4 = new BigDecimal(4);
    private final static BigDecimal BD_5 = new BigDecimal(5);
    private final static BigDecimal BD_6 = new BigDecimal(6);

    private final static BigDecimal BD_TWO_PI = BigDecimal.valueOf(2*Math.PI);
    private final static BigDecimal BD_SQRT_TWO_PI = new BigDecimal("2.506628274631000502415765284811");
    private final static BigDecimal BD_TWO_SQRT_PI = new BigDecimal("3.5449077018110320545963349666823");

    private final static BigDecimal BD_PI = new BigDecimal(Math.PI);

    private PublishSubject<BigDecimal> action = PublishSubject.create();

    private AtomicReference<BigDecimal> lastPrice = new AtomicReference<>(ZERO);

    private StrategyService strategyService;

    public LevelStrategy(StrategyService strategyService, Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService,  XChangeService xChangeService) {
        super(strategy, orderService, orderMapper, tradeService, depthService, xChangeService);

        this.strategy = strategy;
        this.userInfoService = userInfoService;
        this.tradeService = tradeService;
        this.orderService = orderService;

        this.strategyService = strategyService;

        if (strategy.getSymbolType() != null) {
            userInfoService.createUserInfoObservable(strategy.getAccount().getId(), strategy.getSymbol().substring(0, 3))
                    .filter(u -> u.getRiskRate() != null)
                    .subscribe(u -> {
                        if (u.getRiskRate().compareTo(BigDecimal.valueOf(10)) < 0){
                            risk = TEN.divide(u.getRiskRate(), 8, HALF_EVEN).pow(3).setScale(2, HALF_EVEN);
                        }else {
                            risk = ONE;
                        }
                    });
        }

        action.distinctUntilChanged().onBackpressureBuffer().subscribe(this::actionLevel);

//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()-> actionLevel("schedule", lastPrice.get(), null), 5000, 10, TimeUnit.MILLISECONDS);
    }

    private void pushOrders(BigDecimal price){
        BigDecimal spread = ZERO;
        boolean inverse = strategy.isLevelInverse();

        if (!isBidRefused()){
            getOrderMap().get(price.subtract(spread), BID).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(WAIT) && (isVol() || !inverse || !getOrderMap().containsAsk(o.getPositionId()))){
                        pushWaitOrder(o);
                    }
                });
            });
        }

        if (!isAskRefused()){
            getOrderMap().get(price.add(spread), ASK).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(WAIT) && (isVol() || inverse || !getOrderMap().containsBid(o.getPositionId()))){
                        pushWaitOrder(o);
                    }
                });
            });
        }
    }

    private Executor executor = Executors.newCachedThreadPool();
    private AtomicReference<BigDecimal> lastMarket = new AtomicReference<>(ZERO);

    private void closeByMarketAsync(BigDecimal price, Date time){
        if (lastMarket.get().compareTo(price) != 0) {
            executor.execute(() -> closeByMarket(price, time));
            lastMarket.set(price);
        }
    }

    @SuppressWarnings("Duplicates")
    private void closeByMarket(BigDecimal price, Date time){
        try {
            getOrderMap().get(price.add(getSideSpread(price)), BID, false).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(OPEN) && time.getTime() - o.getOpen().getTime() > 0){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());
                        log.info("{} CLOSE by market {} {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType(), time.getTime() - o.getOpen().getTime());
                    }
                });
            });

            getOrderMap().get(price.subtract(getSideSpread(price)), ASK, false).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(OPEN) && time.getTime() - o.getOpen().getTime() > 0){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());
                        log.info("{} CLOSE by market {} {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType(), time.getTime() - o.getOpen().getTime());
                    }
                });
            });
        } catch (Exception e) {
            log.error("error close by market", e);
        }
    }

    private void actionLevel(BigDecimal price){
        actionLevel("subject", price, null);
    }

    private AtomicReference<BigDecimal> lastAction = new AtomicReference<>(ZERO);

    private void actionLevel(String key, BigDecimal price, OrderType orderType){
        try {
            if (isBidRefused() || isAskRefused()) {
                return;
            }

            if (lastAction.get().equals(price)){
                return;
            }

            action(key, price, orderType, 0);

            BigDecimal spread = getSpread(price);
            for (int i = 1; i < 11; ++i){
                if (strategy.isLevelInverse()){
                    action(key, price.add(spread.multiply(BigDecimal.valueOf(i))), orderType, i);
                }else {
                    action(key, price.subtract(spread.multiply(BigDecimal.valueOf(i))), orderType, -i);
                }
            }

            lastAction.set(price);
        } catch (Exception e) {
            log.error("error actionLevel", e);
        }
    }

    protected boolean getSpotBalance(){
        String[] symbol = strategy.getSymbol().split("/");

        BigDecimal freeBtc = userInfoService.getVolume("free", strategy.getAccount().getId(), symbol[0]);
        BigDecimal freeCny = userInfoService.getVolume("free", strategy.getAccount().getId(), symbol[1]);

        BigDecimal ask = ZERO;
        BigDecimal bid = ZERO;

        for (BaseStrategy strategy : strategyService.getBaseStrategies()) {
            for (Collection<Order> orders : strategy.getOrderMap().getAskMap().values()){
                for (Order o : orders){
                    if (OPEN.equals(o.getStatus())){
                        ask = ask.add(o.getAmount().multiply(o.getPrice()));
                    }
                }
            }

            for (Collection<Order> orders : strategy.getOrderMap().getBidMap().values()){
                for (Order o : orders){
                    if (OPEN.equals(o.getStatus())){
                        bid = bid.add(o.getAmount().multiply(o.getPrice()));
                    }
                }
            }
        }

        return freeCny.add(bid).compareTo(ask.add(freeBtc.multiply(lastAction.get()))) > 0;
    }

    private AtomicReference<BigDecimal> lastCloseOrderPrice = new AtomicReference<>(ZERO);

    @Override
    protected void onCloseOrder(Order order) {
        BigDecimal freeBtc = userInfoService.getVolume("free", strategy.getAccount().getId(), "BTC");
        BigDecimal freeCny = userInfoService.getVolume("free", strategy.getAccount().getId(), "CNY");

        BigDecimal price = order.getAvgPrice() != null ? order.getAvgPrice() : order.getPrice();

        if (BID.equals(order.getType())){
            if (CLOSED.equals(order.getStatus())){
                freeBtc = freeBtc.add(order.getAmount());
            }else if (CANCELED.equals(order.getStatus())){
                freeCny = freeCny.add(order.getAmount().multiply(price));
            }
        }else if (ASK.equals(order.getType())){
            if (CLOSED.equals(order.getStatus())){
                freeCny = freeCny.add(order.getAmount().multiply(price));
            }else if (CANCELED.equals(order.getStatus())){
                freeBtc = freeBtc.add(order.getAmount());
            }
        }

        userInfoService.setVolume("free", strategy.getAccount().getId(), "BTC", freeBtc);
        userInfoService.setVolume("free", strategy.getAccount().getId(), "CNY", freeCny);

        lastCloseOrderPrice.set(price);
    }

    private boolean isRange(){
        String[] symbol = strategy.getSymbol().split("/");

        BigDecimal spot = userInfoService.getVolume("subtotal", strategy.getAccount().getId(), symbol[1]);

        BigDecimal middle = userInfoService.getVolume("total", strategy.getAccount().getId(), null).divide(BD_2, HALF_UP);

        return middle.subtract(spot).abs().compareTo(middle.multiply(BD_0_66)) < 0;
    }

    private BigDecimal getStdDev(){
        return strategy.isLevelInverse() ? tradeService.getValue("ask") : tradeService.getValue("bid");
    }

    protected BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;
        BigDecimal sideSpread = getSideSpread(price);

        if (strategy.getSymbol().equals("BTC/CNY") || strategy.getSymbol().equals("LTC/CNY")){
            BigDecimal stdDev = getStdDev();

            if (stdDev != null){
                spread = stdDev.divide(BD_5, HALF_UP);
            }
        }else {
            spread = strategy.getSymbolType() == null
                    ? strategy.getLevelSpread().multiply(price)
                    : strategy.getLevelSpread();
        }

        return spread.compareTo(sideSpread) > 0 ? spread : sideSpread;
    }


    private BigDecimal getSideSpread(BigDecimal price){
        BigDecimal sideSpread = strategy.getSymbolType() == null
                ? strategy.getLevelSideSpread().multiply(price)
                : strategy.getLevelSideSpread();

        return sideSpread.compareTo(getStep()) > 0 ? sideSpread : getStep();
    }

    private final static int[] prime = {101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
            181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307,
            311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439,
            443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563, 569, 571, 577, 587,
            593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727,
            733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877,
            881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997};

    private void action(String key, BigDecimal price, OrderType orderType, int priceLevel) {
        try {
            boolean up = getSpotBalance();

            BigDecimal spread = scale(getSpread(price));

            boolean close = price.compareTo(lastCloseOrderPrice.get()) > 0;

            BigDecimal buyPrice = close ? price : price.subtract(spread);
            BigDecimal sellPrice = close ? price.add(spread) : price;

            if (!getOrderMap().contains(buyPrice, spread, BID, lastTrade.get()) &&
                    !getOrderMap().contains(sellPrice, spread, ASK, lastTrade.get())){
                log.info("{} "  + key + " {} {} {}", strategy.getId(), price.setScale(3, HALF_EVEN), orderType, spread);

                BigDecimal total = userInfoService.getVolume("total", strategy.getAccount().getId(), null).setScale(8, HALF_UP);
                BigDecimal amount = total.divide(price, 8, HALF_UP)
                        .divide(getStdDev().multiply(TEN).divide(getSpread(price), 8, HALF_UP), 8, HALF_UP);

                BigDecimal buyAmount = up
                        ? amount.multiply(BigDecimal.valueOf(QuranRandom.nextDouble()))
                        : BD_0_01;

                BigDecimal sellAmount = up
                        ? BD_0_01
                        : amount.multiply(BigDecimal.valueOf(QuranRandom.nextDouble()));

                if (buyAmount.compareTo(BD_0_01) < 0){
                    buyAmount = BD_0_01;
                }
                if (sellAmount.compareTo(BD_0_01) < 0){
                    sellAmount = BD_0_01;
                }

                Long positionId = positionIdGen.incrementAndGet();
                Order buyOrder = new Order(strategy, positionId, BID, buyPrice, buyAmount.setScale(3, HALF_UP));
                Order sellOrder = new Order(strategy, positionId, ASK, sellPrice, sellAmount.setScale(3, HALF_UP));

                buyOrder.setSpread(spread);
                sellOrder.setSpread(spread);

                if (!strategy.isLevelInverse()) {
                    createOrderSync(sellOrder);
                    createOrderSync(buyOrder);
                }else {
                    createOrderSync(buyOrder);
                    createOrderSync(sellOrder);
                }
            }
        } catch (Exception e) {
            log.error("action error -> ", e);
        }
    }

    private double nextDouble(){
        double r = random.nextGaussian()/6 + 0.5;
        return r < 0 ? 0 : r > 1 ? 1 : r;
    }

    private AtomicReference<BigDecimal> lastTrade = new AtomicReference<>(ZERO);

    @Override
    protected void onTrade(Trade trade) {
        if (lastTrade.get().compareTo(ZERO) != 0 &&
                lastTrade.get().subtract(trade.getPrice()).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(BD_0_01) < 0){

            if ((!strategy.isLevelInverse() && trade.getOrderType().equals(BID)) || (strategy.isLevelInverse() && trade.getOrderType().equals(ASK))) {
                lastPrice.set(trade.getPrice());
                action.onNext(trade.getPrice());
            }

            //closeByMarketAsync(trade.getPrice(), trade.getOrigTime());
        }else{
            log.warn("trade price diff 1% {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
        }

        lastTrade.set(trade.getPrice());
    }

    private AtomicReference<BigDecimal> depthSpread = new AtomicReference<>(BD_0_25);
    private AtomicReference<BigDecimal> depthBid = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> depthAsk = new AtomicReference<>(ZERO);

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();

        if (ask != null && bid != null && lastTrade.get().compareTo(ZERO) != 0 &&
                lastTrade.get().subtract(ask).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(BD_0_01) < 0 &&
                lastTrade.get().subtract(bid).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(BD_0_01) < 0) {

            if (strategy.isLevelInverse()) {
//                lastPrice.set(ask);
                action.onNext(ask);
            }else {
//                lastPrice.set(bid);
                action.onNext(bid);
            }

            depthSpread.set(ask.subtract(bid).abs());
            depthBid.set(bid);
            depthAsk.set(ask);
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            if ((!strategy.isLevelInverse() && order.getType().equals(BID)) || (strategy.isLevelInverse() && order.getType().equals(ASK))) {
                lastPrice.set(order.getAvgPrice());
                action.onNext(order.getAvgPrice());
            }
        }
    }
}

