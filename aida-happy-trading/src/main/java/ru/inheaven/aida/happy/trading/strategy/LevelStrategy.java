package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import ru.inheaven.aida.happy.trading.service.UserInfoService;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

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

    private Map<String, Long> levelTimeMap = new ConcurrentHashMap<>();

    private UserInfoService userInfoService;
    private TradeService tradeService;

    private static AtomicLong positionId = new AtomicLong(System.nanoTime());

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService) {
        super(strategy, orderService, orderMapper, tradeService, depthService);

        this.strategy = strategy;
        this.userInfoService = userInfoService;
        this.tradeService = tradeService;

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
        boolean reversing = isReversing(price);
        BigDecimal sideSpread = getSideSpread(price);

        if (!isBidRefused()){
            getOrderMap().get(price.subtract(sideSpread), BID).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(WAIT)){
                        pushWaitOrderAsync(o);
                    }
                });
            });
        }

        if (!isAskRefused()){
            getOrderMap().get(price.add(sideSpread), ASK).forEach((k,v) -> {
                v.forEach(o -> {
                    if (o.getStatus().equals(WAIT) && (!reversing || !getOrderMap().containsBid(o.getPositionId()))){
                        pushWaitOrderAsync(o);
                    }
                });
            });
        }
    }

    @SuppressWarnings("Duplicates")
    private void closeByMarket(BigDecimal price){
        getOrderMap().get(price.add(getSideSpread(price)), BID, false).forEach((k,v) -> {
            v.forEach(o -> {
                if (o.getStatus().equals(OPEN) && System.currentTimeMillis() - o.getOpen().getTime() > 600000){
                    o.setStatus(CLOSED);
                    log.info("{} CLOSED by market {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType());
                }
            });
        });

        getOrderMap().get(price.subtract(getSideSpread(price)), ASK, false).forEach((k,v) -> {
            v.forEach(o -> {
                if (o.getStatus().equals(OPEN) && System.currentTimeMillis() - o.getOpen().getTime() > 600000){
                    o.setStatus(CLOSED);
                    log.info("{} CLOSED by market {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType());
                }
            });
        });
    }

    private Semaphore semaphore = new Semaphore(1);

    private void action(String key, BigDecimal price, OrderType orderType){
        try {
            semaphore.acquire();

            pushOrders(price);
            actionLevel(key, price, orderType);
        } catch (Exception e) {
            log.error("error action level", e);
        } finally {
            semaphore.release();
        }
    }

    private void actionLevel(String key, BigDecimal price, OrderType orderType){
        BigDecimal spreadF = getSideSpread(price);

//        for (int i = strategy.getLevelSize().intValue(); i > 0 ; --i){
//            action(key, price.add(BigDecimal.valueOf(i).multiply(spreadF)), orderType, i);
//            action(key, price.add(BigDecimal.valueOf(-i).multiply(spreadF)), orderType, -i);
//        }

        int i = random.nextInt(strategy.getLevelSize().intValue());
        action(key, price.add(BigDecimal.valueOf(i).multiply(spreadF)), orderType, i);

        i = random.nextInt(strategy.getLevelSize().intValue());
        action(key, price.add(BigDecimal.valueOf(-i).multiply(spreadF)), orderType, -i);
    }

    private boolean isReversing(BigDecimal price){
        UserInfoTotal avg = userInfoService.getAvg(strategy.getAccount().getId());

        if (avg != null){
            if (strategy.getSymbol().contains("LTC")) {
                return price.compareTo(avg.getLtcPrice()) < 0;
            } else {
                return strategy.getSymbol().contains("BTC") && price.compareTo(avg.getBtcPrice()) < 0;
            }
        }

        return false;
    }

    private BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;

        if (strategy.getSymbol().equals("BTC/CNY")){
            BigDecimal stdDev = tradeService.getStdDev("BTC/CNY");

            if (stdDev != null){
                spread = stdDev.subtract(getSideSpread(price)).divide(BigDecimal.valueOf(2), HALF_EVEN);
            }
        }else {
             spread = strategy.getSymbolType() == null
                    ? strategy.getLevelSpread().multiply(price)
                    : strategy.getLevelSpread();
        }

        BigDecimal sideSpread = getSideSpread(price);

        return spread.compareTo(sideSpread) > 0 ? spread : sideSpread;
    }

    private BigDecimal getSideSpread(BigDecimal price){
        BigDecimal sideSpread = strategy.getSymbolType() == null
                ? strategy.getLevelSideSpread().multiply(price)
                : strategy.getLevelSideSpread();

        return sideSpread.compareTo(getStep()) > 0 ? sideSpread : getStep();
    }

    private void action(String key, BigDecimal price, OrderType orderType, int priceLevel) {
        try {
            boolean reversing = isReversing(price);

            BigDecimal priceF = scale(orderType.equals(ASK) ? price.subtract(getStep()) : price.add(getStep()));
            BigDecimal spread = scale(getSpread(priceF));
            BigDecimal sideSpread = scale(getSideSpread(priceF));
            BigDecimal level = priceF.divideToIntegralValue(spread);
            BigDecimal buyPrice = reversing ? priceF.subtract(spread) : priceF;
            BigDecimal sellPrice = reversing ? priceF : priceF.add(spread);

            if (!getOrderMap().contains(buyPrice, sideSpread, BID) && !getOrderMap().contains(sellPrice, sideSpread, ASK)){
                log.info(key + " {} {} r={} {}", price.setScale(3, HALF_EVEN), orderType, reversing, spread);

                BigDecimal amountHFT = strategy.getLevelLot();

//                if (levelTimeMap.get(level.toString() + reversing) != null){
//                    Long time = System.currentTimeMillis() - levelTimeMap.get(level.toString() + reversing);
//
//                    if (strategy.getSymbolType() == null){
//                        if (time < 600000 && strategy.getAccount().getExchangeType().equals(ExchangeType.OKCOIN)){
//                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(10.0 - 9.0*time/600000));
//
//                            log.info("HFT -> {}s {} {} -> {}", time/1000, price.setScale(3, HALF_EVEN),
//                                    strategy.getLevelLot().setScale(3, HALF_EVEN), amountHFT.setScale(3, HALF_EVEN));
//                        }
//
//                        if (time < 5000 && strategy.getAccount().getExchangeType().equals(ExchangeType.OKCOIN_CN)){
//                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(3.0 - 2.0*time/5000));
//
//                            log.info("HFT -> {}ms {} {} -> {}", time, price.setScale(3, HALF_EVEN),
//                                    strategy.getLevelLot().setScale(3, HALF_EVEN), amountHFT.setScale(3, HALF_EVEN));
//                        }
//                    }else if (time < 15000){
//                        amountHFT = amountHFT.multiply(BigDecimal.valueOf(2));
//
//                        log.info("HFT -> {}s {} {} {}", time/1000, price.setScale(3, HALF_EVEN),
//                                amountHFT.setScale(3, HALF_EVEN), strategy.getSymbolType());
//                    }
//                }

                if (risk.compareTo(ONE) > 0){
                    log.warn("RISK RATE {} {} {}", risk.setScale(3, HALF_EVEN), strategy.getSymbol(),
                            Objects.toString(strategy.getSymbolType(), ""));
                }

                Long positionId = LevelStrategy.positionId.incrementAndGet();

                BigDecimal buyAmount = amountHFT;
                BigDecimal sellAmount = amountHFT;

                if (strategy.getSymbolType() == null){
                    buyAmount = amountHFT.add(amountHFT.multiply(spread).divide(buyPrice, 8, HALF_EVEN));
                    sellAmount = amountHFT;
                }

                Order buyOrder = new Order(strategy, positionId, strategy.getSymbolType() != null ? OPEN_LONG : BID,
                        buyPrice, buyAmount);
                Order sellOrder = new Order(strategy, positionId, strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                        sellPrice, sellAmount);

//                if (priceLevel < 1){
//                    createWaitOrder(buyOrder);
//
//                    if (reversing){
//                        createOrderAsync(sellOrder);
//                    }else {
//                        createWaitOrder(sellOrder);
//                    }
//                }else{
//                    createOrderAsync(buyOrder);
//                    createWaitOrder(sellOrder);
//                }

                createWaitOrder(buyOrder);
                createWaitOrder(sellOrder);

                if (orderType.equals(ASK)) {
                    levelTimeMap.put(level.toString() + reversing,  System.currentTimeMillis());
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
                lastTrade.subtract(trade.getPrice()).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(getStep()) < 0){
            action("on trade", trade.getPrice(), trade.getOrderType());
            closeByMarket(trade.getPrice());
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
            action("on depth ask", ask, ASK);
            action("on depth bid", bid, BID);
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            action("on real trade", order.getAvgPrice(), order.getType());
        }
    }
}
