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
import java.util.Comparator;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.Semaphore;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderType.*;

/**
 * @author inheaven on 08.07.2015 19:39.
 */
public class LevelStrategy extends BaseStrategy{
    private Logger log = LoggerFactory.getLogger(getClass());

    private SecureRandom random = new SecureRandom("LevelStrategy".getBytes());

    private Strategy strategy;

    private int errorCount = 0;
    private long errorTime = 0;

    private BigDecimal risk = ONE;

    private Map<BigDecimal, Long> levelTimeMap = new ConcurrentHashMap<>();

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService) {
        super(strategy, orderService, orderMapper, tradeService, depthService);

        this.strategy = strategy;

        if (strategy.getSymbolType() != null) {
            userInfoService.createUserInfoObservable(strategy.getAccount().getId(), strategy.getSymbol().substring(0, 3))
                    .filter(u -> u.getRiskRate() != null)
                    .subscribe(u -> {
                        if (u.getRiskRate().compareTo(BigDecimal.valueOf(5)) < 0) {
                            risk = BigDecimal.valueOf(10);
                        } else if (u.getRiskRate().compareTo(BigDecimal.valueOf(8)) < 0) {
                            risk = BigDecimal.valueOf(5);
                        }else if (u.getRiskRate().compareTo(BigDecimal.valueOf(10)) < 0) {
                            risk = BigDecimal.valueOf(2);
                        }else {
                            risk = ONE;
                        }
                    });
        }
    }

    private Semaphore lock = new Semaphore(1);

    private void action(String key, BigDecimal price) {
        if (errorCount > 10){
            if (System.currentTimeMillis() - errorTime < 60000){
                return;
            }else{
                errorCount = 0;
                errorTime = 0;
            }
        }

        BigDecimal spread = strategy.getLevelSpread().multiply(risk);
        BigDecimal spreadX2 = spread.multiply(BigDecimal.valueOf(2)).setScale(8, HALF_UP);
        BigDecimal level = price.divideToIntegralValue(spreadX2);

        try {
            lock.acquire();

            if (!getOrderMap().values().parallelStream()
                    .filter(order -> LONG.contains(order.getType()))
                    .filter(o -> (SELL_SET.contains(o.getType()) &&
                                    o.getPrice().subtract(price).abs().compareTo(spreadX2) < 0) ||
                                    (BUY_SET.contains(o.getType()) &&
                                            price.subtract(o.getPrice()).abs().compareTo(spread) < 0))
                    .findAny()
                    .isPresent()){
                BigDecimal spreadHFT = spread;
                BigDecimal amountHFT = strategy.getLevelLot();

                if (levelTimeMap.get(level) != null){
                    Long time = System.currentTimeMillis() - levelTimeMap.get(level);

                    if (strategy.getSymbolType() == null){
                        if (time < 5000){
                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(4));
                        }else if (time < 15000){
                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(3));
                        }else if (time < 30000){
                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(2));
                        }
                    }else if (time < 30000){
                        spreadHFT = spreadHFT.divide(BigDecimal.valueOf(2), 8, HALF_UP);
                    }

                    if (time < 30000) {
                        log.info("HFT -> {} {} {} {}", time/1000, price, amountHFT, Objects.toString(strategy.getSymbolType(), ""));
                    }
                }

                if (risk.compareTo(ONE) > 0){
                    log.warn("RISK RATE {} {} {}", risk, strategy.getSymbol(), Objects.toString(strategy.getSymbolType(), ""));
                }

                //BUY

                if (strategy.getSymbolType() == null){
                    amountHFT = amountHFT.multiply(BigDecimal.valueOf(1 + (random.nextDouble() / 5))).setScale(8, HALF_UP);
                }

                Future<Order> open = createOrderAsync(
                        new Order(strategy,
                                strategy.getSymbolType() != null ? OPEN_LONG : BID,
                                price.add(getStep()),
                                amountHFT));

                //SELL

                if (strategy.getSymbolType() == null){
                    amountHFT = amountHFT.multiply(BigDecimal.valueOf(1 + (random.nextDouble() / 5))).setScale(8, HALF_UP);
                }

                Future<Order> close = createOrderAsync(
                        new Order(strategy,
                                strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                                price.add(spreadHFT),
                                amountHFT));

                log.info(key + " {}", price);

                open.get();
                close.get();

                levelTimeMap.put(level, System.currentTimeMillis());
            }
        } catch (Exception e) {
            errorCount++;
            errorTime = System.currentTimeMillis();
        } finally {
            lock.release();
        }
    }

    private BigDecimal lastTrade = ZERO;

    @Override
    protected void onTrade(Trade trade) {
        if (lastTrade.compareTo(ZERO) == 0 || lastTrade.subtract(trade.getPrice()).abs()
                .divide(lastTrade, 8, HALF_UP).compareTo(BigDecimal.valueOf(0.1)) < 0){
            action("on trade", trade.getPrice());
        }else{
            log.warn("trade price diff 10% than last trade {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
        }

        lastTrade = trade.getPrice();
    }

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal spread = strategy.getLevelSpread();

        BigDecimal ask = depth.getAskMap().keySet().parallelStream().min(Comparator.<BigDecimal>naturalOrder()).get();
        BigDecimal bid = depth.getBidMap().keySet().parallelStream().max(Comparator.<BigDecimal>naturalOrder()).get();

        if (ask.subtract(bid).compareTo(spread.multiply(BigDecimal.valueOf(2.1))) > 0 && ask.compareTo(bid) > 0){
            action("on depth ask", ask.subtract(spread).subtract(getStep()));
            action("on depth bid", bid.add(spread.multiply(BigDecimal.valueOf(2))).add(getStep()));
        }
    }

    @Override
    protected void onCloseOrder(Order order) {
        if (errorCount > 0 && SELL_SET.contains(order.getType())){
            errorCount--;
        }

        if (order.getStatus().equals(OrderStatus.CLOSED)){
            action("on close order", order.getPrice());
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(OrderStatus.CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            action("on real trade", order.getAvgPrice());
        }
    }

    private BigDecimal getStep(){
        switch (strategy.getSymbol()){
            case "BTC/USD":
                return BigDecimal.valueOf(0.01);
            case "LTC/USD":
                return BigDecimal.valueOf(0.001);
        }

        return ZERO;
    }
}
