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

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_UP;
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

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService) {
        super(strategy, orderService, orderMapper, tradeService, depthService);

        this.strategy = strategy;

        if (strategy.getSymbolType() != null) {
            userInfoService.createUserInfoObservable(strategy.getAccount().getId(), strategy.getSymbol().substring(0, 3))
                    .filter(u -> u.getRiskRate() != null)
                    .subscribe(u -> {
                        if (u.getRiskRate().compareTo(BigDecimal.valueOf(10)) < 0){
                            risk = TEN.divide(u.getRiskRate(), 8, HALF_UP).pow(3).setScale(2, HALF_UP);
                        }else {
                            risk = ONE;
                        }
                    });
        }
    }

    private Semaphore lock = new Semaphore(1);

    private void action(String key, BigDecimal price) {
        if (getErrorCount() > 10){
            if (System.currentTimeMillis() - getErrorTime() < 60000){
                return;
            }else{
                setErrorCount(0);
                setErrorTime(0);
            }
        }

        BigDecimal spread = strategy.getLevelSpread().multiply(risk);
        BigDecimal spreadX2 = spread.multiply(BigDecimal.valueOf(2)).setScale(8, HALF_UP);
        BigDecimal level = price.divideToIntegralValue(spreadX2);

        try {
            lock.acquire();

            if (!getOrderMap().values().parallelStream()
                    .filter(o -> LONG.contains(o.getType()))
                    .filter(o -> (SELL_SET.contains(o.getType()) &&
                            o.getPrice().subtract(price).abs().compareTo(spreadX2) < 0) ||
                            (BUY_SET.contains(o.getType()) &&
                                    price.subtract(o.getPrice()).abs().compareTo(spread) < 0))
                    .findAny()
                    .isPresent()){
                BigDecimal amountHFT = strategy.getLevelLot();

                if (levelTimeMap.get(level) != null){
                    Long time = System.currentTimeMillis() - levelTimeMap.get(level);

                    if (strategy.getSymbolType() == null){
                        if (time < 60000){
                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(-3.0*time/60000 + 4.0));

                            log.info("HFT -> {}s {} {} -> {}", time/1000, price, strategy.getLevelLot(), amountHFT);
                        }
                    }else if (time < 15000){
                        amountHFT = amountHFT.multiply(BigDecimal.valueOf(2));

                        log.info("HFT -> {}s {} {} {}", time/1000, price.setScale(3, HALF_UP), amountHFT.setScale(3, HALF_UP), strategy.getSymbolType());
                    }
                }

                if (risk.compareTo(ONE) > 0){
                    log.warn("RISK RATE {} {} {}", risk.setScale(3, HALF_UP), strategy.getSymbol(), Objects.toString(strategy.getSymbolType(), ""));
                }

                Long positionId = System.nanoTime();

                //BUY
                BigDecimal buyAmount = amountHFT;

                if (strategy.getSymbolType() == null){
                    buyAmount = buyAmount.multiply(BigDecimal.valueOf(1 + (random.nextDouble() / 5))).setScale(8, HALF_UP);
                }

                createOrderAsync(new Order(strategy,
                        positionId,
                        strategy.getSymbolType() != null ? OPEN_LONG : BID,
                        price.add(getStep()),
                        buyAmount));

                //SELL

                BigDecimal sellAmount = amountHFT;

                if (strategy.getSymbolType() == null && strategy.getSymbol().equals("LTC/USD")){
                    sellAmount = sellAmount.multiply(BigDecimal.valueOf(1 + (random.nextDouble() / 5))).setScale(8, HALF_UP);
                }

                createOrderAsync(new Order(strategy,
                        positionId,
                        strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                        price.add(spread),
                        sellAmount));

                log.info(key + " {}", price);

                levelTimeMap.put(level, System.currentTimeMillis());
            }
        } catch (Exception e) {
            incrementErrorCount();
            setErrorTime(System.currentTimeMillis());
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

        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();

        if (ask.subtract(bid).compareTo(spread.multiply(BigDecimal.valueOf(2))) > 0 && ask.compareTo(bid) > 0){
            BigDecimal step = getStep().multiply(BigDecimal.valueOf(2));

            action("on depth ask", ask.subtract(spread).subtract(step));
            action("on depth bid", bid.add(spread.multiply(BigDecimal.valueOf(2))).add(step));
        }

        closeOnCheck(ask);
        closeOnCheck(bid);
    }

    @Override
    protected void onCloseOrder(Order order) {
        if (getErrorCount() > 0 && SELL_SET.contains(order.getType())){
            decrementErrorCount();
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
