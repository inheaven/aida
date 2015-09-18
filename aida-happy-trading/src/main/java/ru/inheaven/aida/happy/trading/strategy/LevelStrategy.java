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
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN;
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

    private void actionLevel(String key, BigDecimal price, OrderType orderType){
        if (strategy.getSymbol().equals("BTC/CNY")) {
            for (int i = -10; i < 10; ++i){
                action(key, price.add(BigDecimal.valueOf(i).multiply(strategy.getLevelSpread())), orderType);
            }
        }else{
            action(key, price, orderType);

        }
    }

    private Executor actionExecutor = Executors.newWorkStealingPool();

    private void action(String key, BigDecimal price, OrderType orderType){
        actionExecutor.execute(()-> actionSync(key, price, orderType));
    }

    private static AtomicLong positionId = new AtomicLong(System.nanoTime());

    private void actionSync(String key, BigDecimal price, OrderType orderType) {
        if (getErrorCount() > 10){
            if (System.currentTimeMillis() - getErrorTime() < 60000){
                return;
            }else{
                setErrorCount(0);
                setErrorTime(0);
            }
        }

        try {
            if (System.currentTimeMillis() - getRefusedTime() < 5000){
                return;
            }

            BigDecimal spread = strategy.getLevelSpread().multiply(risk);

            if (strategy.getSymbolType() == null) {
                BigDecimal minSpread = price.multiply(new BigDecimal("0.002"));

                if (strategy.getAccount().getExchangeType().equals(OKCOIN) && spread.compareTo(minSpread) < 0){
                    spread = minSpread;

                    log.warn("LOW Spread -> {} {} {} {}", strategy.getId(), minSpread.setScale(3, HALF_UP), strategy.getSymbol(),
                            Objects.toString(strategy.getSymbolType(), ""));
                }
            }

            BigDecimal spreadF = spread;
            BigDecimal spreadX2 = spread.multiply(BigDecimal.valueOf(2));
            BigDecimal level = price.divideToIntegralValue(spread);
            BigDecimal step = getStep(orderType);

            boolean reversing = random.nextBoolean();

            Order search = getOrderMap().searchValues(64, (o) -> {
                if (LONG.contains(o.getType()) && (OPEN.equals(o.getStatus()) || CREATED.equals(o.getStatus())) &&
                        ((SELL_SET.contains(o.getType()) &&
                                o.getPrice().subtract(price).abs().subtract(step.abs())
                                        .compareTo(reversing ? spreadF : spreadX2) <= 0) ||
                        (BUY_SET.contains(o.getType()) &&
                                price.subtract(o.getPrice()).abs().subtract(step.abs())
                                        .compareTo(reversing ? spreadX2 : spreadF) <= 0))){
                    return o;
                }

                return null;
            });

            if (search == null){
                log.info(key + " {} {}", price.setScale(3, HALF_UP), orderType);

                BigDecimal amountHFT = strategy.getLevelLot();

                if (levelTimeMap.get(level.toString() + reversing) != null){
                    Long time = System.currentTimeMillis() - levelTimeMap.get(level.toString() + reversing);

                    if (strategy.getSymbolType() == null){
                        if (time < 600000 && !strategy.getSymbol().contains("/CNY")){
                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(10.0 - 9.0*time/600000));

                            log.info("HFT -> {}s {} {} -> {}", time/1000, price.setScale(3, HALF_UP),
                                    strategy.getLevelLot().setScale(3, HALF_UP), amountHFT.setScale(3, HALF_UP));
                        }

                        if (time < 5000 && strategy.getSymbol().contains("/CNY")){
                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(3.0 - 2.0*time/5000));

                            log.info("HFT -> {}ms {} {} -> {}", time, price.setScale(3, HALF_UP),
                                    strategy.getLevelLot().setScale(3, HALF_UP), amountHFT.setScale(3, HALF_UP));
                        }

                    }else if (time < 15000){
                        amountHFT = amountHFT.multiply(BigDecimal.valueOf(2));

                        log.info("HFT -> {}s {} {} {}", time/1000, price.setScale(3, HALF_UP),
                                amountHFT.setScale(3, HALF_UP), strategy.getSymbolType());
                    }
                }

                if (risk.compareTo(ONE) > 0){
                    log.warn("RISK RATE {} {} {}", risk.setScale(3, HALF_UP), strategy.getSymbol(),
                            Objects.toString(strategy.getSymbolType(), ""));
                }

                Long positionId = this.positionId.incrementAndGet();

                BigDecimal buyAmount;
                BigDecimal sellAmount;

                if (strategy.getSymbol().contains("/CNY")){
                    buyAmount = amountHFT;
                    sellAmount = amountHFT;
                }else if (strategy.getSymbolType() == null){
                    buyAmount = amountHFT.multiply(BigDecimal.valueOf(1.0 + random.nextDouble()/5)).setScale(8, HALF_UP);
                    sellAmount = amountHFT.multiply(BigDecimal.valueOf(1.0 + random.nextDouble()/5)).setScale(8, HALF_UP);
                }else {
                    buyAmount = amountHFT;
                    sellAmount = amountHFT;
                }

                if (reversing){
                    createOrderAsync(new Order(strategy, positionId, strategy.getSymbolType() != null ? OPEN_LONG : BID,
                            price.subtract(spreadX2), buyAmount));
                    createOrderAsync(new Order(strategy, positionId, strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                            price, sellAmount));
                }else{
                    createOrderAsync(new Order(strategy, positionId, strategy.getSymbolType() != null ? OPEN_LONG : BID,
                            price, buyAmount));

                    createOrderAsync(new Order(strategy, positionId, strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                            price.add(spreadX2), sellAmount));
                }

                if (orderType.equals(ASK)) {
                    levelTimeMap.put(level.toString() + reversing,  System.currentTimeMillis());
                }
            }
        } catch (Exception e) {
            incrementErrorCount();
            setErrorTime(System.currentTimeMillis());
        }
    }

    private BigDecimal lastTrade = ZERO;

    @Override
    protected void onTrade(Trade trade) {
        if (lastTrade.compareTo(ZERO) == 0 || lastTrade.subtract(trade.getPrice()).abs()
                .divide(lastTrade, 8, HALF_UP).compareTo(new BigDecimal("0.05")) < 0){
            actionLevel("on trade", trade.getPrice(), trade.getOrderType());
        }else{
            log.warn("trade price diff 5% than last trade {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
        }

        lastTrade = trade.getPrice();
    }

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal spread = strategy.getLevelSpread();

        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();

        if (ask != null && bid != null) {
            closeOnCheck(bid, ask);

            if (ask.subtract(bid).abs().compareTo(spread.multiply(BigDecimal.valueOf(2))) > 0 && ask.compareTo(bid) > 0){
                action("on depth ask", ask, ASK);
                action("on depth bid", bid, BID);

                action("on depth ask spread", ask.subtract(spread), ASK);
                action("on depth bid spread", bid.add(spread), BID);
            }
        }
    }

    @Override
    protected void onCloseOrder(Order order) {
        if (getErrorCount() > 0 && SELL_SET.contains(order.getType())){
            decrementErrorCount();
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            action("on real trade", order.getAvgPrice(), order.getType());
        }
    }

    private BigDecimal getStep(OrderType orderType){
        int bid = orderType.equals(BID) ? 1 : -1;

        switch (strategy.getSymbol()){
            case "BTC/USD":
            case "BTC/CNY":
            case "LTC/CNY":
                return new BigDecimal("0.01").multiply(BigDecimal.valueOf(bid));
            case "LTC/USD":
                return new BigDecimal("0.001").multiply(BigDecimal.valueOf(bid));
        }

        return ZERO;
    }
}
