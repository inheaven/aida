package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.mapper.StrategyMapper;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author inheaven on 29.06.2015 23:35.
 */
@Singleton
public class StrategyService {
    private StrategyMapper strategyMapper;

    public StrategyService() {

    }

    @Inject
    public StrategyService(StrategyMapper strategyMapper) {
        this.strategyMapper = strategyMapper;


    }
}
