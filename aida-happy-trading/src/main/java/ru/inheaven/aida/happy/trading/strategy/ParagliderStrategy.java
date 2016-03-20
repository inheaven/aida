package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderType.*;

/**
 * @author inheaven on 29.06.2015 23:38.
 */
public class ParagliderStrategy extends BaseStrategy{
    private Strategy strategy;
    private OrderMapper orderMapper;

    private int errorCount = 0;
    private long errorTime = 0;

    private int orderPositionDelta = 0;

    private SecureRandom random = new SecureRandom("ParagliderStrategy".getBytes());

    private BigDecimal middlePrice;

    public ParagliderStrategy(Strategy strategy, AccountMapper accountMapper, OrderService orderService, OrderMapper orderMapper,
                              TradeService tradeService, DepthService depthService) {
        super(strategy);

        this.strategy = strategy;
        this.orderMapper = orderMapper;
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

        BigDecimal spread = strategy.getBigDecimal(LevelParameter.SPREAD);
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

            if (!getOrderMap().values().parallelStream()
                    .filter(order -> OrderType.SHORT.contains(order.getType()))
                    .map(order -> order.getPrice().subtract(trade.getPrice()))
                    .filter(d ->
                            (d.compareTo(ZERO) > 0 && d.compareTo(spreadX2) < 0) ||
                                    (d.compareTo(ZERO) <= 0 && d.abs().compareTo(spread) < 0))
                    .findAny()
                    .isPresent()){

                createOrderAsync(new Order(strategy, OPEN_SHORT, trade.getPrice().add(spread), ONE)).get();
                createOrderAsync(new Order(strategy, CLOSE_SHORT, trade.getPrice().add(step), ONE)).get();
            }
        } catch (Exception e) {
            errorCount++;
            errorTime = System.currentTimeMillis();
        }
    }

    @Override
    protected void onCloseOrder(Order o) {
        Map<OrderType, OrderPosition> map = orderMapper.getOrderPositionMap(strategy);

        orderPositionDelta = 0;

        if (map.get(OPEN_LONG) != null){
            orderPositionDelta += map.get(OPEN_LONG).getCount();
        }
        if (map.get(CLOSE_LONG) != null){
            orderPositionDelta -= map.get(CLOSE_LONG).getCount();
        }
        if (map.get(OPEN_SHORT) != null){
            orderPositionDelta -= map.get(OPEN_SHORT).getCount();
        }
        if (map.get(CLOSE_SHORT) != null){
            orderPositionDelta += map.get(CLOSE_SHORT).getCount();
        }
    }
}
