package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;

import static java.lang.Math.signum;
import static java.math.BigDecimal.ONE;
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

    public ParagliderStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper,
                              TradeService tradeService) {
        super(strategy, orderService, orderMapper, tradeService);

        this.strategy = strategy;
        this.orderMapper = orderMapper;
    }

    @Override
    protected void onTrade(Trade trade) {
        if (errorCount > 10){
            if (System.currentTimeMillis() - errorTime < 600000){
                return;
            }else{
                errorCount = 0;
                errorTime = 0;
            }
        }

        BigDecimal priceSpread = trade.getPrice().multiply(strategy.getLevelSpread());

        if (!getOrderMap().values().parallelStream()
                .filter(order -> order.getPrice().subtract(trade.getPrice()).abs().compareTo(priceSpread) < 0)
                .findAny()
                .isPresent()){
            try {
                BigDecimal delta = priceSpread.divide(BigDecimal.valueOf(2), HALF_UP);

                createOrder(new Order(strategy, OPEN_LONG, trade.getPrice().subtract(delta).subtract(getBalance(delta)), ONE));
                createOrder(new Order(strategy, CLOSE_SHORT, trade.getPrice().subtract(delta).subtract(getBalance(delta)), ONE));

                createOrder(new Order(strategy, OPEN_SHORT, trade.getPrice().add(delta).subtract(getBalance(delta)), ONE));
                createOrder(new Order(strategy, CLOSE_LONG, trade.getPrice().add(delta).subtract(getBalance(delta)), ONE));
            } catch (CreateOrderException e) {
                errorCount++;
                errorTime = System.currentTimeMillis();
            }
        }
    }

    private BigDecimal getBalance(BigDecimal delta){
        return delta.multiply(BigDecimal.valueOf(random.nextDouble() * signum(orderPositionDelta)));
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
