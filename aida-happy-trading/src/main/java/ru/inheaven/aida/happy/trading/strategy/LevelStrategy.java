package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;

import java.math.BigDecimal;
import java.security.SecureRandom;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderType.CLOSE_LONG;
import static ru.inheaven.aida.happy.trading.entity.OrderType.OPEN_LONG;

/**
 * @author inheaven on 08.07.2015 19:39.
 */
public class LevelStrategy extends BaseStrategy{
    private Strategy strategy;

    private int errorCount = 0;
    private long errorTime = 0;

    private SecureRandom random = new SecureRandom("LevelStrategy".getBytes());

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService) {
        super(strategy, orderService, orderMapper, tradeService);

        this.strategy = strategy;
    }

    @Override
    protected void onTrade(Trade trade) {
        if (errorCount > 10){
            if (System.currentTimeMillis() - errorTime < 60000){
                return;
            }else{
                errorCount = 0;
                errorTime = 0;
            }
        }

        BigDecimal spread = strategy.getLevelSpread();
        BigDecimal spreadX2 = spread.multiply(BigDecimal.valueOf(2)).setScale(8, HALF_UP);
        BigDecimal step = BigDecimal.valueOf(0.001);

        try {
            if (!getOrderMap().values().parallelStream()
                    .filter(order -> OrderType.LONG.contains(order.getType()))
                    .map(order -> order.getPrice().subtract(trade.getPrice()))
                    .filter(d ->
                            (d.compareTo(ZERO) > 0 && d.compareTo(spread) < 0) ||
                                    (d.compareTo(ZERO) <= 0 && d.abs().compareTo(spreadX2) < 0))
                    .findAny()
                    .isPresent()){
                createOrderAsync(new Order(strategy, OPEN_LONG, trade.getPrice().subtract(spread), ONE)).get();
                createOrderAsync(new Order(strategy, CLOSE_LONG, trade.getPrice().subtract(step), ONE)).get();
            }
        } catch (Exception e) {
            errorCount++;
            errorTime = System.currentTimeMillis();
        }
    }
}
