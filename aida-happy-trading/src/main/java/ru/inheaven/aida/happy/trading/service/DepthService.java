package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Depth;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.mapper.DepthMapper;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author inheaven on 10.07.2015 0:22.
 */
@Singleton
public class DepthService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private ConnectableObservable<Depth> depthObservable;

    private Map<String, Depth> depthMap = new ConcurrentHashMap<>();

    @Inject
    public DepthService(DepthMapper depthMapper, OkcoinCnFixService okcoinCnFixService,
                        BroadcastService broadcastService) {
        depthObservable = okcoinCnFixService.getDepthObservable()
                .onBackpressureBuffer()
                .observeOn(Schedulers.computation())
                .publish();
        depthObservable.connect();

        depthObservable.subscribe(d -> {
            try {
                String key = d.getExchangeType() + d.getSymbol() + d.getSymbolType();

                Depth d0 = depthMap.get(key);

                if (d0 == null || d0.getAsk().compareTo(d.getAsk()) != 0 || d0.getBid().compareTo(d.getBid()) != 0){
                    //depthMapper.asyncSave(d); todo
                }

                depthMap.put(key, d);
            } catch (Exception e) {
                log.error("error store depth -> ", e);
            }
        });

        depthObservable.sample(500, TimeUnit.MILLISECONDS).subscribe(d -> broadcastService.broadcast(getClass(), "depth", d));
    }

    public Observable<Depth> createDepthObservable(Strategy strategy){
        return depthObservable.filter(d -> Objects.equals(strategy.getAccount().getExchangeType(), d.getExchangeType()))
                .filter(d -> Objects.equals(strategy.getSymbol(), d.getSymbol()))
                .filter(d -> Objects.equals(strategy.getSymbolType(), d.getSymbolType()));
    }
}
