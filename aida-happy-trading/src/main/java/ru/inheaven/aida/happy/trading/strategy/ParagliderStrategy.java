package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.exception.CreateOrderException;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;

import java.math.BigDecimal;

import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 29.06.2015 23:38.
 */
public class ParagliderStrategy extends BaseStrategy{
    private Strategy strategy;

    private int errorCount = 0;
    private long errorTime = 0;

    public ParagliderStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper,
                              TradeService tradeService) {
        super(strategy, orderService, orderMapper, tradeService);

        this.strategy = strategy;
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
                BigDecimal priceDelta = priceSpread.divide(BigDecimal.valueOf(2), HALF_UP);

                createOrder(new Order(strategy, OrderType.OPEN_LONG, trade.getPrice().subtract(priceDelta), BigDecimal.ONE));
                createOrder(new Order(strategy, OrderType.CLOSE_SHORT, trade.getPrice().subtract(priceDelta), BigDecimal.ONE));

                createOrder(new Order(strategy, OrderType.OPEN_SHORT, trade.getPrice().add(priceDelta), BigDecimal.ONE));
                createOrder(new Order(strategy, OrderType.CLOSE_LONG, trade.getPrice().add(priceDelta), BigDecimal.ONE));
            } catch (CreateOrderException e) {
                errorCount++;
                errorTime = System.currentTimeMillis();
            }
        }
    }
}
