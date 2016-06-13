package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.SymbolType;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.mapper.StrategyMapper;
import ru.inheaven.aida.happy.trading.strategy.BaseStrategy;
import ru.inheaven.aida.happy.trading.strategy.LevelStrategy;
import ru.inheaven.aida.happy.trading.strategy.ParagliderStrategy;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author inheaven on 29.06.2015 23:35.
 */
@Singleton
public class StrategyService {
    private List<BaseStrategy> baseStrategies = new ArrayList<>();

    @Inject
    public StrategyService(StrategyMapper strategyMapper, OrderService orderService, OrderMapper orderMapper,
                           TradeService tradeService, DepthService depthService, UserInfoService userInfoService,
                           XChangeService xChangeService) {
        List<Strategy> strategies = strategyMapper.getActiveStrategies();

        strategies.stream()
                .filter(Strategy::isActive)
                .forEach(s -> {
            BaseStrategy baseStrategy;

            switch (s.getType()) {
                case LEVEL_SPOT:
                case LEVEL_FUTURES:
                    baseStrategy = new LevelStrategy(this, s, orderService, orderMapper, tradeService, depthService,
                            userInfoService, xChangeService);
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
    }

    public List<BaseStrategy> getBaseStrategies() {
        return baseStrategies;
    }

    public List<BaseStrategy> getStrategies(ExchangeType exchangeType, String symbol, SymbolType symbolType){
        return baseStrategies.stream()
                .filter(bs -> bs.getStrategy().getAccount().getExchangeType().equals(exchangeType))
                .filter(bs -> bs.getStrategy().getSymbol().equals(symbol))
                .filter(bs -> Objects.equals(bs.getStrategy().getSymbolType(), symbolType))
                .collect(Collectors.toList());
    }

    public boolean isMaxProfit(Long strategyId){
        return baseStrategies.stream()
                .max((s1, s2) -> s1.getProfit().compareTo(s2.getProfit()))
                .filter(s -> s.getStrategy().getId().equals(strategyId))
                .isPresent();
    }


}
