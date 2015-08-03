package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Depth;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.mapper.DepthMapper;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Objects;

/**
 * @author inheaven on 10.07.2015 0:22.
 */
@Singleton
public class DepthService {
    private ConnectableObservable<Depth> depthObservable;

    @Inject
    public DepthService(OkcoinService okcoinService, DepthMapper depthMapper) {
        depthObservable = okcoinService.createFutureDepthObservable()
                .mergeWith(okcoinService.createSpotDepthObservable())
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        depthObservable.connect();

        depthObservable.subscribe(depthMapper::asyncSave);
    }

    public Observable<Depth> createDepthObservable(Strategy strategy){
        return depthObservable.filter(d -> Objects.equals(strategy.getAccount().getExchangeType(), d.getExchangeType()))
                .filter(d -> Objects.equals(strategy.getSymbol(), d.getSymbol()))
                .filter(d -> Objects.equals(strategy.getSymbolType(), d.getSymbolType()));
    }
}
