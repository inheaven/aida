package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.entity.SymbolType;
import ru.inheaven.aida.happy.trading.mapper.StrategyMapper;
import ru.inheaven.aida.happy.trading.strategy.BaseStrategy;
import ru.inheaven.aida.happy.trading.strategy.LevelStrategy;

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
    public StrategyService(StrategyMapper strategyMapper) {
        List<Strategy> strategies = strategyMapper.getActiveStrategies();

        strategies.stream()
                .filter(Strategy::isActive)
                .forEach(s -> {
            BaseStrategy baseStrategy;

            switch (s.getType()) {
                case LEVEL_SPOT:
                case LEVEL_FUTURES:
                    baseStrategy = new LevelStrategy(s);
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
                .filter(bs -> bs.getAccount().getExchangeType().equals(exchangeType))
                .filter(bs -> bs.getStrategy().getSymbol().equals(symbol))
                .filter(bs -> Objects.equals(bs.getStrategy().getSymbolType(), symbolType))
                .collect(Collectors.toList());
    }
}
