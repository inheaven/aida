package ru.inheaven.aida.okex.strategy;

import com.google.common.collect.Lists;
import com.google.common.math.Stats;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.mapper.TradeMapper;
import ru.inheaven.aida.okex.service.ForecastService;
import ru.inheaven.aida.okex.service.InfluxService;
import ru.inheaven.aida.okex.util.Buffer;
import ru.inheaven.aida.okex.ws.OkexWsExchange;
import ru.inheaven.aida.okex.model.*;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.PI;
import static java.math.RoundingMode.HALF_EVEN;

@SuppressWarnings({"Duplicates", "WeakerAccess"})
public class FuturesWsStrategy {
    private Logger log = LoggerFactory.getLogger(FuturesWsStrategy.class);

    @Inject
    private OkexWsExchange okexWsExchange;

    @Inject
    private InfluxService influxService;

    @Inject
    private ForecastService forecastService;

    @Inject
    private TradeMapper tradeMapper;

    private Long strategyId;

    private String symbol;
    private String currency;
    private double longMinSpread;
    private double shortMinSpread;

    private volatile int longDelta;
    private volatile int shortDelta;

    private int stdDevSize;

    private Long initTime;

    private Buffer<Info> info = Buffer.create();
    private Buffer<Position> longPosition  = Buffer.create();
    private Buffer<Position> shortPosition  = Buffer.create();

    private Buffer<Trade> lastTrade  = Buffer.create();
    private Buffer<Depth> lastDepth  = Buffer.create();

    private Buffer<Double> stdDevBuffer;
    private Buffer<Double> stdDev = Buffer.create();

    private Buffer<Double> prediction = Buffer.create();
    private Buffer<Double> predictionAccBuffer;
    private Buffer<Double> predictionAcc = Buffer.create();

    private Buffer<Order> lastLong = Buffer.create();
    private Buffer<Order> lastShort = Buffer.create();

    private ConcurrentNavigableMap<Long, Order> marketOrderMap = new ConcurrentSkipListMap<>();
    private ConcurrentNavigableMap<Long, Order> stopOrderMap = new ConcurrentSkipListMap<>();

    private ConcurrentNavigableMap<Long, Order> limitOrderMap = new ConcurrentSkipListMap<>();
    private ConcurrentNavigableMap<Long, Order> stopLimitOrderMap = new ConcurrentSkipListMap<>();

    private FlowableProcessor<Double> actionLong = PublishProcessor.create();
    private FlowableProcessor<Double> actionShort = PublishProcessor.create();

    private FuturesWsStrategy sideStrategy;

    public FuturesWsStrategy(Long strategyId, String symbol, String currency, double longMinSpread, double shortMinSpread,
                             int longDelta, int shortDelta, int stdDevSize) {
        this.strategyId = strategyId;
        this.symbol = symbol;
        this.currency = currency;
        this.longMinSpread = longMinSpread;
        this.shortMinSpread = shortMinSpread;
        this.shortDelta = shortDelta;
        this.longDelta = longDelta;
        this.stdDevSize = stdDevSize;

        predictionAccBuffer = Buffer.create((int) (stdDevSize/2/PI));
        stdDevBuffer = Buffer.create(stdDevSize);
    }

    public void setSideStrategy(FuturesWsStrategy sideStrategy) {
        this.sideStrategy = sideStrategy;
    }

    private long levelPrice(BigDecimal price){
        return levelPrice(price.doubleValue());
    }

    private long levelPrice(double price){
        return Math.round(price*100);
    }

    @Inject
    private void init(){
        initTime = System.currentTimeMillis();

        stdDevBuffer.addAll(Lists.reverse(tradeMapper.getLastTrades(symbol, currency, stdDevSize).stream()
                .map(t -> t.getPrice().doubleValue())
                .collect(Collectors.toList())));
    }

