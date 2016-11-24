package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import rx.Observable;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.TimeUnit;

import static java.math.BigDecimal.ZERO;
import static ru.inheaven.aida.happy.trading.entity.ExchangeType.OKCOIN_CN;
import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 002 02.07.15 16:45
 */
@Singleton
public class TradeService {
    private Logger log = LoggerFactory.getLogger(TradeService.class);

    private Observable<Trade> tradeObservable;


    @Inject
    public TradeService(FixService fixService, TradeMapper tradeMapper, BroadcastService broadcastService, InfluxService influxService) {
        ConnectableObservable<Trade> tradeObservable = fixService.getTradeObservable()
                .onBackpressureBuffer()
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .publish();
        tradeObservable.connect();

        tradeObservable.subscribe(tradeMapper::asyncSave);

        this.tradeObservable = tradeObservable.asObservable();

        //trade metric
        tradeObservable
                .filter(t -> t.getExchangeType().equals(OKCOIN_CN))
                .filter(t -> t.getSymbol().equals("BTC/CNY"))
                .buffer(1, TimeUnit.SECONDS)
                .subscribe(trades -> {
                    try {
                        BigDecimal askPrice = ZERO;
                        BigDecimal askVolume = ZERO;
                        Integer askCount = 0;

                        BigDecimal bidPrice = ZERO;
                        BigDecimal bidVolume = ZERO;
                        Integer bidCount = 0;

                        for (Trade trade : trades){
                            if (trade.getOrderType().equals(ASK)){
                                askCount++;
                                askVolume = askVolume.add(trade.getAmount());
                                askPrice = askPrice.add(trade.getPrice().multiply(trade.getAmount()));
                            }else if (trade.getOrderType().equals(BID)){
                                bidCount++;
                                bidVolume = bidVolume.add(trade.getAmount());
                                bidPrice = bidPrice.add(trade.getPrice().multiply(trade.getAmount()));
                            }
                        }

                        askPrice = askVolume.compareTo(ZERO) > 0 ? askPrice.divide(askVolume, 8, RoundingMode.HALF_EVEN) : null;
                        bidPrice = bidVolume.compareTo(ZERO) > 0 ? bidPrice.divide(bidVolume, 8, RoundingMode.HALF_EVEN) : null;

                        influxService.addTradeMetric(OKCOIN_CN, "BTC/CNY", askPrice, askVolume, askCount,
                                bidPrice, bidVolume, bidCount);
                    } catch (Exception e) {
                        log.error("error add trade metric", e);
                    }

                });
    }

    public Observable<Trade> getTradeObservable() {
        return tradeObservable;
    }
}
