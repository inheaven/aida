package ru.inheaven.aida.okex.strategy;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.google.common.collect.EvictingQueue;
import com.google.common.collect.Queues;
import com.google.common.math.Stats;
import com.google.common.util.concurrent.AtomicDouble;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import org.influxdb.dto.Point;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.fix.OkexExchange;
import ru.inheaven.aida.okex.mapper.StrategyMapper;
import ru.inheaven.aida.okex.mapper.TradeMapper;
import ru.aida.okex.model.*;
import ru.inheaven.aida.okex.service.ForecastOrderService;
import ru.inheaven.aida.okex.service.InfluxService;
import ru.inheaven.aida.okex.model.*;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Math.PI;
import static java.math.RoundingMode.HALF_EVEN;
import static quickfix.field.PositionEffect.CLOSE;
import static quickfix.field.PositionEffect.OPEN;
import static quickfix.field.Side.BUY;
import static quickfix.field.Side.SELL;

@SuppressWarnings("Duplicates")
public class FuturesStrategy {
    private Logger log = LoggerFactory.getLogger(FuturesStrategy.class);

    @Inject
    private OkexExchange okexExchange;

    @Inject
    private InfluxService influxService;

    @Inject
    private ForecastOrderService forecastOrderService;

    @Inject
    private StrategyMapper strategyMapper;

    @Inject
    private TradeMapper tradeMapper;

    private Long strategyId;

    private String symbol;
    private String currency;
    private double longMinSpread;
    private double shortMinSpread;

    private volatile int longDelta;
    private volatile int shortDelta;

    private AtomicReference<Info>  info = new AtomicReference<>();
    private AtomicReference<Position> longPosition  = new AtomicReference<>();
    private AtomicReference<Position> shortPosition  = new AtomicReference<>();

    private AtomicReference<Trade> lastTrade  = new AtomicReference<>();
    private AtomicReference<Depth> lastDepth  = new AtomicReference<>();

    private Queue<Double> stdDevBuffer;
    private AtomicDouble stdDev = new AtomicDouble();

    private ConcurrentNavigableMap<Long, Order> marketOrderMap = new ConcurrentSkipListMap<>();
    private ConcurrentNavigableMap<Long, Order> stopOrderMap = new ConcurrentSkipListMap<>();

    private ConcurrentNavigableMap<Long, Order> limitOrderMap = new ConcurrentSkipListMap<>();
    private ConcurrentNavigableMap<Long, Order> stopLimitOrderMap = new ConcurrentSkipListMap<>();

    private AtomicDouble prediction = new AtomicDouble();
    private Queue<Double> predictionAccBuffer;
    private AtomicDouble predictionAcc = new AtomicDouble();

    private int stdDevSize;

    private FlowableProcessor<Double> actions = PublishProcessor.create();

    private FuturesStrategy sideStrategy;

    private final static int CANCEL_RANGE = 11;
    private final static int CREATE_RANGE = 3;
    private final static int LEVEL_RANGE = 1;

//    private Deque<Order> createOrders = new ConcurrentLinkedDeque<>();

    private Long initTime;

    private AtomicLong count = new AtomicLong(10000);

    private AtomicReference<Order> lastLong = new AtomicReference<>();
    private AtomicReference<Order> lastShort = new AtomicReference<>();

