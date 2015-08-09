package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;

/**
 * @author inheaven on 07.08.2015 0:32.
 */
public class ArbitrageStrategy extends BaseStrategy{

    public ArbitrageStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper,
                             TradeService tradeService, DepthService depthService) {
        super(strategy, orderService, orderMapper, tradeService, depthService);
    }

    private void action(){



    }
}
