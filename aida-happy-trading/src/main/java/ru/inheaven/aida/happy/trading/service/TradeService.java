package ru.inheaven.aida.happy.trading.service;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import ru.inheaven.aida.happy.trading.entity.CacheKey;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ZERO;

/**
 * @author inheaven on 002 02.07.15 16:45
 */
@Singleton
public class TradeService {
    private ConnectableObservable<Trade> tradeObservable;

    private LoadingCache<CacheKey, BigDecimal> cache;

    @Inject
    public TradeService(FixService fixService, TradeMapper tradeMapper, BroadcastService broadcastService) {
        tradeObservable = fixService.getTradeObservable()
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .publish();
        tradeObservable.connect();

        tradeObservable.subscribe(tradeMapper::asyncSave);

        cache = CacheBuilder.newBuilder()
                .expireAfterWrite(5, TimeUnit.SECONDS)
                .build(new CacheLoader<CacheKey, BigDecimal>() {
                    @Override
                    public BigDecimal load(CacheKey key) throws Exception {
                        switch (key.getMethod()){
                            case "getStdDev":
                                return tradeMapper.getTradeStdDevPt(key.getExchangeType(), key.getSymbol(), key.getPoints());
                            case "getAvgAmount":
                                return tradeMapper.getTradeAvgAmountPt(key.getExchangeType(), key.getSymbol(), key.getPoints());
                            case "getAvgPrice":
                                return tradeMapper.getTradeAvgPricePt(key.getExchangeType(), key.getSymbol(), key.getPoints());
                        }

                        return ZERO;
                    }
                });
    }

    public ConnectableObservable<Trade> getTradeObservable() {
        return tradeObservable;
    }

    public BigDecimal getStdDev(ExchangeType exchangeType, String symbol, int points){
        try {
            return cache.get(new CacheKey("getStdDev", exchangeType, symbol, points));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public BigDecimal getAvgAmount(ExchangeType exchangeType, String symbol, int points){
        try {
            return cache.get(new CacheKey("getAvgAmount", exchangeType, symbol, points));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }

    public BigDecimal getAvgPrice(ExchangeType exchangeType, String symbol, int points){
        try {
            return cache.get(new CacheKey("getAvgPrice", exchangeType, symbol, points));
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