    public Trade getLastTrade() {
        return lastTrade.peekLast();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Inject
    private void subscribe(){
        //info
        okexWsExchange.getInfos()
                .filter(i -> currency.equals(i.getCurrency()))
                .subscribe(i -> info.add(i));

        //position
        okexWsExchange.getPositions()
                .filter(p -> symbol.equals(p.getSymbol()) && currency.equals(p.getCurrency()))
                .subscribe(p -> {
                    try {
                        if ("long".equals(p.getType())){
                            longPosition.add(p);
                        }else if ("short".equals(p.getType())){
                            shortPosition.add(p);
                        }
                    } catch (Exception e) {
                        log.error("error position", e);
                    }
                });

        //trade
        Flowable<Trade> trades = okexWsExchange.getTrades()
                .filter(t -> symbol.equals(t.getSymbol()) && currency.equals(t.getCurrency()));

        trades.subscribe(t -> {
            try {
                ("buy".equals(t.getSide()) ? actionLong : actionShort).onNext(t.getPrice().doubleValue());
            } catch (Exception e) {
                log.error("error trades", e);
            }
        });
        trades.subscribe(t -> {
            try {
                lastTrade.add(t);
            } catch (Exception e) {
                log.error("error last trade", e);
            }
        });

        trades.subscribe(t -> {
            try {
                stdDevBuffer.add(t.getPrice().doubleValue());
            } catch (Exception e) {
                log.error("error update std dev", e);
            }
        });

        //depth
        Flowable<Depth> depths = okexWsExchange.getDepths()
                .filter(d -> symbol.equals(d.getSymbol()) && currency.equals(d.getCurrency()));
        depths.subscribe( d -> {
            try {
                actionShort.onNext(d.getAsk().doubleValue());
                actionLong.onNext(d.getBid().doubleValue());
            } catch (Exception e) {
                log.error("error depth", e);
            }
        });

        depths.subscribe(d -> lastDepth.add(d));

        //order
        Flowable<Order> orders = okexWsExchange.getOrders()
                .filter(o -> symbol.equals(o.getSymbol()) && currency.equals(o.getCurrency()));

//        orders.filter(o -> "filled".equals(o.getStatus()))
//                .subscribe(o -> {
//                    try {
//                        actions.onNext(o.getAvgPrice().doubleValue());
//                    } catch (Exception e) {
//                        log.error("error orders", e);
//                    }
//                });

        orders.filter(o -> "new".equals(o.getStatus()))
                .subscribe(o -> {
                    try {
                        if (o.getPrice() != null && o.getOrderId() != null) {
                            long moneyPrice = levelPrice(o.getPrice().doubleValue());

                            if ("market".equals(o.getType())){
                                marketOrderMap.put(moneyPrice, o);
                            }
                            if ("stop".equals(o.getType())){
                                stopOrderMap.put(moneyPrice, o);
                            }
                            if ("limit".equals(o.getType())){
                                limitOrderMap.put(moneyPrice, o);
                            }
                            if ("stop_limit".equals(o.getType())){
                                stopLimitOrderMap.put(moneyPrice, o);
                            }
                        }

                        if (getLastTrade() != null) {
                            if (longDelta > 0){
                                log.info("{} {} {} {} {} {} {} {}", symbol, currency, stdDev.peekLast(), getSpread( true), getLongQty(),
                                        getShift( true), prediction.peekLast(), getBalance());
                            }
                            if (shortDelta > 0){
                                log.info("{} {} {} {} {} {} {} {}", symbol, currency, stdDev.peekLast(), getSpread( false), getShortQty(),
                                        getShift(false), prediction.peekLast(),getBalance());
                            }
                        }
                    } catch (Exception e) {
                        log.error("error new order subscribe ", e);
                    }
                });

        orders.filter(o -> "filled".equals(o.getStatus()) || "canceled".equals(o.getStatus()) || "pending_cancel".equals(o.getStatus()))
                .subscribe(o -> {
                    try {
                        if ("market".equals(o.getType()) || "stop".equals(o.getType())){
                            lastLong.add(o);
                        }else if ("limit".equals(o.getType()) || "stop_limit".equals(o.getType())){
                            lastShort.add(o);
                        }

                        marketOrderMap.values().removeIf(o1 -> o.getOrderId().equals(o1.getOrderId()));
                        stopOrderMap.values().removeIf(o1 -> o.getOrderId().equals(o1.getOrderId()));
                        limitOrderMap.values().removeIf(o1 -> o.getOrderId().equals(o1.getOrderId()));
                        stopLimitOrderMap.values().removeIf(o1 -> o.getOrderId().equals(o1.getOrderId()));
                    } catch (Exception e) {
                        log.error("error remove filled canceled", e);
                    }
                });

        //action
        if (longDelta > 0) {
            actionLong.observeOn(Schedulers.single()).subscribe(p -> {
                try {
                    action(p, true);
                } catch (Exception e) {
                    log.error("error action long", e);
                }
            });
        }

        if (shortDelta > 0) {
            actionShort.observeOn(Schedulers.single()).subscribe(p -> {
                try {
                    action(p, false);
                } catch (Exception e) {
                    log.error("error action short", e);
                }
            });
        }

        //market
        trades.throttleLast(1, TimeUnit.MINUTES).subscribe(t -> {
            try {
                marketOrderMap.values().removeIf(o -> o.getPrice().compareTo(t.getPrice()) > 0);
                stopOrderMap.values().removeIf(o -> o.getPrice().compareTo(t.getPrice()) < 0);

                limitOrderMap.values().removeIf(o -> o.getPrice().compareTo(t.getPrice()) < 0);
                stopLimitOrderMap.values().removeIf(o -> o.getPrice().compareTo(t.getPrice()) > 0);
            } catch (Exception e) {
                log.error("error trade remove ", e);
            }
        });

    }

    @Inject
    private void schedule(){
        //stddev
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (!stdDevBuffer.isEmpty()){
                    stdDev.add(Stats.of(stdDevBuffer).sampleStandardDeviation());
                }
            } catch (Exception e) {
                log.error("std dev error ", e);
            }
        }, 60, 1, TimeUnit.SECONDS);

        //cancel
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                cancel(marketOrderMap);
                cancel(stopOrderMap);
                cancel(limitOrderMap);
                cancel(stopLimitOrderMap);

                BigDecimal buy = lastTrade.peekLast().getPrice().add(new BigDecimal(getSpread()));
                BigDecimal sell = lastTrade.peekLast().getPrice().subtract(new BigDecimal(getSpread()));

                marketOrderMap.values().removeIf(o -> o.getPrice().compareTo(buy) > 0);
                stopOrderMap.values().removeIf(o -> o.getPrice().compareTo(sell) < 0);

                limitOrderMap.values().removeIf(o -> o.getPrice().compareTo(sell) < 0);
                stopLimitOrderMap.values().removeIf(o -> o.getPrice().compareTo(buy) > 0);
            } catch (Exception e) {
                log.error("error order map cancel ", e);
            }
        }, 1, 1, TimeUnit.MINUTES);

        //print
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                Stream.of(marketOrderMap, stopOrderMap, limitOrderMap, stopLimitOrderMap)
                        .flatMap(m -> m.values().stream())
                        .forEach((v) -> log.info("marketOrderMap {} {} {} {} {} {}",
                                v.getSymbol(), v.getPrice(), v.getQty(), v.getType(), v.getOrderId(), v.getCreated()));
            } catch (Exception e) {
                log.error("error print order map ", e);
            }
        },1, 10, TimeUnit.MINUTES);

        //prediction
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (!stdDevBuffer.isEmpty()){
                    prediction.add(getPrediction(new ArrayList<>(stdDevBuffer.getDeque())));

                    //acceleration
                    predictionAccBuffer.add(prediction.peekLast());
                    predictionAcc.add(getPrediction(new ArrayList<>(predictionAccBuffer.getDeque())));
                }
            } catch (Exception e) {
                log.error("error prediction schedule ", e);
            }
        }, 60, 1, TimeUnit.SECONDS);

        //metric
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                influxService.getInfluxDB().write(Point.measurement("okex_strategy")
                        .tag("currency", currency)
                        .tag("symbol", symbol)
                        .addField("long_qty", getLongQty())
                        .addField("short_qty", getShortQty())
                        .addField("long_evening_up", getLongEveningUp())
                        .addField("short_evening_up", getShortEveningUp())
//                        .addField("long_shift_", getShift(true))
//                        .addField("short_shift_", getShift(false))
                        .addField("long_spread", getSpread(true))
                        .addField("short_spread", getSpread(false))
                        .addField("prediction", getPrediction())
                        .addField("prediction_acc", predictionAcc.peekLast())
                        .addField("forecast", getForecast())
                        .addField("std_dev", stdDev.peekLast())
                        .addField("balance_v", getBalance())
                        .addField("delta", getDelta())
                        .build());
            } catch (Exception e) {
                log.error("error metric schedule ", e);
            }
        }, 90, 1, TimeUnit.SECONDS);
    }

    private void cancel(ConcurrentNavigableMap<Long, Order> orderMap) {
        Trade lastTrade = getLastTrade();

        if (lastTrade != null) {
            orderMap.tailMap(levelPrice(lastTrade.getPrice().doubleValue() + 2*getSpread())).values()
                    .forEach(o -> {
                        if (o.getOrderId() != null) {
                            okexWsExchange.futureCancel(o.getCurrency(), o.getSymbol(), o.getOrderId());
                        }
                    });

            orderMap.headMap(levelPrice(lastTrade.getPrice().doubleValue() - 2*getSpread())).values()
                    .forEach(o -> {
                        if (o.getOrderId() != null) {
                            okexWsExchange.futureCancel(o.getCurrency(), o.getSymbol(), o.getOrderId());
                        }
                    });

            orderMap.values().removeIf(o -> o.getOrderId() == null && o.getCreated() != null &&
                    System.currentTimeMillis() - o.getCreated().getTime() > 10*60*1000);
        }
    }

    private double getSpread(){
        return longDelta > 0 ? getSpread( true) : getSpread( false);
    }

    private double getSpread(boolean side){
        return Math.max(stdDev.peekLast()/PI, 8);
    }

    public double getPrice(){
        return lastTrade.peekLast() != null ? lastTrade.peekLast().getPrice().doubleValue() : 0;
    }

    public double getLongVolume(){
        return longPosition.peekLast() != null
                ? 100*longPosition.peekLast().getQty()/longPosition.peekLast().getAvgPrice().doubleValue()
                : 0;
    }

    public double getShortVolume(){
        return shortPosition.peekLast() != null
                ? 100*shortPosition.peekLast().getQty()/shortPosition.peekLast().getAvgPrice().doubleValue()
                : 0;
    }

    public double getLongAvgPrice(){
        return longPosition.peekLast().getAvgPrice().doubleValue();
    }

    public double getShortAvgPrice(){
        return shortPosition.peekLast().getAvgPrice().doubleValue();
    }

    public int getLongQty(){
        return longPosition.peekLast() != null ? longPosition.peekLast().getQty() : 0;
    }

    public int getShortQty(){
        return shortPosition.peekLast() != null ? shortPosition.peekLast().getQty() : 0;
    }

    public int getLongEveningUp(){
        return longPosition.peekLast() != null ? longPosition.peekLast().getEveningUp().intValue() : 0;
    }

    public int getShortEveningUp(){
        return shortPosition.peekLast() != null ? shortPosition.peekLast().getEveningUp().intValue() : 0;
    }

    private double getShift(boolean side){
        return side ? getSpread(true) : getSpread(false)*getBalance()/PI;
    }

    private final static BigDecimal HUNDRED = new BigDecimal("100");

    public int getOwnQty(){
        Info i = info.peekLast();
        Trade t = lastTrade.peekLast();

        if (i == null || t == null){
            return 1;
        }

        return Math.max(i.getBalance().add(i.getProfit()).multiply(t.getPrice()).divide(HUNDRED, 8, HALF_EVEN).intValue(), 1);
    }

    private double getBalance(){
        if (sideStrategy != null) {
            return (100*getOwnQty()*getForecast() + 100*sideStrategy.getOwnQty()*sideStrategy.getForecast() +
                    getShortVolume()*getPrice() + sideStrategy.getShortVolume()*sideStrategy.getPrice() -
                    getLongVolume()*getPrice() - sideStrategy.getLongVolume()*sideStrategy.getPrice())/
                    (100*getOwnQty() + 100*sideStrategy.getOwnQty())/2;
        }else{
            int fQty = Math.max(getOwnQty(), 1);

            return (100*fQty*getForecast() + getShortVolume()*getPrice() - getLongVolume()*getPrice())/(100*getOwnQty());
        }
    }

    private int getDelta(){
        if (sideStrategy != null) {

            return (getLongQty() + sideStrategy.getLongQty() - getShortQty() - sideStrategy.getShortQty());
        }else{
            return getLongQty() - getShortQty();
        }
    }

    private double getForecast(){
        return forecastService.getForecast(symbol, currency);
    }

    private void action(double price, boolean side) {
        if (System.currentTimeMillis() - initTime < 100000){
            return;
        }

        double spread = getSpread(side);
        double priceS = price + getShift(side);
        double buy = side ? priceS : priceS - spread;
        double sell = side ? priceS + spread : priceS;
        double balance = getBalance();

        if (trade(buy, sell, spread, side)){
            BigDecimal roundBuy = new BigDecimal(buy).setScale(2, HALF_EVEN);
            BigDecimal roundBuy2 = new BigDecimal(buy).setScale(2, HALF_EVEN);

            BigDecimal roundSell = new BigDecimal(sell).setScale(2, HALF_EVEN);
            BigDecimal roundSell2 = new BigDecimal(sell).setScale(2, HALF_EVEN);

            if (side){
                marketOrderMap.put(levelPrice(roundBuy), new Order(symbol, currency, "buy", "market", roundBuy, 1));
                okexWsExchange.futureTrade(currency, symbol, roundBuy.toPlainString(), 1, 1, 0, 20);

                stopOrderMap.put(levelPrice(roundSell), new Order(symbol, currency, "buy", "stop", roundSell, 1));
                okexWsExchange.futureTrade(currency, symbol, roundSell.toPlainString(), 1, 3, 0, 20);

//                if (balance >= 0.5) {
//                    marketOrderMap.put(levelPrice(roundBuy2), new Order(symbol, currency, "buy", "market", roundBuy2, 1));
//                    okexWsExchange.futureTrade(currency, symbol, roundBuy2.toPlainString(), 1, 1, 0, 20);
//                }else if (balance <= -0.5){
//                    stopOrderMap.put(levelPrice(roundSell2), new Order(symbol, currency, "buy", "stop", roundSell2, 1));
//                    okexWsExchange.futureTrade(currency, symbol, roundSell2.toPlainString(), 1, 3, 0, 20);
//                }
            }else{
                limitOrderMap.put(levelPrice(roundSell), new Order(symbol, currency, "sell", "limit", roundSell, 1));
                okexWsExchange.futureTrade(currency, symbol, roundBuy.toPlainString(), 1, 4, 0, 20);

                stopLimitOrderMap.put(levelPrice(roundBuy), new Order(symbol, currency, "sell", "stop_limit", roundBuy, 1));
                okexWsExchange.futureTrade(currency, symbol, roundSell.toPlainString(), 1, 2, 0, 20);

//                if (balance >= 0.5) {
//                    stopLimitOrderMap.put(levelPrice(roundBuy2), new Order(symbol, currency, "sell", "stop_limit", roundBuy2, 1));
//                    okexWsExchange.futureTrade(currency, symbol, roundBuy2.toPlainString(), 1, 4, 0, 20);
//                }else if (balance <= -0.5){
//                    limitOrderMap.put(levelPrice(roundSell2), new Order(symbol, currency, "sell", "limit", roundSell2, 1));
//                    okexWsExchange.futureTrade(currency, symbol, roundSell2.toPlainString(), 1, 2, 0, 20);
//                }
            }
        }
    }

    private boolean trade(double buy, double sell, double spread, boolean side){
        return trade(buy, spread, side ? marketOrderMap : stopLimitOrderMap) &&
                trade(sell, spread, side ? stopOrderMap : limitOrderMap);

    }

    private boolean trade(double price, double spread, ConcurrentNavigableMap<Long, Order> orderMap){
        long p = levelPrice(price);
        long s = levelPrice(spread);

        Long min = orderMap.floorKey(p);

        if (min != null && p - min < s){
            return false;
        }

        Long max = orderMap.ceilingKey(p);

        return max == null || max - p > s;
    }

    private boolean trade(double buyPrice, double sellPrice, double spread, Order lastOrder) {
        if (lastOrder != null){
            double p = lastOrder.getAvgPrice().doubleValue();

            return Math.abs(p - buyPrice) > spread && Math.abs(p - sellPrice) > spread;
        }

        return true;
    }

    private Random random = new SecureRandom();

    private double getPrediction(List<Double> prices){
        int size = prices.size();

        int up = 0;
        int down = 0;

        for (int i = 0; i < size/2/ PI; ++i){
            int p1 = random.nextInt(size);
            int p2 = random.nextInt(size);

            if (p1 != p2 && !Objects.equals(prices.get(p1), prices.get(p2))) {
                if (p1 < p2 == prices.get(p1) < prices.get(p2)){
                    up++;
                }else{
                    down++;
                }
            }
        }

        return up + down > 0 ? (double) (up - down)/(up + down) : 0;
    }

    private double getPrediction(){
        Double p = prediction.peekLast();

        return p != null ? p : 0;
    }

    private double getPredictionMiddle(List<Double> prices){
        int size = prices.size();

        int up = 0;
        int down = 0;

        for (int i = 0; i < size/2/ PI; ++i){
            int p1 = random.nextInt(size/2);
            int p2 = size/2 + random.nextInt(size/2);

            if (!Objects.equals(prices.get(p1), prices.get(p2))) {
                if (prices.get(p1) < prices.get(p2)){
                    up++;
                }else{
                    down++;
                }
            }
        }

        return up + down > 0 ? (double) (up - down)/(up + down) : 0;
    }

    private double getPredictionAcc(){
        Double p = predictionAcc.peekLast();

        return p != null ? p : 0;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getCurrency() {
        return currency;
    }

    public Info getInfo() {
        return info.peekLast();
    }
}