    private ObjectMapper objectMapper = new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);

    private boolean ownBalance = false;

    public FuturesStrategy(Long strategyId, String symbol, String currency, double longMinSpread, double shortMinSpread,
                           int longDelta, int shortDelta, int stdDevSize) {
        this.strategyId = strategyId;
        this.symbol = symbol;
        this.currency = currency;
        this.longMinSpread = longMinSpread;
        this.shortMinSpread = shortMinSpread;
        this.shortDelta = shortDelta;
        this.longDelta = longDelta;
        this.stdDevSize = stdDevSize;

        predictionAccBuffer = Queues.synchronizedQueue(EvictingQueue.create((int) (stdDevSize/2/PI)));
        stdDevBuffer = Queues.synchronizedQueue(EvictingQueue.create(stdDevSize));
    }

    public void setSideStrategy(FuturesStrategy sideStrategy) {
        this.sideStrategy = sideStrategy;
    }

    public void setOwnBalance(boolean ownBalance) {
        this.ownBalance = ownBalance;
    }

    private long levelPrice(double price){
        return Math.round(price*100);
    }

    @Inject
    private void init(){
        initTime = System.currentTimeMillis();

//        try {
//            createOrders.addAll(objectMapper.readValue(strategyMapper.getCreatedOrders(strategyId),
//                    new TypeReference<List<Order>>(){}));
//        } catch (Exception e) {
//            log.error("error load created orders", e);
//        }


        List<Double> prices = tradeMapper.getLastTrades(symbol, currency, stdDevSize).stream().map(t -> t.getPrice().doubleValue()).collect(Collectors.toList());
        Collections.reverse(prices);

        stdDevBuffer.addAll(prices);

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            try {
                okexExchange.createMarketOrder(1,  SELL, OPEN, symbol, currency, "i-s-o-" + System.nanoTime());
                okexExchange.createMarketOrder(1,  BUY, OPEN, symbol, currency, "i-b-o-" + System.nanoTime());
                Thread.sleep(5000);
                okexExchange.createMarketOrder(1,  SELL, CLOSE, symbol, currency, "i-s-c-" + System.nanoTime());
                okexExchange.createMarketOrder(1,  BUY, CLOSE, symbol, currency, "i-b-c-" + System.nanoTime());
            } catch (Exception e) {
                log.error("aida-init create order", e);
            }
        }, 10, TimeUnit.SECONDS);
    }

    public Trade getLastTrade() {
        return lastTrade.get();
    }

    @Inject
    private void subscribe(){
        //info
        okexExchange.getInfos()
                .filter(i -> currency.equals(i.getCurrency()))
                .subscribe(i -> info.set(i));

        //position
        //noinspection Duplicates
        okexExchange.getPositions()
                .filter(p -> symbol.equals(p.getSymbol()) && currency.equals(p.getCurrency()))
                .subscribe(p -> {
                    try {
                        if ("long".equals(p.getType())){
                            longPosition.set(p);
                        }else if ("short".equals(p.getType())){
                            shortPosition.set(p);
                        }
                    } catch (Exception e) {
                        log.error("error position", e);
                    }
                });

        //trade
        Flowable<Trade> trades = okexExchange.getTrades()
                .filter(t -> symbol.equals(t.getSymbol()) && currency.equals(t.getCurrency()));

        trades.subscribe(t -> {
            try {
                actions.onNext(t.getPrice().doubleValue());
            } catch (Exception e) {
                log.error("error trades", e);
            }
        });
        trades.subscribe(t -> lastTrade.set(t));
        trades.subscribe(t -> count.incrementAndGet());

        trades.subscribe(t -> {
            try {
                stdDevBuffer.add(t.getPrice().doubleValue());
            } catch (Exception e) {
                log.error("error update std dev", e);
            }
        });

        //depth
        Flowable<Depth> depths = okexExchange.getDepths()
                .filter(d -> symbol.equals(d.getSymbol()) && currency.equals(d.getCurrency()));
        depths.subscribe( d -> {
            try {
                if (getBalance() > 0){
                    actions.onNext(d.getAsk().doubleValue());
                }else if (getForecast() < 0){
                    actions.onNext(d.getBid().doubleValue());
                }
            } catch (Exception e) {
                log.error("error depth", e);
            }
        });
        depths.subscribe(d -> lastDepth.set(d));

        //order
        Flowable<Order> orders = okexExchange.getOrders()
                .filter(o -> symbol.equals(o.getSymbol()) && currency.equals(o.getCurrency()));

        orders.filter(o -> "filled".equals(o.getStatus()))
                .subscribe(o -> {
                    try {
                        actions.onNext(o.getAvgPrice().doubleValue());
                    } catch (Exception e) {
                        log.error("error orders", e);
                    }
                });

        orders.filter(o -> "new".equals(o.getStatus()))
                .subscribe(o -> {
                    try {
                        marketOrderMap.values().forEach(o1 -> {
                            if (o.getClOrderId().equals(o1.getClOrderId())){
                                o1.setOrderId(o.getOrderId());
                                o1.setClOrderId(o.getClOrderId());
                            }
                        });
                        stopOrderMap.values().forEach(o1 -> {
                            if (o.getClOrderId().equals(o1.getClOrderId())){
                                o1.setOrderId(o.getOrderId());
                                o1.setClOrderId(o.getClOrderId());
                            }
                        });
                        limitOrderMap.values().forEach(o1 -> {
                            if (o.getClOrderId().equals(o1.getClOrderId())){
                                o1.setOrderId(o.getOrderId());
                                o1.setClOrderId(o.getClOrderId());
                            }
                        });
                        stopLimitOrderMap.values().forEach(o1 -> {
                            if (o.getClOrderId().equals(o1.getClOrderId())){
                                o1.setOrderId(o.getOrderId());
                                o1.setClOrderId(o.getClOrderId());
                            }
                        });

                        if (o.getPrice() != null) {
                            long moneyPrice = levelPrice(o.getPrice().doubleValue());

                            if ("market".equals(o.getType())){
                                if (!marketOrderMap.containsKey(moneyPrice)) {
                                    marketOrderMap.put(moneyPrice, o);
                                }
                            }
                            if ("stop".equals(o.getType())){
                                if (!stopOrderMap.containsKey(moneyPrice)) {
                                    stopOrderMap.put(moneyPrice, o);
                                }
                            }
                            if ("limit".equals(o.getType())){
                                if (!limitOrderMap.containsKey(moneyPrice)) {
                                    limitOrderMap.put(moneyPrice, o);
                                }
                            }
                            if ("stop_limit".equals(o.getType())){
                                if (!stopLimitOrderMap.containsKey(moneyPrice)) {
                                    stopLimitOrderMap.put(moneyPrice, o);
                                }
                            }
                        }

                        if (lastTrade.get() != null) {
                            double price = lastTrade.get().getPrice().doubleValue();

                            if (longDelta > 0){
                                log.info("{} {} {} {} {} {} {} {}", symbol, currency, stdDev.get(), getSpread(price, true), getLongQty(),
                                        getShift(price, true), prediction.get(), getBalance());
                            }
                            if (shortDelta > 0){
                                log.info("{} {} {} {} {} {} {} {}", symbol, currency, stdDev.get(), getSpread(price, false), getShortQty(),
                                        getShift(price, false), prediction.get(),getBalance());
                            }
                        }
                    } catch (Exception e) {
                        log.error("error new order subscribe ", e);
                    }
                });

        orders.filter(o -> "filled".equals(o.getStatus()) || "canceled".equals(o.getStatus()) || "pending_cancel".equals(o.getStatus()))
                .subscribe(o -> {
                    try {
                        if ("filled".equals(o.getStatus())){
                            if ("buy".equals(o.getSide())){
                                lastLong.set(o);
                            }else if ("sell".equals(o.getSide())){
                                lastShort.set(o);
                            }
                        }

                        marketOrderMap.values().removeIf(o1 -> o.getOrderId().equals(o1.getOrderId()));
                        stopOrderMap.values().removeIf(o1 -> o.getOrderId().equals(o1.getOrderId()));
                        limitOrderMap.values().removeIf(o1 -> o.getOrderId().equals(o1.getOrderId()));
                        stopLimitOrderMap.values().removeIf(o1 -> o.getOrderId().equals(o1.getOrderId()));
                    } catch (Exception e) {
                        log.error("error remove filled canceled", e);
                    }
                });

        //reject
        okexExchange.getRejects().subscribe(r -> {
            try {
                marketOrderMap.values().removeIf(o -> r.getOrderId().equals(o.getOrderId()));
                stopOrderMap.values().removeIf(o -> r.getOrderId().equals(o.getOrderId()));
                limitOrderMap.values().removeIf(o -> r.getOrderId().equals(o.getOrderId()));
                stopLimitOrderMap.values().removeIf(o -> r.getOrderId().equals(o.getOrderId()));
                log.info("reject {}", r);
            } catch (Exception e) {
                log.error("error reject", e);
            }
        });

        //action
        actions.subscribe(this::action);
    }

    @Inject
    private void schedule(){
        //stddev
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (!stdDevBuffer.isEmpty()){
                    stdDev.set(Stats.of(stdDevBuffer).sampleStandardDeviation());
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
            } catch (Exception e) {
                log.error("error order map cancel ", e);
            }
        }, 1, 1, TimeUnit.MINUTES);

        //stop loss
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (longDelta > 0 && getLongQty() > longDelta){
                    String clOrderId = "sell:" + LocalTime.now() + ":M";
                    clOrderId = clOrderId.substring(0, Math.min(clOrderId.length(), 32));

                    okexExchange.createMarketOrder((int) (longDelta/2), BUY, CLOSE, symbol, currency, clOrderId);

                    log.info("LONG STOP LOSS {}", lastTrade.get().getPrice());
                }

                if (shortDelta > 0 && getShortQty() > shortDelta){
                    String clOrderId = "buy:" + LocalTime.now() + ":M";
                    clOrderId = clOrderId.substring(0, Math.min(clOrderId.length(), 32));

                    okexExchange.createMarketOrder((int) (shortDelta/2), SELL, CLOSE, symbol, currency, clOrderId);

                    log.info("SHORT STOP LOSS {}", lastTrade.get().getPrice());
                }

            } catch (Exception e) {
                log.error("error order map cancel ", e);
            }
        }, 1, 1, TimeUnit.MINUTES);

        //create
