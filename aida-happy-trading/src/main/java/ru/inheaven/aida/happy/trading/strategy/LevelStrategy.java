package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.*;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_EVEN;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;
import static ru.inheaven.aida.happy.trading.entity.OrderType.*;

/**
 * @author inheaven on 08.07.2015 19:39.
 */
public class LevelStrategy extends BaseStrategy{
    private Logger log = LoggerFactory.getLogger(getClass());

    private SecureRandom random = new SecureRandom("LevelStrategy".getBytes());
    private Strategy strategy;
    private BigDecimal risk = ONE;

    private Map<BigDecimal, Long> levelTimeMap = new ConcurrentHashMap<>();

    private UserInfoService userInfoService;
    private TradeService tradeService;
    private OrderService orderService;

    private static AtomicLong positionId = new AtomicLong(System.nanoTime());

    private AtomicReference<BigDecimal> profit = new AtomicReference<>(ZERO);
    private final static BigDecimal BD0_001 = new BigDecimal("0.001");
    private final static BigDecimal BD0_002 = new BigDecimal("0.002");

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
        BigDecimal spread = getSpread(price);
        boolean inverse = strategy.isLevelInverse();

        if (!isBidRefused()){
            getOrderMap().get(price.subtract(spread), BID).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(WAIT) && (!inverse || !getOrderMap().containsAsk(o.getPositionId()))){
                        pushWaitOrderAsync(o);
                    }
                });
            });
        }

        if (!isAskRefused()){
            getOrderMap().get(price.add(spread), ASK).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(WAIT) && (inverse || !getOrderMap().containsBid(o.getPositionId()))){
                        pushWaitOrderAsync(o);
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
                    if (o.getStatus().equals(OPEN) && o.getOpen().compareTo(time) < 0){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());
                        log.info("{} CLOSED by market {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType());
                    }
                });
            });

            getOrderMap().get(price.subtract(getSideSpread(price)), ASK, false).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(OPEN) && o.getOpen().compareTo(time) < 0){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());
                        log.info("{} CLOSED by market {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType());
                    }
                });
            });
        } catch (Exception e) {
            log.error("error close by market", e);
        }
    }

    private Executor executor2 = Executors.newCachedThreadPool();
    private AtomicReference<BigDecimal> lastAction = new AtomicReference<>(ZERO);
    private ConcurrentLinkedDeque<BigDecimal> actionDeque = new ConcurrentLinkedDeque<>();

    private void actionAsync(String key, BigDecimal price, OrderType orderType){
        if (lastAction.get().compareTo(price) != 0) {
            lastAction.set(price);
            actionDeque.add(price);

            executor2.execute(() -> action(key, price, orderType));
        }
    }

    private Semaphore semaphore = new Semaphore(1);

    private void action(String key, BigDecimal price, OrderType orderType){
        try {
            semaphore.acquire();

            BigDecimal dequePrice = actionDeque.poll();

            if ((!strategy.isLevelInverse() && !isMinSpot()) || (strategy.isLevelInverse() && !isMaxSpot())) {
                actionLevel(key, dequePrice, orderType);
                pushOrders(dequePrice);
            }
        } catch (Exception e) {
            log.error("error action level", e);
        } finally {
            semaphore.release();
        }
    }

    private AtomicLong index = new AtomicLong(0);

    private void actionLevel(String key, BigDecimal price, OrderType orderType){
        BigDecimal sideSpread = getSideSpread(price);

        int i = (int) (index.incrementAndGet() % strategy.getLevelSize().intValue());
        action(key, price.add(BigDecimal.valueOf(i).multiply(sideSpread)), price, orderType, i);
        action(key, price.add(BigDecimal.valueOf(-i).multiply(sideSpread)), price, orderType, -i);
        action(key, price, price, orderType, 0);
    }

    private static final BigDecimal CNY_MIN = BigDecimal.valueOf(30000);
    private static final BigDecimal USD_MIN = BigDecimal.valueOf(300);

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

    private static final BigDecimal CNY_MAX = BigDecimal.valueOf(33000);
    private static final BigDecimal USD_MAX = BigDecimal.valueOf(6000);

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

    private static final BigDecimal CNY_MIDDLE = BigDecimal.valueOf(26000);
    private static final BigDecimal USD_MIDDLE = BigDecimal.valueOf(3250);

    @SuppressWarnings("Duplicates")
    private boolean isUpSpot(){
        BigDecimal volume = userInfoService.getVolume("subtotal_spot", strategy.getAccount().getId(), null);

        if (volume != null){
            if (strategy.getSymbol().contains("CNY")){
                return volume.compareTo(CNY_MIDDLE) > 0;
            }else if (strategy.getSymbol().contains("USD")){
                return volume.compareTo(USD_MIDDLE) > 0;
            }
        }

        return true;
    }

    private static final BigDecimal TWO = BigDecimal.valueOf(2);

    private BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;
        BigDecimal sideSpread = getSideSpread(price);

        if (strategy.getSymbol().equals("BTC/CNY")){
            BigDecimal stdDev = tradeService.getStdDev("BTC/CNY");

            if (stdDev != null){
                spread = stdDev.subtract(sideSpread).divide(TWO, HALF_EVEN);
            }
        }else {
            spread = strategy.getSymbolType() == null
                    ? strategy.getLevelSpread().multiply(price)
                    : strategy.getLevelSpread();
        }

        return spread.compareTo(sideSpread) > 0 ? spread : sideSpread;
    }

    private static final BigDecimal BD05 = new BigDecimal("0.5");

    private BigDecimal getSideSpread(BigDecimal price){
        BigDecimal sideSpread = strategy.getSymbolType() == null
                ? strategy.getLevelSideSpread().multiply(price)
                : strategy.getLevelSideSpread();

//        if (strategy.getSymbol().equals("BTC/CNY")){
//            BigDecimal stdDev = tradeService.getStdDev("BTC/CNY");
//
//            if (stdDev != null && stdDev.compareTo(BD05) > 0){
//                sideSpread = sideSpread.multiply(stdDev.multiply(TWO));
//            }
//        }

        return sideSpread.compareTo(getStep()) > 0 ? sideSpread : getStep();
    }

    private final static BigDecimal BD1_1 = new BigDecimal("1.1");

    private void action(String key, BigDecimal price, BigDecimal realPrice, OrderType orderType, int priceLevel) {
        try {
            boolean up = isUpSpot();

            BigDecimal priceF = scale(up ? price.add(getStep()) : price.subtract(getStep()));
            BigDecimal spread = scale(getSpread(priceF));
            BigDecimal sideSpread = scale(getSideSpread(priceF));
            BigDecimal level = priceF.divideToIntegralValue(spread);


            BigDecimal buyPrice = up ? priceF : priceF.subtract(spread);
            BigDecimal sellPrice = up ? priceF.add(spread) : priceF;

            if (!getOrderMap().contains(buyPrice, sideSpread, BID, realPrice) && !getOrderMap().contains(sellPrice, sideSpread, ASK, realPrice)){
                log.info("{} "  + key + " {} {} {} {} {} {}", strategy.getId(), price.setScale(3, HALF_EVEN), orderType,
                        sideSpread, spread, isMinSpot(), isMaxSpot());

                BigDecimal amountHFT = strategy.getLevelLot();

                if (priceLevel == 0 && levelTimeMap.get(level) != null){
                    Long time = System.currentTimeMillis() - levelTimeMap.get(level);

//                    if (strategy.getSymbolType() == null){
//                        if (time < 60000 && strategy.getAccount().getExchangeType().equals(ExchangeType.OKCOIN)){
//                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(5.0 - 4.0*time/60000));
//
//                            log.info("{} HFT {} {} {} {}", strategy.getId(), (int) (time/1000), priceF,
//                                    scale(strategy.getLevelLot()), scale(amountHFT));
//                        }
//
//                        if (time < 5000 && strategy.getAccount().getExchangeType().equals(ExchangeType.OKCOIN_CN)){
//                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(5.0 - 4.0*time/5000));
//
//                            log.info("{} HFT {} {} {} {}", strategy.getId(), priceF, time,
//                                    scale(strategy.getLevelLot()), scale(amountHFT));
//                        }
//                    }else if (time < 15000){
//                        amountHFT = amountHFT.multiply(BigDecimal.valueOf(2));
//
//                        log.info("HFT -> {}s {} {} {}", time/1000, price.setScale(3, HALF_EVEN),
//                                amountHFT.setScale(3, HALF_EVEN), strategy.getSymbolType());
//                    }
                }

                if (risk.compareTo(ONE) > 0){
                    log.warn("RISK RATE {} {} {}", risk.setScale(3, HALF_EVEN), strategy.getSymbol(),
                            Objects.toString(strategy.getSymbolType(), ""));
                }

                Long positionId = LevelStrategy.positionId.incrementAndGet();

                BigDecimal buyAmount = amountHFT;
                BigDecimal sellAmount = amountHFT;

                if (strategy.getSymbolType() == null){
                    buyAmount = amountHFT;
                    sellAmount = amountHFT;

                    profit.set(profit.get().add(amountHFT.multiply(spread).divide(buyPrice, 8, HALF_EVEN)));

                    if (profit.get().compareTo(BD0_002) > 0){
                        if (strategy.getSymbol().equals("BTC/CNY")){
                            BigDecimal ltcPrice = userInfoService.getPrice(ExchangeType.OKCOIN_CN, "LTC/CNY", null);
                            Order ltcOrder = new Order(null, null, BID, ltcPrice.multiply(BD1_1),
                                    profit.get().multiply(realPrice).divide(ltcPrice, 8, HALF_EVEN));
                            ltcOrder.setExchangeType(ExchangeType.OKCOIN_CN);
                            ltcOrder.setInternalId(String.valueOf(System.nanoTime()));
                            ltcOrder.setSymbol("LTC/CNY");

                            orderService.createOrder(strategy.getAccount(), ltcOrder);
                        }else{
                            buyAmount = buyAmount.add(profit.get());
                        }

                        profit.set(ZERO);

                        log.info("{} !!!!!!!!!!!!!!! PROFIT !!!!!!!!!!!!!!!", strategy.getId());
                    }
                }

                Order buyOrder = new Order(strategy, positionId, strategy.getSymbolType() != null ? OPEN_LONG : BID,
                        buyPrice, buyAmount);
                Order sellOrder = new Order(strategy, positionId, strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                        sellPrice, sellAmount);

                if (strategy.isLevelInverse()){
                    sellOrder.setAmount(sellOrder.getAmount().multiply(TWO));

                    createOrderSync(sellOrder);
                    createWaitOrder(buyOrder);
                }else {
                    createOrderSync(buyOrder);
                    createWaitOrder(sellOrder);
                }

                levelTimeMap.put(level,  System.currentTimeMillis());
            }
        } catch (Exception e) {
            log.error("action error -> ", e);
        }
    }

    private BigDecimal lastTrade = ZERO;

    @Override
    protected void onTrade(Trade trade) {
        if (lastTrade.compareTo(ZERO) != 0 &&
                lastTrade.subtract(trade.getPrice()).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(getStep()) < 0){
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
                lastTrade.subtract(ask).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(getStep()) < 0 &&
                lastTrade.subtract(bid).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(getStep()) < 0) {
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
