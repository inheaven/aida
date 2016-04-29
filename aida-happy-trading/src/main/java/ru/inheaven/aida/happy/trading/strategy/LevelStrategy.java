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
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
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

    //private PublishSubject<BigDecimal> action = PublishSubject.create();

    private AtomicReference<BigDecimal> lastPrice = new AtomicReference<>(ZERO);

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

        //action.distinctUntilChanged().onBackpressureBuffer().subscribe(this::actionLevel);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()-> actionLevel("schedule", lastPrice.get(), null), 5000, 10, TimeUnit.MILLISECONDS);
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
        if (isBidRefused() || isAskRefused()) {
            return;
        }

        if (lastAction.get().equals(price)){
            return;
        }

        action(key, price, orderType, 0);

//        BigDecimal spread = getSpread(price);
//        if (strategy.isLevelInverse()){
//            action(key, price.subtract(spread), orderType, -1);
//        }else {
//            action(key, price.add(spread), orderType, 1);
//        }

        lastAction.set(price);
    }

    protected boolean getSpotBalance(){
        String[] symbol = strategy.getSymbol().split("/");

        BigDecimal subtotal = userInfoService.getVolume("subtotal", strategy.getAccount().getId(), symbol[0]);
        BigDecimal total = userInfoService.getVolume("total", strategy.getAccount().getId(), null);

        return total.divide(subtotal.multiply(lastAction.get()), HALF_EVEN).compareTo(BD_2) > 0;
    }

    private boolean isRange(){
        String[] symbol = strategy.getSymbol().split("/");

        BigDecimal spot = userInfoService.getVolume("subtotal", strategy.getAccount().getId(), symbol[1]);

        BigDecimal middle = userInfoService.getVolume("total", strategy.getAccount().getId(), null).divide(BD_2, HALF_UP);

        return middle.subtract(spot).abs().compareTo(middle.multiply(BD_0_66)) < 0;
    }

    protected BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;
        BigDecimal sideSpread = getSideSpread(price);

        if (strategy.getSymbol().equals("BTC/CNY") || strategy.getSymbol().equals("LTC/CNY")){
            BigDecimal stdDev = strategy.isLevelInverse()
                    ? tradeService.getValue("ask")
                    : tradeService.getValue("bid");

            if (stdDev != null){
                spread = stdDev.divide(BD_SQRT_TWO_PI, HALF_UP);
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

    private AtomicLong meanTime = new AtomicLong(System.currentTimeMillis());

    private void action(String key, BigDecimal price, OrderType orderType, int priceLevel) {
        try {
            boolean up = getSpotBalance();

            //boolean depth = price.subtract(depthAsk.get()).abs().compareTo(price.subtract(depthBid.get()).abs()) > 0;
//           boolean std = tradeService.getValue("ask").compareTo(tradeService.getValue("bid")) > 0;

            BigDecimal spread = scale(getSpread(price));
//            BigDecimal halfSpread = spread.divide(BD_2, HALF_EVEN);
            BigDecimal sideSpread = isVol() ? spread : scale(getSideSpread(price));

            //BigDecimal priceF = up ? price.add(getStep()) : price.subtract(getStep());
//            BigDecimal buyPrice = up ? priceF : priceF.subtract(spread);
//            BigDecimal sellPrice = up ? priceF.add(spread) : priceF;

//            BigDecimal bidSpread = tradeService.getValue("bid").divide(BD_2, HALF_UP);
//            BigDecimal askSpread = tradeService.getValue("ask").divide(BD_2, HALF_UP);
//
//            if (bidSpread.add(askSpread).compareTo(BD_0_1) < 0){
//                bidSpread = BD_0_05;
//                askSpread = BD_0_05;
//            }

            BigDecimal priceF = !strategy.isLevelInverse() ? price.add(BD_0_01) : price.subtract(BD_0_01);
            BigDecimal buyPrice = !strategy.isLevelInverse() ? priceF : priceF.subtract(spread);
            BigDecimal sellPrice = !strategy.isLevelInverse() ? priceF.add(spread) : priceF;

            if (!getOrderMap().contains(buyPrice, sideSpread, BID, lastTrade.get()) &&
                    !getOrderMap().contains(sellPrice, sideSpread, ASK, lastTrade.get())){
                //free
//                if (userInfoService.getVolume("free", strategy.getAccount().getId(), "BTC").compareTo(ONE) < 0){
//                    createOrderAsync(new Order(strategy, System.nanoTime(), BID, lastAction.get().add(spread), strategy.getLevelLot()));
//                }else if (userInfoService.getVolume("free", strategy.getAccount().getId(), "CNY").compareTo(price) < 0){
//                    createOrderAsync(new Order(strategy, System.nanoTime(), ASK, lastAction.get().subtract(spread), strategy.getLevelLot()));
//                }

                log.info("{} "  + key + " {} {} {} {}", strategy.getId(), price.setScale(3, HALF_EVEN), orderType, sideSpread, spread);

                //amount
                BigDecimal amount = strategy.getLevelLot();
                Long positionId = positionIdGen.incrementAndGet();

                BigDecimal buyAmount = amount;
                BigDecimal sellAmount = amount;

                if (strategy.getSymbolType() == null){
                    buyAmount = amount;
                    sellAmount = amount;
                }

                double rBuy;
                double rSell;

                boolean mean = System.currentTimeMillis() - meanTime.get() > 60000;

                //mean
                if (mean && buyPrice.compareTo(getBuyPrice().get()) < 0 &&
                        getSellVolume().get().multiply(getSellPrice().get()).subtract(getBuyVolume().get().multiply(getBuyPrice().get()))
                                .compareTo(BD_0_01.multiply(price)) > 0){
                    rBuy = getSellVolume().get().multiply(getSellPrice().get()).subtract(getBuyVolume().get().multiply(getBuyPrice().get()))
                            .divide(price, HALF_EVEN).doubleValue();
                    rSell = 0.01;

                    meanTime.set(System.currentTimeMillis());
                }else if (mean && sellPrice.compareTo(getSellPrice().get()) > 0 &&
                        getBuyVolume().get().multiply(getBuyPrice().get()).subtract(getSellVolume().get().multiply(getSellPrice().get()))
                                .compareTo(BD_0_01.multiply(price)) > 0){
                    rBuy = 0.01;
                    rSell = getBuyVolume().get().multiply(getBuyPrice().get()).subtract(getSellVolume().get().multiply(getSellPrice().get()))
                            .divide(price, HALF_EVEN).doubleValue();

                    meanTime.set(System.currentTimeMillis());
                }else {
                    double a = amount.doubleValue() * QuranRandom.nextDouble();

                    rBuy = up ? a : 0.01;
                    rSell = up ? 0.01 : a;
                }

                if (isVol()){
                    buyAmount = new BigDecimal(rBuy).setScale(3, HALF_EVEN);
                    sellAmount = new BigDecimal(rSell).setScale(3, HALF_EVEN);

                    if (buyAmount.compareTo(BD_0_01) < 0){
                        buyAmount = BD_0_01;
                    }
                    if (sellAmount.compareTo(BD_0_01) < 0){
                        sellAmount = BD_0_01;
                    }
                }

                Order buyOrder = new Order(strategy, positionId, BID, buyPrice, buyAmount);
                Order sellOrder = new Order(strategy, positionId, ASK, sellPrice, sellAmount);

                buyOrder.setSpread(spread);
                sellOrder.setSpread(spread);

                if (isVol()) {
                    if (!strategy.isLevelInverse()) {
                        createOrderSync(sellOrder);
                        createOrderSync(buyOrder);
                    }else {
                        createOrderSync(buyOrder);
                        createOrderSync(sellOrder);
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
//                action.onNext(trade.getPrice());
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
                lastPrice.set(ask);
//                action.onNext(ask);
            }else {
                lastPrice.set(bid);
//                action.onNext(bid);
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
                //action.onNext(order.getAvgPrice());
            }
        }
    }
}