//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            try {
//                Iterator<Order> iterator = createOrders.descendingIterator();
//
//                while (iterator.hasNext()){
//                    Order o = iterator.next();
//
//                    BigDecimal price = lastTrade.get().getPrice();
//
//                    boolean side = "buy".equals(o.getSide());
//
//                    if ((o.getPrice().subtract(price).abs().doubleValue() < CREATE_RANGE *getSpread()
//                            || (("market".equals(o.getType()) || "stop_limit".equals(o.getType())) && o.getPrice().compareTo(price) > 0)
//                            || (("stop".equals(o.getType()) || "limit".equals(o.getType())) && o.getPrice().compareTo(price) < 0))
//                            && (!"stop".equals(o.getType()) || longPosition.get().getEveningUp().intValue() >= o.getQty())
//                            && (!"stop_limit".equals(o.getType()) || shortPosition.get().getEveningUp().intValue() >= o.getQty())
//                            && trade(o.getPrice().doubleValue(), o.getPrice().doubleValue(), getSpread(price.doubleValue(), side), side)){
//                        String clOrderId = o.getType().length() + ":" + LocalTime.now() + ":" + o.getPrice();
//                        clOrderId = clOrderId.substring(0, Math.min(clOrderId.length(), 32));
//                        o.setClOrderId(clOrderId);
//
//                        long moneyPrice = levelPrice(o.getPrice().doubleValue());
//
//                        switch (o.getType()){
//                            case "market":
//                                marketOrderMap.put(moneyPrice, o);
//                                break;
//                            case "stop":
//                                stopOrderMap.put(moneyPrice, o);
//                                longPosition.get().setEveningUp(longPosition.get().getEveningUp().subtract(BigDecimal.ONE));
//                                break;
//                            case "limit":
//                                limitOrderMap.put(moneyPrice, o);
//                                break;
//                            case "stop_limit":
//                                stopLimitOrderMap.put(moneyPrice, o);
//                                shortPosition.get().setEveningUp(shortPosition.get().getEveningUp().subtract(BigDecimal.ONE));
//                                break;
//                        }
//
//
//                        okexExchange.createOrder(1,
//                                o.getPrice().doubleValue(),
//                                "buy".equals(o.getSide()) ? BUY : SELL,
//                                "market".equals(o.getType()) || "limit".equals(o.getType()) ? OPEN : CLOSE,
//                                symbol,
//                                currency,
//                                clOrderId);
//
//                        iterator.remove();
//                    }
//                };
//            } catch (Exception e) {
//                log.error("error order map cancel ", e);
//            }
//        }, 60, 1, TimeUnit.SECONDS);
//
//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            try {
//                strategyMapper.updateCreatedOrders(strategyId, objectMapper.writeValueAsString(createOrders));
//            } catch (Exception e) {
//                log.error("error save created orders ", e);
//            }
//        },1, 1, TimeUnit.MINUTES);

        //print
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                Stream.of(marketOrderMap, stopOrderMap, limitOrderMap, stopLimitOrderMap)
                        .flatMap(m -> m.values().stream())
                        .forEach((v) -> log.info("marketOrderMap {} {} {} {} {} {} {}",
                                v.getSymbol(), v.getPrice(), v.getQty(), v.getSide(), v.getType(),v.getOrderId(), v.getClOrderId()));
            } catch (Exception e) {
                log.error("error print order map ", e);
            }
        },2, 10, TimeUnit.MINUTES);

        //prediction
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (!stdDevBuffer.isEmpty()){
                    prediction.set(getPrediction(new ArrayList<>(stdDevBuffer)));

                    //acceleration
                    predictionAccBuffer.add(prediction.get());
                    predictionAcc.set(getPrediction(new ArrayList<>(predictionAccBuffer)));
                }
            } catch (Exception e) {
                log.error("error prediction schedule ", e);
            }
        }, 60, 1, TimeUnit.SECONDS);

        //metric
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (lastTrade.get() != null) {
                    double price = lastTrade.get().getPrice().doubleValue();

                    influxService.getInfluxDB().write(Point.measurement("okex_strategy")
                            .tag("currency", currency)
                            .tag("symbol", symbol)
                            .addField("long_qty", getLongQty())
                            .addField("short_qty", getShortQty())
                            .addField("long_evening_up", getLongEveningUp())
                            .addField("short_evening_up", getShortEveningUp())
                            .addField("long_shift_", getShift(price, true))
                            .addField("short_shift_", getShift(price, false))
                            .addField("long_spread", getSpread(price, true))
                            .addField("short_spread", getSpread(price, false))
                            .addField("prediction", getPrediction())
                            .addField("prediction_acc", predictionAcc.get())
                            .addField("forecast", getForecast())
                            .addField("std_dev", stdDev.get())
                            .addField("balance", getBalance())
                            .build());
                }
            } catch (Exception e) {
                log.error("error metric schedule ", e);
            }
        }, 90, 1, TimeUnit.SECONDS);

        //invert todo ext
