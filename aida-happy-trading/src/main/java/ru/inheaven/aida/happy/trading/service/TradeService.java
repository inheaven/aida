package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import rx.observables.ConnectableObservable;
import rx.schedulers.Schedulers;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author inheaven on 002 02.07.15 16:45
 */
@Singleton
public class TradeService {
    private ConnectableObservable<Trade> tradeObservable;

    private Map<String, BigDecimal> stdDevMap = new ConcurrentHashMap<>();

    private final static BigDecimal TWO = new BigDecimal(2);
    @Inject
    public TradeService(OkcoinService okcoinService, OkcoinFixService okcoinFixService,
                        OkcoinCnFixService okcoinCnFixService,
                        TradeMapper tradeMapper, BroadcastService broadcastService) {
        tradeObservable = okcoinCnFixService.getTradeObservable()
                .onBackpressureBuffer()
                .observeOn(Schedulers.computation())
                .subscribeOn(Schedulers.computation())
                .publish();
        tradeObservable.connect();

        tradeObservable.subscribe(tradeMapper::asyncSave);

//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            BigDecimal bid = tradeMapper.getTradeStdDevPtType("BTC/CNY", 50000, OrderType.BID);
//            stdDevMap.put("bid", bid);
//
//            BigDecimal ask = tradeMapper.getTradeStdDevPtType("BTC/CNY", 50000, OrderType.ASK);
//            stdDevMap.put("ask", ask);
//
//            stdDevMap.put("BTC/CNY_1", bid.add(ask).divide(TWO, RoundingMode.HALF_EVEN)); //44
//            //stdDevMap.put("BTC/CNY_2", tradeMapper.getTradeStdDevPt("BTC/CNY", 8000)); //45
////            stdDevMap.put("BTC/CNY_3", bid.add(ask).divide(TWO, RoundingMode.HALF_EVEN)); //46
////            stdDevMap.put("BTC/CNY_4", tradeMapper.getTradeStdDevPt("BTC/CNY", 4000)); //47
////            stdDevMap.put("BTC/CNY_5", tradeMapper.getTradeStdDevPt("BTC/CNY", 5000)); //48
////            stdDevMap.put("BTC/CNY_6", tradeMapper.getTradeStdDevPt("BTC/CNY", 6000)); //49
////            stdDevMap.put("BTC/CNY_7", tradeMapper.getTradeStdDevPt("BTC/CNY", 7000)); //50
//
//            stdDevMap.put("av_BTC/CNY_1", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 100));
////            stdDevMap.put("av_BTC/CNY_2", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 10000));
////            stdDevMap.put("av_BTC/CNY_3", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 10500));
////            stdDevMap.put("av_BTC/CNY_4", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 23000));
////            stdDevMap.put("av_BTC/CNY_5", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 29000));
////            stdDevMap.put("av_BTC/CNY_6", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 31000));
////            stdDevMap.put("av_BTC/CNY_7", tradeMapper.getTradeAvgAmountPt("BTC/CNY", 37000));
//
//            stdDevMap.put("ap_BTC/CNY_1", tradeMapper.getTradeAvgPricePt("BTC/CNY", 100));
//
//            stdDevMap.put("BTC/CNY", stdDevMap.get("BTC/CNY_1"));
//            stdDevMap.put("av_BTC/CNY", stdDevMap.get("av_BTC/CNY_1"));
//            stdDevMap.put("ap_BTC/CNY", stdDevMap.get("ap_BTC/CNY_1"));
//        }, 0, 60, TimeUnit.SECONDS);
    }

    public ConnectableObservable<Trade> getTradeObservable() {
        return tradeObservable;
    }

    public BigDecimal getValue(String key){
        return stdDevMap.get(key);
    }

    public BigDecimal getStdDev(String symbol, String suffix){
        BigDecimal value = stdDevMap.get(symbol + suffix);

        return value != null ? value : stdDevMap.get(symbol);
    }

    public BigDecimal getAvgAmount(String symbol, String suffix){
        BigDecimal value = stdDevMap.get("av_" + symbol + suffix);

        return value != null ? value : stdDevMap.get("av_" + symbol);
    }

    public BigDecimal getAvgPrice(String symbol, String suffix){
        BigDecimal value = stdDevMap.get("ap_" + symbol + suffix);

        return value != null ? value : stdDevMap.get("ap_" + symbol);
    }
}
