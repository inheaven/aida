package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Comparator;
import java.util.concurrent.Future;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderType.CLOSE_LONG;
import static ru.inheaven.aida.happy.trading.entity.OrderType.OPEN_LONG;

/**
 * @author inheaven on 08.07.2015 19:39.
 */
public class LevelStrategy extends BaseStrategy{
    private Logger log = LoggerFactory.getLogger(getClass());
    private Strategy strategy;

    private int errorCount = 0;
    private long errorTime = 0;

    private SecureRandom random = new SecureRandom("LevelStrategy".getBytes());

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService) {
        super(strategy, orderService, orderMapper, tradeService, depthService);

        this.strategy = strategy;
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
            BigDecimal price = ask.add(bid).divide(BigDecimal.valueOf(2), 8, HALF_UP).add(spread);

            log.info("onDepth -> {} {}", price, strategy.getSymbolType());

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

        BigDecimal risk = ONE;
        if (strategy.getSymbol().equals("LTC/USD") && price.compareTo(BigDecimal.valueOf(3.2)) < 0){
            risk = BigDecimal.valueOf(3);
        }
        if (strategy.getSymbol().equals("BTC/USD") && price.compareTo(BigDecimal.valueOf(210)) < 0){
            risk = BigDecimal.valueOf(3);
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
                Future<Order> open = createOrderAsync(new Order(strategy, OPEN_LONG, price.subtract(spread), ONE));
                Future<Order> close = createOrderAsync(new Order(strategy, CLOSE_LONG, price.subtract(step), ONE));

                open.get();
                close.get();
            }
        } catch (Exception e) {
            errorCount++;
            errorTime = System.currentTimeMillis();
        }
    }
}
