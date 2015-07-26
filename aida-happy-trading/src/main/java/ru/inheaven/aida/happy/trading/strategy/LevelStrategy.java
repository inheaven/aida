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
import java.util.concurrent.Future;

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

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService) {
        super(strategy, orderService, orderMapper, tradeService, depthService);

        this.strategy = strategy;

        if (strategy.getSymbolType() != null) {
            userInfoService.createUserInfoObservable(strategy.getSymbol().substring(0, 3))
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

                        if (risk.compareTo(ONE) > 0){
                            log.warn("RISK RATE {} {}", u.getCurrency(), u.getRiskRate());
                        }
                    });
        }
    }

    @Override
    protected void onTrade(Trade trade) {
        action(trade.getPrice());
    }

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal spread = strategy.getLevelSpread();

        BigDecimal ask = depth.getAskMap().keySet().parallelStream().min(Comparator.<BigDecimal>naturalOrder()).get();
        BigDecimal bid = depth.getBidMap().keySet().parallelStream().max(Comparator.<BigDecimal>naturalOrder()).get();

        if (ask.subtract(bid).compareTo(spread.multiply(BigDecimal.valueOf(2.1))) > 0){
            BigDecimal price = ask.add(bid).divide(BigDecimal.valueOf(2), 8, HALF_UP)
                    .add(spread.multiply(BigDecimal.valueOf(random.nextDouble())))
                    .setScale(8, HALF_UP);

            //log.info("onDepth -> {} {} {}", price, strategy.getSymbol(), Objects.toString(strategy.getSymbolType(), ""));

            action(price);
        }
    }

    private void action(BigDecimal price) {
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
        BigDecimal step = BigDecimal.valueOf(0.001);

        try {
            if (!getOrderMap().values().parallelStream()
                    .filter(order -> OrderType.LONG.contains(order.getType()))
                    .map(order -> order.getPrice().subtract(price))
                    .filter(d ->
                            (d.compareTo(ZERO) > 0 && d.compareTo(spread) < 0) ||
                                    (d.compareTo(ZERO) <= 0 && d.abs().compareTo(spreadX2) < 0))
                    .findAny()
                    .isPresent()){
                BigDecimal amount = strategy.getLevelLot();

                if (strategy.getSymbolType() == null){
                    amount = strategy.getLevelLot().multiply(BigDecimal.valueOf(1 + (random.nextDouble() / 2))).setScale(8, HALF_UP);
                }

                Future<Order> open = createOrderAsync(
                        new Order(strategy,
                                strategy.getSymbolType() != null ? OPEN_LONG : BID,
                                price.subtract(spread),
                                amount));

                if (strategy.getSymbolType() == null){
                    amount = strategy.getLevelLot().multiply(BigDecimal.valueOf(1 + (random.nextDouble() / 2))).setScale(8, HALF_UP);
                }

                Future<Order> close = createOrderAsync(
                        new Order(strategy,
                                strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                                price.subtract(step),
                                amount));

                open.get();
                close.get();
            }
        } catch (Exception e) {
            errorCount++;
            errorTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onCloseOrder(Order order) {
        if (errorCount > 0 && OrderType.SELL_SET.contains(order.getType())){
            errorCount--;
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(OrderStatus.CLOSED)){
            action(order.getAvgPrice());
        }
    }
}
