package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.mapper.StrategyMapper;
import ru.inheaven.aida.happy.trading.strategy.BaseStrategy;
import ru.inheaven.aida.happy.trading.strategy.LevelStrategy;
import ru.inheaven.aida.happy.trading.strategy.ParagliderStrategy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author inheaven on 29.06.2015 23:35.
 */
@Singleton
public class StrategyService {
    private List<BaseStrategy> baseStrategies = new ArrayList<>();

    @Inject
    public StrategyService(StrategyMapper strategyMapper, OrderService orderService, OrderMapper orderMapper,
                           TradeService tradeService, DepthService depthService, UserInfoService userInfoService) {
        List<Strategy> strategies = strategyMapper.getActiveStrategies();

        strategies.stream()
                .filter(Strategy::isActive)
                .forEach(s -> {
            BaseStrategy baseStrategy;

            switch (s.getType()) {
                case LEVEL:
                    baseStrategy = new LevelStrategy(s, orderService, orderMapper, tradeService, depthService, userInfoService);
                    baseStrategy.start();
                    break;
                case PARAGLIDER:
                    baseStrategy = new ParagliderStrategy(s, orderService, orderMapper, tradeService, depthService);
                    baseStrategy.start();
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            baseStrategies.add(baseStrategy);
        });

        //start user info service
        Module.getInjector().getInstance(UserInfoService.class);
    }

    public List<BaseStrategy> getBaseStrategies() {
        return baseStrategies;
    }
}