//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            try {
//                if (sideStrategy != null){
//                    if (longDelta > 0){
//                        if (lastTrade.get().getPrice().compareTo(sideStrategy.lastTrade.get().getPrice()) < 0){
//                            shortDelta = longDelta;
//                            longDelta = 0;
//
//                            log.info("INVERT {} {} {}", lastTrade.get().getPrice(), longDelta, shortDelta);
//                        }
//                    }else if (shortDelta > 0){
//                        if (lastTrade.get().getPrice().compareTo(sideStrategy.lastTrade.get().getPrice()) > 0){
//                            longDelta = shortDelta;
//                            shortDelta = 0;
//
//                            log.info("INVERT {} {} {}", lastTrade.get().getPrice(), longDelta, shortDelta);
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                log.error("error invert schedule ", e);
//            }
//        }, 5, 360, TimeUnit.MINUTES);
//
//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            try {
//                if (sideStrategy != null){
//                    if (longDelta > 0){
//                        createOrders.removeIf(o -> "sell".equals(o.getSide()));
//
//                        Stream.of(limitOrderMap, stopLimitOrderMap)
//                                .flatMap(m -> m.values().stream())
//                                .filter(o -> o.getOrderId() != null)
//                                .forEach(o -> {
//                                    okexExchange.cancelOrder(o.getOrderId(), o.getClOrderId(), o.getSymbol(), o.getCurrency(), SELL);
//                                });
//
//                        if (shortPosition.get() != null) {
//                            int qty = shortPosition.get().getEveningUp().intValue();
//
//                            if (qty > 0) {
//                                Thread.sleep(100000);
//
//                                okexExchange.createMarketOrder(qty, SELL, CLOSE, symbol, currency, "inv_sc:" + LocalTime.now() + ":M");
//                                okexExchange.createMarketOrder(qty, SELL, OPEN, sideStrategy.symbol, sideStrategy.currency, "inv_so:" + LocalTime.now() + ":M");
//                            }
//                        }
//                    }else if (shortDelta > 0){
//                        createOrders.removeIf(o -> "buy".equals(o.getSide()));
//
//                        Stream.of(marketOrderMap, stopOrderMap)
//                                .flatMap(m -> m.values().stream())
//                                .filter(o -> o.getOrderId() != null)
//                                .forEach(o -> {
//                                    okexExchange.cancelOrder(o.getOrderId(), o.getClOrderId(), o.getSymbol(), o.getCurrency(), BUY);
//                                });
//
//                        if (longPosition.get() != null) {
//                            int qty = longPosition.get().getEveningUp().intValue();
//
//                            if (qty > 0) {
//                                Thread.sleep(100000);
//
//                                okexExchange.createMarketOrder(qty, BUY, CLOSE, symbol, currency, "inv_bc:" + LocalTime.now() + ":M");
//                                okexExchange.createMarketOrder(qty, BUY, OPEN, sideStrategy.symbol, sideStrategy.currency, "inv_bo:" + LocalTime.now() + ":M");
//                            }
//                        }
//                    }
//                }
//            } catch (Exception e) {
//                log.error("error invert schedule ", e);
//            }
//        }, 1, 1, TimeUnit.MINUTES);

        //backtest
