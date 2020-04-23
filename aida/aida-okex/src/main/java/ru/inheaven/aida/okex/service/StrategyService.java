package ru.inheaven.aida.okex.service;

import ru.inheaven.aida.okex.strategy.DeltaWsStrategy;
import ru.inheaven.aida.okex.strategy.FuturesWsStrategy;
import ru.inheaven.aida.okex.Module;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class StrategyService {
    @Inject
    public void init(){
        FuturesWsStrategy strategy;

        Module.getInjector().injectMembers(strategy = new FuturesWsStrategy(1L,"this_week", "btc_usd",
                0.001, 0.001, 0, 20, 1024));
        Module.getInjector().injectMembers(new DeltaWsStrategy(strategy, strategy, null));
    }
}
