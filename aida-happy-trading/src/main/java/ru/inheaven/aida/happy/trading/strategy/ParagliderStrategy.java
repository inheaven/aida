package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;

/**
 * @author inheaven on 29.06.2015 23:38.
 */
public class ParagliderStrategy extends BaseStrategy{
    private Strategy strategy;
    private OrderService orderService;

    public ParagliderStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper,
                              TradeService tradeService) {
        super(strategy, orderService, orderMapper, tradeService);

        this.strategy = strategy;
        this.orderService = orderService;



    }


}