//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            try {
//                if (count.get() > 10000) {
//                    count.set(0);
//
//                    backtestSpread = backtestService
//                            .getLevels(new ArrayList<>(stdDevBuffer), 5, 15)
//                            .get(0)
//                            .getSpread();
//                }
//            } catch (Exception e) {
//                log.error("error backtest schedule ", e);
//            }
//        }, 1, 1, TimeUnit.MINUTES);
    }

    private void cancel(ConcurrentNavigableMap<Long, Order> orderMap) {
        if (lastTrade.get() == null){
            return;
        }

        orderMap.tailMap(levelPrice(lastTrade.get().getPrice().doubleValue() + CANCEL_RANGE *getSpread())).values()
                .forEach(o -> {
                    if (o.getOrderId() != null) {
                        okexExchange.cancelOrder(o.getOrderId(), o.getClOrderId(), o.getSymbol(), o.getCurrency(),
                                "buy".equals(o.getSide()) ? BUY : SELL);

//                        if (createOrders.stream().noneMatch(o1 -> Objects.equals(o.getOrderId(), o1.getOrderId()))){
//                            createOrders.add(o);
//                        }
                    }
                });
        orderMap.headMap(levelPrice(lastTrade.get().getPrice().doubleValue() - CANCEL_RANGE *getSpread())).values()
                .forEach(o -> {
                    if (o.getOrderId() != null) {
                        okexExchange.cancelOrder(o.getOrderId(), o.getClOrderId(), o.getSymbol(), o.getCurrency(),
                                "buy".equals(o.getSide()) ? BUY : SELL);
//
//                        if (createOrders.stream().noneMatch(o1 -> Objects.equals(o.getOrderId(), o1.getOrderId()))){
//                            createOrders.add(o);
//                        }
                    }
                });

        orderMap.tailMap(levelPrice(lastTrade.get().getPrice().doubleValue())).values()
                .forEach(o -> {
                    if (("market".equals(o.getType()) || ("stop_limit".equals(o.getType())
                            && shortPosition.get().getEveningUp().intValue() >= o.getQty()))
                            && System.currentTimeMillis() - o.getCreated().getTime() > 10000){
                        orderMap.values().removeIf(o1 -> o.getOrderId() != null
                                ? o.getOrderId().equals(o1.getOrderId())
                                : o.getClOrderId().equals(o1.getClOrderId()));

                        log.info("cancel 0 {}", o);
                    }
                });
        orderMap.headMap(levelPrice(lastTrade.get().getPrice().doubleValue())).values()
                .forEach(o -> {
                    if (("limit".equals(o.getType()) || ("stop".equals(o.getType())
                            && longPosition.get().getEveningUp().intValue() >= o.getQty()))
                            && System.currentTimeMillis() - o.getCreated().getTime() > 10000){
                        orderMap.values().removeIf(o1 -> o.getOrderId() != null
                                ? o.getOrderId().equals(o1.getOrderId())
                                : o.getClOrderId().equals(o1.getClOrderId()));

                        log.info("cancel 0 {}", o);
                    }
                });
    }

    private double getSpread(){
        return longDelta > 0
                ? getSpread(lastTrade.get().getPrice().doubleValue(), true)
                : getSpread(lastTrade.get().getPrice().doubleValue(), false);
    }

    private double getSpread(double price, boolean side){
        double spread = 0;

        if (info.get() != null){
            spread = 100*Math.PI*stdDev.get()/info.get().getBalance().add(info.get().getProfit()).multiply(lastTrade.get().getPrice()).doubleValue();
        }

        return Math.max(side ? price*longMinSpread : price*shortMinSpread, spread);
    }

    public int getLongQty(){
        return longPosition.get() != null ? longPosition.get().getQty() : 0;
    }

    public int getShortQty(){
        return shortPosition.get() != null ? shortPosition.get().getQty() : 0;
    }

    private int getLongEveningUp(){
        return longPosition.get() != null ? longPosition.get().getEveningUp().intValue() : 0;
    }

    private int getShortEveningUp(){
        return shortPosition.get() != null ? shortPosition.get().getEveningUp().intValue() : 0;
    }

    private double getShift(double price, boolean side){
        if (sideStrategy != null){
            if (side){
                if (sideStrategy.getShortQty() + getLongQty() != 0) {
                    return getSpread(price, true)*(getBalance())/(sideStrategy.getShortQty() + getLongQty());
                }
            }else{
                if (getShortQty() + sideStrategy.getLongQty() != 0) {
                    return getSpread(price, false)*(getBalance())/(getShortQty() + sideStrategy.getLongQty());
                }
            }
        }

        return 0;
    }

    private final static BigDecimal HUNDRED = new BigDecimal("100");

    private int getBalance(){
        if (info.get() != null && lastTrade.get() != null){
            int balanceQty = 0;

            if (ownBalance) {
                int ownQty = info.get().getBalance().add(info.get().getProfit()).multiply(lastTrade.get().getPrice())
                        .divide(HUNDRED, 8, HALF_EVEN).intValue();

                balanceQty = (int) (0.16*getForecast()*ownQty - ownQty);
            }

            if (sideStrategy != null) {
                return (getShortQty() + sideStrategy.getShortQty() - getLongQty() - sideStrategy.getLongQty() + balanceQty);
            }else{
                return getShortQty() - getLongQty() + balanceQty;
            }
        }

        return 0;
    }

    private void action(double price){
        try {
            if (longDelta > 0 && longPosition.get() != null) {
                action(price, true);
            }

            if (shortDelta > 0 && shortPosition.get() != null) {
                action(price, false);
            }
        } catch (Exception e) {
            log.error("error action", e);
        }
    }

    private double getForecast(){
        return forecastOrderService.getForecast(symbol, currency);
    }

    private void action(double price, boolean side) {
        if (System.currentTimeMillis() - initTime < 100000){
            return;
        }

        double spread = getSpread(price, side);
        double priceS = price + getShift(price, side);
        double buy = side ? priceS - spread : priceS;
        double sell = side ? priceS : priceS + spread;
        boolean balance = getBalance() > 0;

        if (trade(buy, spread, side ? marketOrderMap : stopLimitOrderMap) &&
                trade(sell, spread, side ? stopOrderMap : limitOrderMap)){
            BigDecimal roundBuy = new BigDecimal(buy).setScale(2, HALF_EVEN);
            BigDecimal roundSell = new BigDecimal(sell).setScale(2, HALF_EVEN);

            int buyQty = balance ? 2 : 1;
            int sellQty = balance ? 1 : 2;

            String buyClOrderId = "b:" + LocalTime.now() + ":" + buy;
            buyClOrderId = buyClOrderId.substring(0, Math.min(buyClOrderId.length(), 32));
            Order buyOrder = new Order(buyClOrderId, currency, buyQty, roundBuy, symbol);


            String sellClOrderId = "s:" + LocalTime.now() + ":" + sell;
            sellClOrderId = sellClOrderId.substring(0, Math.min(sellClOrderId.length(), 32));
            Order sellOrder = new Order(sellClOrderId, currency, sellQty, roundSell, symbol);

            if (side){
                buyOrder.setSide("buy");
                buyOrder.setType("market");
                marketOrderMap.put(levelPrice(buy), buyOrder);
                okexExchange.createOrder(buyQty, roundBuy.doubleValue(), BUY, OPEN, symbol, currency, buyOrder.getClOrderId());

                sellOrder.setSide("buy");
                sellOrder.setType("stop");
                stopOrderMap.put(levelPrice(sell), sellOrder);
                if (longPosition.get().getEveningUp().intValue() >= sellQty){
                    longPosition.get().setEveningUp(longPosition.get().getEveningUp().subtract(BigDecimal.valueOf(sellQty)));
                    okexExchange.createOrder(sellQty, roundSell.doubleValue(), BUY, CLOSE, symbol, currency, sellOrder.getClOrderId());
                }else {
//                    createOrders.add(sellOrder);
                }
            }else{
                sellOrder.setSide("sell");
                sellOrder.setType("limit");
                limitOrderMap.put(levelPrice(sell), sellOrder);
                okexExchange.createOrder(sellQty, roundSell.doubleValue(), SELL, OPEN, symbol, currency, sellOrder.getClOrderId());

                buyOrder.setSide("sell");
                buyOrder.setType("stop_limit");
                stopLimitOrderMap.put(levelPrice(buy), buyOrder);

                if (shortPosition.get().getEveningUp().intValue() >= buyQty){
                    shortPosition.get().setEveningUp(shortPosition.get().getEveningUp().subtract(BigDecimal.valueOf(buyQty)));
                    okexExchange.createOrder(buyQty, roundBuy.doubleValue(), SELL, CLOSE, symbol, currency, buyOrder.getClOrderId());
                }else{
//                    createOrders.add(buyOrder);
                }
            }
        }
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

    private boolean trade(double buyPrice, double sellPrice, double spread, boolean side) {
        Order order = side ? lastLong.get() : lastShort.get();

        return order == null || Math.abs(order.getPrice().doubleValue() - buyPrice) >= spread &&
                Math.abs(order.getPrice().doubleValue() - sellPrice) >= spread;
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
        return prediction.get();
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
        return predictionAcc.get();
    }
}
