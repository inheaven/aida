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
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_UP;
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

    private BigDecimal lastAsk = ZERO;
    private BigDecimal lastBid = ZERO;

    private UserInfoService userInfoService;

    private final static BigDecimal MIN_USD = new BigDecimal("500");

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService) {
        super(strategy, orderService, orderMapper, tradeService, depthService);

        this.strategy = strategy;

        this.userInfoService = userInfoService;

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

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(levelTimeMap::clear, 0, 1, TimeUnit.HOURS);
    }

    private Semaphore lock = new Semaphore(1);

    private void action(String key, BigDecimal price, OrderType orderType) {
        if (getErrorCount() > 10){
            if (System.currentTimeMillis() - getErrorTime() < 60000){
                return;
            }else{
                setErrorCount(0);
                setErrorTime(0);
            }
        }

        try {
            lock.acquire();

            BigDecimal spread = strategy.getLevelSpread().multiply(risk);

            if (strategy.getSymbolType() == null) {
                BigDecimal minSpread = price.multiply(new BigDecimal("0.002"));

                if (spread.compareTo(minSpread) < 0){
                    spread = minSpread;

                    log.warn("LOW Spread -> {} {} {} {}", strategy.getId(), minSpread.setScale(3, HALF_UP), strategy.getSymbol(),
                            Objects.toString(strategy.getSymbolType(), ""));
                }
            }

            BigDecimal spreadF = spread;
            BigDecimal spreadX2 = spread.multiply(BigDecimal.valueOf(2));
            BigDecimal level = price.divideToIntegralValue(spread);
            BigDecimal step = getStep(price);

            boolean reversing = step.compareTo(ZERO) < 0;

            Order search = getOrderMap().searchValues(64, (o) -> {
                if (LONG.contains(o.getType()) && (OPEN.equals(o.getStatus()) || CREATED.equals(o.getStatus())) &&
                        (SELL_SET.contains(o.getType()) &&
                                o.getPrice().subtract(price).abs().compareTo(reversing ? spreadF : spreadX2) < 0) ||
                        (BUY_SET.contains(o.getType()) &&
                                price.subtract(o.getPrice()).abs().compareTo(reversing ? spreadX2 : spreadF) < 0)){

                    return o;
                }

                return null;
            });

            if (search == null){
                log.info(key + " {} {}", price.setScale(3, HALF_UP), orderType);

                BigDecimal amountHFT = strategy.getLevelLot();

                if (levelTimeMap.get(level) != null){
                    Long time = System.currentTimeMillis() - levelTimeMap.get(level);

                    if (strategy.getSymbolType() == null){
                        if (time < 600000){
                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(10.0 - 9.0*time/600000));

                            log.info("HFT -> {}s {} {} -> {}", time/1000, price.setScale(3, HALF_UP),
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

                Long positionId = System.nanoTime();

                BigDecimal buyAmount = amountHFT;
                if (strategy.getSymbolType() == null && userInfoService.getUsdSpot().compareTo(MIN_USD) > 0){
                    buyAmount = buyAmount.multiply(BigDecimal.valueOf(1.0 + random.nextDouble()/5)).setScale(8, HALF_UP);
                }

                BigDecimal sellAmount = amountHFT;
                if (strategy.getSymbolType() == null){
                    sellAmount = sellAmount.multiply(BigDecimal.valueOf(1.0 + random.nextDouble()/5)).setScale(8, HALF_UP);
                }

                if (reversing){
                    createOrderAsync(new Order(strategy, positionId, strategy.getSymbolType() != null ? OPEN_LONG : BID,
                            price.subtract(spread), sellAmount));
                    createOrderAsync(new Order(strategy, positionId, strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                            price.add(step), buyAmount));
                }else{
                    createOrderAsync(new Order(strategy, positionId, strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                            price.add(spread), sellAmount));
                    createOrderAsync(new Order(strategy, positionId, strategy.getSymbolType() != null ? OPEN_LONG : BID,
                            price.add(step), buyAmount));
                }

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
            action("on trade", trade.getPrice(), trade.getOrderType());
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

        lastAsk = ask;
        lastBid = bid;

        if (ask.subtract(bid).compareTo(spread.multiply(BigDecimal.valueOf(2))) > 0 && ask.compareTo(bid) > 0){
            action("on depth ask", ask.subtract(spread), ASK);
            action("on depth bid", bid.add(spread.multiply(BigDecimal.valueOf(2))), BID);
        }

        closeOnCheck(ask);
        closeOnCheck(bid);
    }

    @Override
    protected void onCloseOrder(Order order) {
        if (getErrorCount() > 0 && SELL_SET.contains(order.getType())){
            decrementErrorCount();
        }

        if (order.getStatus().equals(CLOSED)){
            action("on close order", order.getPrice(), order.getType());
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            action("on real trade", order.getAvgPrice(), order.getType());
        }
    }

    private BigDecimal getStep(BigDecimal price){
        int bid = price.subtract(lastAsk).abs().compareTo(price.subtract(lastBid).abs()) >= 0 ? 1 : -1;

        switch (strategy.getSymbol()){
            case "BTC/USD":
                return new BigDecimal("0.01").multiply(BigDecimal.valueOf(bid));
            case "LTC/USD":
                return new BigDecimal("0.001").multiply(BigDecimal.valueOf(bid));
        }

        return ZERO;
    }
}
