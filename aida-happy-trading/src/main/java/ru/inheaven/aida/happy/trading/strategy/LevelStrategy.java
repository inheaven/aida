package ru.inheaven.aida.happy.trading.strategy;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.*;
import ru.inheaven.aida.happy.trading.util.TorahRandom;
import ru.inheaven.aida.happy.trading.util.TradeUtil;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Date;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CLOSED;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.OPEN;
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
    private final static BigDecimal BD_1_33 = new BigDecimal("1.33");

    private final static BigDecimal BD_2 = new BigDecimal(2);
    private final static BigDecimal BD_2_14 = new BigDecimal(Math.PI - 1);
    private final static BigDecimal BD_2_5 = new BigDecimal("2.5");
    private final static BigDecimal BD_3 = new BigDecimal(3);
    private final static BigDecimal BD_3_5 = new BigDecimal("3.5");
    private final static BigDecimal BD_4 = new BigDecimal(4);
    private final static BigDecimal BD_5 = new BigDecimal(5);
    private final static BigDecimal BD_6 = new BigDecimal(6);
    private final static BigDecimal BD_8_45 = new BigDecimal("8.45");
    private final static BigDecimal BD_11 = new BigDecimal(11);
    private final static BigDecimal BD_12 = new BigDecimal(12);

    private final static BigDecimal BD_TWO_PI = BigDecimal.valueOf(2*Math.PI);
    private final static BigDecimal BD_SQRT_TWO_PI = new BigDecimal("2.506628274631000502415765284811");
    private final static BigDecimal BD_TWO_SQRT_PI = new BigDecimal("3.5449077018110320545963349666823");

    private final static BigDecimal BD_PI = new BigDecimal(Math.PI);
    private Deque<Double> spreadPrices = new ConcurrentLinkedDeque<>();

    private AtomicBoolean maxProfit = new AtomicBoolean();

    private StrategyService strategyService;

    private VSSAService vssaService;

    private Deque<BigDecimal> actionPrices = new ConcurrentLinkedDeque<>();

    private BigDecimal sideSpread;

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

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()-> {
            try {
                while (true){
                    BigDecimal price =  actionPrices.poll();

                    if (price == null){
                        break;
                    }else {
                        actionLevel(price);
                    }
                }
            } catch (Exception e) {
                log.error("error action level executor", e);

                throw e;
            }
        }, 5000, 20, TimeUnit.MILLISECONDS);

        vssaService = new VSSAService(strategy.getSymbol(), null, 0.5, 22, 52, 365, 12, 4, 1000);

        Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors()).scheduleWithFixedDelay(() -> {
            try {
                if (vssaService.isLoaded()) {
                    vssaService.fit();
                }
            } catch (Throwable e) {
                log.error("vssaService ", e);
            }
        }, 0, 1, TimeUnit.HOURS);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                //stddev
                stdDev.set(standardDeviation.evaluate(Doubles.toArray(spreadPrices)));
            } catch (Exception e) {
                stdDev.set(0);

                log.error("error stdDev", e);
            }
        }, 5, 1, TimeUnit.SECONDS);

        sideSpread = strategy.getLevelSideSpread();

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                sideSpread = strategy.getLevelSideSpread().multiply(BigDecimal.valueOf(random.nextDouble() + 1));

            } catch (Exception e) {
                sideSpread = strategy.getLevelSideSpread();

                log.error("error sideSpread", e);
            }
        }, 0, 1, TimeUnit.HOURS);
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
            double sideSpread = getSideSpread(price).doubleValue();

            getOrderMap().get(price.doubleValue() + sideSpread, BID, false).forEach((k,v) -> {
                v.values().forEach(o -> {
                    if (o.getStatus().equals(OPEN) && time.getTime() - o.getOpen().getTime() > 1000){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());

                        onOrder(o);
//                        log.info("{} CLOSE by market {} {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType(), time.getTime() - o.getOpen().getTime());
                    }
                });
            });

            getOrderMap().get(price.doubleValue() - sideSpread, ASK, false).forEach((k,v) -> {
                v.values().forEach(o -> {
                    if (o.getStatus().equals(OPEN) && time.getTime() - o.getOpen().getTime() > 1000){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());

                        onOrder(o);
                        //log.info("{} CLOSE by market {} {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType(), time.getTime() - o.getOpen().getTime());
                    }
                });
            });
        } catch (Exception e) {
            log.error("error close by market", e);
        }
    }

    private void actionLevel(BigDecimal price){
        actionLevel("schedule", price, null);
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

            lastAction.set(price);

            action(key, price, orderType, 0);
        } catch (Exception e) {
            log.error("error actionLevel", e);
        }
    }

    private AtomicLong lastBalanceTime = new AtomicLong(System.currentTimeMillis());
    private AtomicBoolean balance = new AtomicBoolean(true);

    protected boolean getSpotBalance(){
        if (System.currentTimeMillis() - lastBalanceTime.get() >= window.get()){
            String[] symbol = strategy.getSymbol().split("/");
            BigDecimal subtotalBtc = userInfoService.getVolume("subtotal", strategy.getAccount().getId(), symbol[0]);
            BigDecimal subtotalCny = userInfoService.getVolume("subtotal", strategy.getAccount().getId(), symbol[1]);

            BigDecimal price = lastAvgPrice.get().compareTo(ZERO) > 0 ? lastAvgPrice.get() : lastTrade.get();

            if (subtotalBtc.compareTo(ZERO) > 0 && price.compareTo(ZERO) > 0) {
                balance.set(subtotalCny.divide(subtotalBtc.multiply(price), 8, HALF_EVEN).compareTo(BD_2_14) > 0);
            }

            lastBalanceTime.set(System.currentTimeMillis());
        }

        return balance.get();
    }

    @Override
    protected double getForecast() {
        return vssaService.getForecast();
    }

    @Override
    protected BigDecimal getAvgPrice() {
        return lastAvgPrice.get();
    }

    @Override
    protected Long getWindow() {
        return window.get();
    }

    private StandardDeviation standardDeviation = new StandardDeviation(true);
    private AtomicDouble stdDev = new AtomicDouble(0);

    protected BigDecimal getStdDev(){
        return BigDecimal.valueOf(stdDev.get());
    }

    private BigDecimal spreadDiv = BigDecimal.valueOf(Math.sqrt(Math.PI*5));

    protected BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;
        BigDecimal sideSpread = getSideSpread(price);

        if (strategy.getSymbol().equals("BTC/CNY") || strategy.getSymbol().equals("LTC/CNY")){
//            BigDecimal stdDev = getStdDev();
//
//            if (stdDev != null){
//                spread = stdDev.divide(spreadDiv, 8, HALF_EVEN);
//            }
            spread = getDSpread(price);
        }else {
            spread = strategy.getSymbolType() == null
                    ? strategy.getLevelSpread().multiply(price)
                    : strategy.getLevelSpread();
        }

        return spread.compareTo(sideSpread) > 0 ? spread : sideSpread;
    }



    private BigDecimal getSideSpread(BigDecimal price){
        BigDecimal sp = strategy.getSymbolType() == null ? sideSpread.multiply(price) : sideSpread;

        return sp.compareTo(getStep()) > 0 ? sp : getStep();
    }

    private BigDecimal getDSpread(BigDecimal price){
        BigDecimal total = userInfoService.getVolume("total", strategy.getAccount().getId(), null);

        if (total != null && total.compareTo(ZERO) > 0 && price != null && price.compareTo(ZERO) > 0){
            return BigDecimal.valueOf(stdDev.get()*2*Math.PI)
                    .multiply(strategy.getLevelLot())
                    .multiply(price)
                    .divide(total, 8, HALF_EVEN);
        }

        return getSideSpread(price);
    }

    private final static int[] prime = {101, 103, 107, 109, 113, 127, 131, 137, 139, 149, 151, 157, 163, 167, 173, 179,
            181, 191, 193, 197, 199, 211, 223, 227, 229, 233, 239, 241, 251, 257, 263, 269, 271, 277, 281, 283, 293, 307,
            311, 313, 317, 331, 337, 347, 349, 353, 359, 367, 373, 379, 383, 389, 397, 401, 409, 419, 421, 431, 433, 439,
            443, 449, 457, 461, 463, 467, 479, 487, 491, 499, 503, 509, 521, 523, 541, 547, 557, 563, 569, 571, 577, 587,
            593, 599, 601, 607, 613, 617, 619, 631, 641, 643, 647, 653, 659, 661, 673, 677, 683, 691, 701, 709, 719, 727,
            733, 739, 743, 751, 757, 761, 769, 773, 787, 797, 809, 811, 821, 823, 827, 829, 839, 853, 857, 859, 863, 877,
            881, 883, 887, 907, 911, 919, 929, 937, 941, 947, 953, 967, 971, 977, 983, 991, 997};


    private AtomicReference<BigDecimal> lastBuyPrice = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> lastSellPrice = new AtomicReference<>(ZERO);

    private AtomicLong index = new AtomicLong(0);

    private void action(String key, BigDecimal price, OrderType orderType, int priceLevel) {
        try {
            double forecast = getForecast();

            boolean balance = getSpotBalance();

            BigDecimal spread = scale(getSpread(price));

            BigDecimal priceF = scale(forecast > 0 ? price.add(getStep()) : price.subtract(getStep()));

            BigDecimal buyPrice = scale(forecast > 0 ? priceF : priceF.subtract(spread));
            BigDecimal sellPrice = scale(forecast > 0 ? priceF.add(spread) : priceF);

            if (!getOrderMap().contains(buyPrice, spread, BID) && !getOrderMap().contains(sellPrice, spread, ASK)){
                double q1 = TorahRandom.nextDouble();
                double q2 = TorahRandom.nextDouble();
                double max = Math.max(q1, q2);
                double min = Math.min(q1, q2);

                //shuffle
                max = max * (random.nextDouble()/33 + 1);
                min = min * (random.nextDouble()/33 + 1);

//                if (forecast > 0 == balance){
//                    double abs = Math.abs(forecast);
//
//                    if (abs > 7){
//                        max *= Math.PI;
//                        min *= Math.PI;
//                    }else if (abs > 3){
//                        max *= 2;
//                        min *= 2;
//                    }
//                }

                //                double max = random.nextGaussian()/2 + 2;
//                double min = random.nextGaussian()/2 + 1;

//                double q1 = Math.sin(index.get()/(2*Math.PI)) + 1.07;
//                double q2 = Math.cos(index.get()/(2*Math.PI)) + 1.07;
//
//                double max = Math.max(q1, q2);
//                double min = Math.min(q1, q2);

                log.info("{} "  + key + " {} {} {} {}", strategy.getId(), price.setScale(3, HALF_EVEN), spread, min, max);

                BigDecimal buyAmount = strategy.getLevelLot().multiply(BigDecimal.valueOf(balance ? max : min));
                BigDecimal sellAmount = strategy.getLevelLot().multiply(BigDecimal.valueOf(balance ? min : max));

                //momentum
//                if (getForecast() < -5){
//                    buyAmount = buyAmount.divide(BD_PI, 8, HALF_EVEN);
//                }
//                if (getForecast() > 5){
//                    sellAmount = sellAmount.divide(BD_PI, 8, HALF_EVEN);
//                }

                //less
                if (buyAmount.compareTo(BD_0_01) < 0 || getForecast() < -9){
                    buyAmount = BD_0_01;
                }
                if (sellAmount.compareTo(BD_0_01) < 0 || getForecast() > 9){
                    sellAmount = BD_0_01;
                }

                Long positionId = positionIdGen.incrementAndGet();
                Order buyOrder = new Order(strategy, positionId, BID, buyPrice, buyAmount.setScale(3, HALF_UP));
                Order sellOrder = new Order(strategy, positionId, ASK, sellPrice, sellAmount.setScale(3, HALF_UP));

                buyOrder.setSpread(spread);
                buyOrder.setForecast(forecast);
                buyOrder.setBalance(balance);

                sellOrder.setSpread(spread);
                sellOrder.setForecast(forecast);
                sellOrder.setBalance(balance);

                BigDecimal freeBtc = userInfoService.getVolume("free", strategy.getAccount().getId(), "BTC");
                BigDecimal freeCny = userInfoService.getVolume("free", strategy.getAccount().getId(), "CNY");

                if (q1 > q2 == buyAmount.compareTo(sellAmount) > 0){
                    if (freeCny.compareTo(buyAmount.multiply(buyPrice).multiply(BD_2)) > 0){
                        createOrderSync(buyOrder);
                    }

                    if (freeBtc.compareTo(sellAmount.multiply(BD_2)) > 0){
                        createOrderSync(sellOrder);
                    }

                }

                lastBuyPrice.set(buyPrice);
                lastSellPrice.set(sellPrice);
            }
        } catch (Exception e) {
            log.error("action error -> ", e);
        }
    }

    private AtomicReference<BigDecimal> lastAvgPrice = new AtomicReference<>(ZERO);

    private AtomicLong window = new AtomicLong(1000);

    private PublishSubject<Trade> tradeBuffer = PublishSubject.create();

    {
//        tradeBuffer.buffer(55).filter(b -> !b.isEmpty()).forEach(b -> {
//            try {
//                prices.add(TradeUtil.avg(b));
//            } catch (Exception e) {
//                log.error("error buffer 1", e);
//            }
//        });

        tradeBuffer.buffer(1460, 4).filter(b -> !b.isEmpty()).forEach(b -> {
            try {
                lastAvgPrice.set(TradeUtil.avg(b));
            } catch (Exception e) {
                log.error("error buffer 10", e);
            }
        });

//        tradeBuffer.buffer(3000, 50)
//                .forEach(l -> {
//                    try {
//                        if (!l.isEmpty()) {
//                            Trade max = l.get(0);
//                            Trade min = l.get(0);
//
//                            for (Trade t : l){
//                                if (t.getPrice().compareTo(max.getPrice()) > 0){
//                                    max = t;
//                                }else if (t.getPrice().compareTo(min.getPrice()) < 0){
//                                    min = t;
//                                }
//                            }
//
//                            window.set(Math.abs(max.getCreated().getTime() - min.getCreated().getTime()));
//                        }
//                    } catch (Exception e) {
//                        log.error("error buffer 60", e);
//                    }
//                });
    }

    private AtomicReference<BigDecimal> lastTrade = new AtomicReference<>(ZERO);

    private AtomicReference<BigDecimal> tradeBid = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> tradeAsk = new AtomicReference<>(ZERO);

    private Executor executorVSSA = Executors.newSingleThreadExecutor();

    @Override
    protected void onTrade(Trade trade) {
        index.incrementAndGet();

        try {
            (trade.getOrderType().equals(BID) ? tradeBid : tradeAsk).set(trade.getPrice());

            if (lastTrade.get().compareTo(ZERO) != 0 && lastTrade.get().subtract(trade.getPrice()).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(BD_0_01) < 0){
                actionPrices.add(trade.getPrice());

                tradeBuffer.onNext(trade);

                executorVSSA.execute(() -> vssaService.add(trade));

                closeByMarketAsync(trade.getPrice(), trade.getOrigTime());
            }else{
                log.warn("trade price diff 1% {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
            }

            //spread
            spreadPrices.add(trade.getPrice().doubleValue());
            if (spreadPrices.size() > 1460){
                spreadPrices.removeFirst();
            }

            lastTrade.set(trade.getPrice());
        } catch (Exception e) {
            log.error("error onTrade", e);
        }
    }

    private AtomicReference<BigDecimal> depthSpread = new AtomicReference<>(BD_0_25);

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();

        if (ask != null && bid != null && lastTrade.get().compareTo(ZERO) != 0 &&
                lastTrade.get().subtract(ask).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(BD_0_01) < 0 &&
                lastTrade.get().subtract(bid).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(BD_0_01) < 0) {
            depthSpread.set(ask.subtract(bid).abs());

            if (getForecast() > 0){
                actionPrices.add(ask);
            }else if (getForecast() < 0){
                actionPrices.add(bid);
            }
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            actionPrices.add(order.getAvgPrice());
        }
    }

    public static void main(String... args){
        Observable.range(0, 100).buffer(55, 1).forEach(l -> System.out.println(Arrays.toString(l.toArray())));
    }
}

