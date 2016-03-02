package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.*;
import ru.inheaven.aida.happy.trading.util.QuranRandom;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;
import static ru.inheaven.aida.happy.trading.entity.OrderType.*;

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
    private final static BigDecimal BD_0_25 = new BigDecimal("0.25");
    private final static BigDecimal BD_0_33 = new BigDecimal("0.33");
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

    private final static BigDecimal BD_TWO_PI = BigDecimal.valueOf(2*Math.PI);
    private final static BigDecimal BD_SQRT_TWO_PI = new BigDecimal("2.506628274631000502415765284811");
    private final static BigDecimal BD_PI = new BigDecimal(Math.PI);

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService,  XChangeService xChangeService) {
        super(strategy, orderService, orderMapper, tradeService, depthService, xChangeService);

        this.strategy = strategy;
        this.userInfoService = userInfoService;
        this.tradeService = tradeService;
        this.orderService = orderService;

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
                        log.info("{} CLOSED by market {} {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType(), time.getTime() - o.getOpen().getTime());
                    }
                });
            });

            getOrderMap().get(price.subtract(getSideSpread(price)), ASK, false).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(OPEN) && time.getTime() - o.getOpen().getTime() > 0){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());
                        log.info("{} CLOSED by market {} {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType(), time.getTime() - o.getOpen().getTime());
                    }
                });
            });
        } catch (Exception e) {
            log.error("error close by market", e);
        }
    }

    private Executor executor2 = Executors.newCachedThreadPool();
    private AtomicReference<BigDecimal> lastAction = new AtomicReference<>(ZERO);
    private Deque<BigDecimal> actionDeque = new ConcurrentLinkedDeque<>();

    private void actionAsync(String key, BigDecimal price, OrderType orderType){
        if (lastAction.get().compareTo(price) != 0) {
            lastAction.set(price);
            actionDeque.push(price);

            executor2.execute(() -> action(key, price, orderType));
        }
    }

    private Semaphore semaphore = new Semaphore(1);

    private void action(String key, BigDecimal price, OrderType orderType){
        try {
            semaphore.acquire();

            while (!actionDeque.isEmpty()) {
                if (isVol() || (!strategy.isLevelInverse() && !isMinSpot()) || (strategy.isLevelInverse() && !isMaxSpot())) {
                    actionLevel(key, actionDeque.pop(), orderType);
//                    pushOrders(dequePrice);
                }
            }
        } catch (Exception e) {
            log.error("error action level", e);
        } finally {
            semaphore.release();
        }
    }

    private void actionLevel(String key, BigDecimal price, OrderType orderType){
        if (isBidRefused() || isAskRefused()) {
            return;
        }

        BigDecimal sideSpread = getSideSpread(price);

        for (int i = strategy.getLevelSize().intValue(); i > 0 ; i--) {
            action(key, price.add(sideSpread), orderType, i);
            action(key, price.subtract(sideSpread), orderType, -i);
        }

        action(key, price, orderType, 0);
    }

    private static final BigDecimal CNY_MIN = BigDecimal.valueOf(0);
    private static final BigDecimal USD_MIN = BigDecimal.valueOf(0);

    @SuppressWarnings("Duplicates")
    private boolean isMinSpot(){
        BigDecimal volume = userInfoService.getVolume("subtotal_spot", strategy.getAccount().getId(), null);

        if (volume != null){
            if (strategy.getSymbol().contains("CNY")){
                return volume.compareTo(CNY_MIN) < 0;
            }else if (strategy.getSymbol().contains("USD")){
                return volume.compareTo(USD_MIN) < 0;
            }
        }

        return true;
    }

    private static final BigDecimal CNY_MAX = BigDecimal.valueOf(100000);
    private static final BigDecimal USD_MAX = BigDecimal.valueOf(10000);

    @SuppressWarnings("Duplicates")
    private boolean isMaxSpot(){
        BigDecimal volume = userInfoService.getVolume("subtotal_spot", strategy.getAccount().getId(), null);

        if (volume != null){
            if (strategy.getSymbol().contains("CNY")){
                return volume.compareTo(CNY_MAX) > 0;
            }else if (strategy.getSymbol().contains("USD")){
                return volume.compareTo(USD_MAX) > 0;
            }
        }

        return true;
    }

    private static final BigDecimal CNY_MIDDLE = BigDecimal.valueOf(3000);
    private static final BigDecimal USD_MIDDLE = BigDecimal.valueOf(1000);

    protected BigDecimal getSpotBalance(){
        String[] symbol = strategy.getSymbol().split("/");

        BigDecimal subtotal = userInfoService.getVolume("subtotal", strategy.getAccount().getId(), symbol[0]);
        BigDecimal spot = userInfoService.getVolume("subtotal", strategy.getAccount().getId(), symbol[1]);
        BigDecimal total = userInfoService.getVolume("total", strategy.getAccount().getId(), null);
        BigDecimal net = userInfoService.getVolume("net", strategy.getAccount().getId(), null);

        if (lastAction.get().equals(ZERO) || subtotal.equals(ZERO) || total.equals(ZERO) || net.equals(ZERO) || spot.equals(ZERO)){
            return ZERO;
        }

        return spot.divide(subtotal.multiply(lastAction.get()), HALF_EVEN).subtract(BD_2_5);
    }

    private BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;
        BigDecimal sideSpread = getSideSpread(price);

        if (strategy.getSymbol().equals("BTC/CNY") || strategy.getSymbol().equals("LTC/CNY")){
            BigDecimal stdDev = tradeService.getStdDev(strategy.getSymbol(), "_1");

            if (stdDev != null){
                BigDecimal d = ONE;

                switch (getVolSuffix()){
                    case "_1":
                        d = BD_1_1;
                        break;
                    case "_2":
                        d = BD_1_5;
                        break;
                    case "_3":
                        d = BD_2;
                        break;
                    case "_4":
                        d = BD_SQRT_TWO_PI;
                        break;
                    case "_5":
                        d = BD_3;
                        break;
                    case "_6":
                        d = BD_3_5;
                        break;
                    case "_7":
                        d = BD_4;
                        break;
                }

                spread = stdDev.divide(d, HALF_EVEN);
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

    private AtomicLong actionTime = new AtomicLong(System.currentTimeMillis());

    private void action(String key, BigDecimal price, OrderType orderType, int priceLevel) {
        try {
            boolean up = getSpotBalance().compareTo(ZERO)> 0;

            BigDecimal spread = scale(getSpread(price));
            BigDecimal priceF = up ? price.add(getStep()) : price.subtract(getStep());
            BigDecimal sideSpread = isVol() ? spread : scale(getSideSpread(priceF));

            BigDecimal buyPrice = up ? priceF : priceF.subtract(spread);
            BigDecimal sellPrice = up ? priceF.add(spread) : priceF;

            if (!getOrderMap().contains(buyPrice, sideSpread, BID) && !getOrderMap().contains(sellPrice, sideSpread, ASK)){
//               //rate
//                if (System.currentTimeMillis() - actionTime.get() < 10){
//                    return;
//                }
//                actionTime.set(System.currentTimeMillis());

                log.info("{} "  + key + " {} {} {} {} {} {}", strategy.getId(), price.setScale(3, HALF_EVEN), orderType,
                        sideSpread, spread, isMinSpot(), isMaxSpot());


                if (risk.compareTo(ONE) > 0){
                    log.warn("RISK RATE {} {} {}", risk.setScale(3, HALF_EVEN), strategy.getSymbol(),
                            Objects.toString(strategy.getSymbolType(), ""));
                }

                BigDecimal amount = strategy.getLevelLot();

                //avg amount
//                BigDecimal avgAmount = tradeService.getAvgAmount(strategy.getSymbol() + getVolSuffix());
//                if (avgAmount.compareTo(amount) > 0){
//                    BigDecimal x = amount.multiply(BD_3);
//
//                    if (avgAmount.compareTo(x) > 0){
//                        amount = x;
//                    }else{
//                        amount = avgAmount;
//                    }
//                }

                Long positionId = positionIdGen.incrementAndGet();

                BigDecimal buyAmount = amount;
                BigDecimal sellAmount = amount;

                if (strategy.getSymbolType() == null){
                    buyAmount = amount;
                    sellAmount = amount;
                }

                double ra = QuranRandom.nextDouble();
                double rb = QuranRandom.nextDouble();
                double rMax = ra > rb ? ra : rb;
                double rMin = ra > rb ? rb : ra;

                double a = amount.doubleValue();
                double rBuy = a * (up ? rMax : rMin);
                double rSell = a * (up ? rMin : rMax);

                if (isVol()){
                    buyAmount = BigDecimal.valueOf(rBuy).setScale(3, HALF_EVEN);
                    sellAmount = BigDecimal.valueOf(rSell).setScale(3, HALF_EVEN);

                    if (buyAmount.compareTo(BD_0_01) < 0){
                        buyAmount = BD_0_01;
                    }
                    if (sellAmount.compareTo(BD_0_01) < 0){
                        sellAmount = BD_0_01;
                    }
                }

                Order buyOrder = new Order(strategy, positionId, BID, buyPrice, buyAmount);
                Order sellOrder = new Order(strategy, positionId, ASK, sellPrice, sellAmount);

                if (isVol()) {
                    if ((ra > rb && rBuy > rSell) || (ra < rb && rBuy < rSell)){
                        createOrderSync(buyOrder);
                        createOrderSync(sellOrder);
                    }else {
                        createOrderSync(sellOrder);
                        createOrderSync(buyOrder);
                    }
                }else {
                    if (strategy.isLevelInverse()){
                        if (priceLevel > strategy.getLevelSize().intValue()/2){
                            sellOrder.setAmount(sellOrder.getAmount().multiply(BD_2));
                        }

                        createOrderSync(sellOrder);
                        createWaitOrder(buyOrder);
                    }else {
                        if (priceLevel > strategy.getLevelSize().intValue()/2){
                            buyOrder.setAmount(buyOrder.getAmount().multiply(BD_2));
                        }

                        createOrderSync(buyOrder);
                        createWaitOrder(sellOrder);
                    }
                }
            }
        } catch (Exception e) {
            log.error("action error -> ", e);
        }
    }

    private BigDecimal lastTrade = ZERO;

    @Override
    protected void onTrade(Trade trade) {
        if (lastTrade.compareTo(ZERO) != 0 &&
                lastTrade.subtract(trade.getPrice()).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(BD_0_01) < 0){
            actionAsync("TRADE", trade.getPrice(), trade.getOrderType());
            closeByMarketAsync(trade.getPrice(), trade.getOrigTime());
        }else{
            log.warn("trade price diff 1% {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
        }

        lastTrade = trade.getPrice();
    }

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();

        if (ask != null && bid != null && lastTrade.compareTo(ZERO) != 0 &&
                lastTrade.subtract(ask).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(BD_0_01) < 0 &&
                lastTrade.subtract(bid).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(BD_0_01) < 0) {
            actionAsync("DEPTH", ask, ASK);
            actionAsync("DEPTH", bid, BID);
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            actionAsync("REAL", order.getAvgPrice(), order.getType());
        }
    }
}

