package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.StrategyType;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.mapper.StrategyMapper;
import ru.inheaven.aida.happy.trading.strategy.BaseStrategy;
import ru.inheaven.aida.happy.trading.strategy.ParagliderStrategy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * @author inheaven on 29.06.2015 23:35.
 */
@Singleton
public class StrategyService {
    private List<BaseStrategy> baseStrategies;

    @Inject
    public StrategyService(StrategyMapper strategyMapper, OrderService orderService, OrderMapper orderMapper,
                           TradeService tradeService) {
        List<Strategy> strategies = strategyMapper.getActiveStrategies();

        strategies.forEach(s -> {
            if (s.getType().equals(StrategyType.PARAGLIDER)){
                baseStrategies.add(new ParagliderStrategy(s, orderService, orderMapper, tradeService));
            }
        });
    }

    public List<BaseStrategy> getBaseStrategies() {
        return baseStrategies;
    }
}
