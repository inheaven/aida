package ru.inheaven.aida.happy.trading.strategy;

import com.google.common.primitives.Doubles;
import com.google.common.util.concurrent.AtomicDouble;
import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.*;
import ru.inheaven.aida.happy.trading.util.QuranRandom;
import ru.inheaven.aida.happy.trading.util.TorahRandom;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CLOSED;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.OPEN;
import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 08.07.2015 19:39.
 */
public class LevelStrategy extends BaseStrategy{
    private Logger log = LoggerFactory.getLogger(getClass());

    private BigDecimal risk = ONE;

    private UserInfoService userInfoService;
    private TradeService tradeService;
    private OrderService orderService;
    private InfluxService influxService;

    private SecureRandom random = new SecureRandom();

    private static AtomicLong positionIdGen = new AtomicLong(System.nanoTime());

    private Deque<Double> spreadPrices = new ConcurrentLinkedDeque<>();

    private StrategyService strategyService;

    private VSSAService vssaService;

    private Deque<BigDecimal> actionPrices = new ConcurrentLinkedDeque<>();
    private Deque<Trade> closedMarketTrades = new ConcurrentLinkedDeque<>();

    public LevelStrategy(StrategyService strategyService, Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService,  XChangeService xChangeService) {
        super(strategy, orderService, orderMapper, tradeService, depthService, xChangeService);

        this.userInfoService = userInfoService;
        this.tradeService = tradeService;
        this.orderService = orderService;
        this.strategyService = strategyService;
        this.influxService = Module.getInjector().getInstance(InfluxService.class);

        //Action
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()-> {
            try {
                while (true){
                    BigDecimal price =  actionPrices.poll();

                    if (price == null){
                        break;
                    }else {
                        actionLevel(price);
                    }
                }
            } catch (Exception e) {
                log.error("error action level executor", e);

                throw e;
            }
        }, 5000, 20, TimeUnit.MILLISECONDS);

        //VSSA
        vssaService = new VSSAService(strategy.getSymbol(), null, 0.5, 100, 10, 300, 10, 1000, 1000);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (vssaService.isLoaded()) {
                    vssaService.fit();
                }
            } catch (Throwable e) {
                log.error("vssaService ", e);
            }
        }, 0, 5, TimeUnit.MINUTES);

        //Std Dev
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                int size = spreadPrices.size();

                for (int i = 0; i < size - getStrategy().getLevelSize().intValue(); ++i){
                    spreadPrices.pollFirst();
                }

                stdDev.set(standardDeviation.evaluate(Doubles.toArray(spreadPrices)));
            } catch (Exception e) {
                stdDev.set(0);

                log.error("error stdDev", e);
            }
        }, 5, 1, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                influxService.addStrategyMetric(getStrategy().getId(), getStrategy().getLevelLot(),
                        getSpread(lastTrade.get()), getStdDev(), getSpotBalance() ? 1d : -1d, getForecast(),
                        getShift(lastTrade.get()));
            } catch (Exception e) {
                log.error("error add strategy metric");
            }
        }, 5, 1, TimeUnit.SECONDS);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() ->{
            try {
                Trade maxTrade = closedMarketTrades.peek();
                Trade minTrade = closedMarketTrades.peek();

                if (maxTrade != null && minTrade != null){
                    Trade trade;

                    while ((trade = closedMarketTrades.poll()) != null){
                        if (trade.getPrice().compareTo(maxTrade.getPrice()) > 0){
                            maxTrade = trade;
                        }

                        if (trade.getPrice().compareTo(minTrade.getPrice()) < 0){
                            minTrade = trade;
                        }
                    }

                    closeByMarket(maxTrade.getPrice(), maxTrade.getOrigTime());
                    closeByMarket(minTrade.getPrice(), minTrade.getOrigTime());
                }
            } catch (Exception e) {
                log.error("error close market trade scheduler", e);
            }
        }, 5, 1, TimeUnit.MINUTES);
    }

    @SuppressWarnings("Duplicates")
    private void closeByMarket(BigDecimal price, Date time){
        try {
            double sideSpread = getSideSpread(price).doubleValue();

            getOrderMap().get(price.doubleValue(), BID, false).forEach((k,v) -> {
                v.values().forEach(o -> {
                    if (o.getStatus().equals(OPEN) && time.getTime() - o.getOpen().getTime() > 1000){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());

                        onOrder(o);
                    }
                });
            });

            getOrderMap().get(price.doubleValue(), ASK, false).forEach((k,v) -> {
                v.values().forEach(o -> {
                    if (o.getStatus().equals(OPEN) && time.getTime() - o.getOpen().getTime() > 1000){
                        o.setStatus(CLOSED);
                        o.setClosed(new Date());

                        onOrder(o);
                    }
                });
            });
        } catch (Exception e) {
            log.error("error close by market", e);
        }
    }

    private void actionLevel(BigDecimal price){
        actionLevel("schedule", price, null);
    }

    private AtomicReference<BigDecimal> lastAction = new AtomicReference<>(ZERO);


    private void actionLevel(String key, BigDecimal price, OrderType orderType){
        try {
            if (isBidRefused() || isAskRefused()) {
                return;
            }

            if (lastAction.get().equals(price)){
                return;
            }

            lastAction.set(price);

            action(key, price, orderType, 0);
        } catch (Exception e) {
            log.error("error actionLevel", e);
        }
    }

    protected boolean getSpotBalance() {
        BigDecimal subtotalBtc = userInfoService.getVolume("subtotal", getStrategy().getAccount().getId(), "BTC");
        BigDecimal net = userInfoService.getVolume("net", getStrategy().getAccount().getId(), null);
        BigDecimal price = lastAvgPrice.get().compareTo(ZERO) > 0 ? lastAvgPrice.get() : lastTrade.get();
        BigDecimal delta = BigDecimal.valueOf(getForecast() / vssaService.getVssaCount()).multiply(Const.BD_0_16).add(ONE);

        return subtotalBtc.compareTo(ZERO) > 0 && price.compareTo(ZERO) > 0 &&
                net.multiply(delta).divide(subtotalBtc.multiply(price), 8, HALF_EVEN).compareTo(ONE) > 0;
    }

    private BigDecimal getDeltaP(){
        BigDecimal subtotalBtc = userInfoService.getVolume("subtotal", getStrategy().getAccount().getId(), "BTC");
        BigDecimal net = userInfoService.getVolume("net", getStrategy().getAccount().getId(), null);

        return ONE.subtract(subtotalBtc.multiply(lastTrade.get()).divide(net, 8, HALF_EVEN));
    }

    private BigDecimal getShift(BigDecimal price){
        return getSpread(price).multiply(getDeltaP());
    }

    @Override
    protected double getForecast() {
        return vssaService.getForecast();
    }

    @Override
    protected BigDecimal getAvgPrice() {
        return lastAvgPrice.get();
    }

    @Override
    protected Long getWindow() {
        return window.get();
    }

    private StandardDeviation standardDeviation = new StandardDeviation(true);
    private AtomicDouble stdDev = new AtomicDouble(0);

    protected BigDecimal getStdDev(){
        return BigDecimal.valueOf(stdDev.get());
    }

    private BigDecimal spreadDiv = BigDecimal.valueOf(Math.sqrt(Math.PI*5));

    protected BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;
        BigDecimal sideSpread = getSideSpread(price);

        if (getStrategy().getSymbol().equals("BTC/CNY") || getStrategy().getSymbol().equals("LTC/CNY")){
//            BigDecimal stdDev = getStdDev();
//
//            if (stdDev != null){
//                spread = stdDev.divide(spreadDiv, 8, HALF_EVEN);
//            }
            spread = getDSpread(price);
        }else {
            spread = getStrategy().getSymbolType() == null
                    ? getStrategy().getLevelSpread().multiply(price)
                    : getStrategy().getLevelSpread();
        }

        return spread.compareTo(sideSpread) > 0 ? spread : sideSpread;
    }

    private BigDecimal getSideSpread(BigDecimal price){
        BigDecimal sp = getStrategy().getSymbolType() == null
                ? getStrategy().getLevelSideSpread().multiply(price)
                : getStrategy().getLevelSideSpread();

        return sp.compareTo(getStep()) > 0 ? sp : getStep();
    }

    private BigDecimal getDSpread(BigDecimal price){
        BigDecimal net = userInfoService.getVolume("net", getStrategy().getAccount().getId(), null);

        if (net != null && net.compareTo(ZERO) > 0 && price != null && price.compareTo(ZERO) > 0){
            return getStdDev()
                    .multiply(Const.BD_PI).multiply(getStrategy().getLevelSpread())
                    .multiply(getStrategy().getLevelLot())
                    .multiply(price)
                    .divide(net, 8, HALF_EVEN);
        }

        return getSideSpread(price);
    }

    private AtomicReference<BigDecimal> lastBuyPrice = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> lastSellPrice = new AtomicReference<>(ZERO);

    private AtomicLong index = new AtomicLong(0);

    private void action(String key, BigDecimal price, OrderType orderType, int priceLevel) {
        try {
            double forecast = getForecast();
            boolean balance = getSpotBalance();

            BigDecimal spread = getSpread(price);
            BigDecimal priceF = price.add(getShift(price));

            BigDecimal buyPrice = scale(priceF);
            BigDecimal sellPrice = scale(priceF.add(spread));

            if (!getOrderMap().contains(buyPrice, spread, BID) && !getOrderMap().contains(sellPrice, spread, ASK)){
                double q1 = TorahRandom.nextDouble();
                double q2 = QuranRandom.nextDouble();
                double max = Math.max(q1, q2);
                double min = Math.min(q1, q2);
////
////                //shuffle
//                max = max * (random.nextDouble()/33 + 1);
//                min = min * (random.nextDouble()/33 + 1);

//                if (forecast > 0 == balance){
//                    double abs = Math.abs(forecast);
//
//                    if (abs > 7){
//                        max *= Math.PI;
//                        min *= Math.PI;
//                    }else if (abs > 3){
//                        max *= 2;
//                        min *= 2;
//                    }
//                }

//                double max = random.nextGaussian()/2 + 2;
//                double min = random.nextGaussian()/2 + 1;

//                double q1 = Math.sin(index.get()/(2*Math.PI)) + 1.07;
//                double q2 = Math.cos(index.get()/(2*Math.PI)) + 1.07;
//
//                double max = Math.max(q1, q2);
//                double min = Math.min(q1, q2);

                log.info("{} "  + key + " {} {} {} {}", getStrategy().getId(), price.setScale(3, HALF_EVEN), spread, min, max);

                BigDecimal buyAmount = getStrategy().getLevelLot().multiply(BigDecimal.valueOf(balance ? max : min)).add(Const.BD_0_01);
                BigDecimal sellAmount = getStrategy().getLevelLot().multiply(BigDecimal.valueOf(balance ? min : max)).add(Const.BD_0_01);

                //less
                if (buyAmount.compareTo(Const.BD_0_01) < 0){
                    buyAmount = Const.BD_0_01;
                }
                if (sellAmount.compareTo(Const.BD_0_01) < 0){
                    sellAmount = Const.BD_0_01;
                }

                Long positionId = positionIdGen.incrementAndGet();
                Order buyOrder = new Order(getStrategy(), positionId, BID, buyPrice, buyAmount.setScale(3, HALF_UP));
                Order sellOrder = new Order(getStrategy(), positionId, ASK, sellPrice, sellAmount.setScale(3, HALF_UP));

                buyOrder.setSpread(spread);
                buyOrder.setForecast(forecast);
                buyOrder.setBalance(balance);

                sellOrder.setSpread(spread);
                sellOrder.setForecast(forecast);
                sellOrder.setBalance(balance);

                //q1 > q2 == buyAmount.compareTo(sellAmount) > 0

                if (buyAmount.compareTo(sellAmount) > 0){
                    createOrderSync(buyOrder);
                    createOrderSync(sellOrder);
                }else{
                    createOrderSync(sellOrder);
                    createOrderSync(buyOrder);
                }

                lastBuyPrice.set(buyPrice);
                lastSellPrice.set(sellPrice);
            }
        } catch (Exception e) {
            log.error("action error -> ", e);
        }
    }

    private AtomicReference<BigDecimal> lastAvgPrice = new AtomicReference<>(ZERO);

    private AtomicLong window = new AtomicLong(1000);

    private AtomicReference<BigDecimal> lastTrade = new AtomicReference<>(ZERO);

    private AtomicReference<BigDecimal> tradeBid = new AtomicReference<>(ZERO);
    private AtomicReference<BigDecimal> tradeAsk = new AtomicReference<>(ZERO);

    @Override
    protected void onTrade(Trade trade) {
        index.incrementAndGet();

        try {
            (trade.getOrderType().equals(BID) ? tradeBid : tradeAsk).set(trade.getPrice());

            if (lastTrade.get().compareTo(ZERO) != 0 && lastTrade.get().subtract(trade.getPrice()).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(Const.BD_0_01) < 0){
                actionPrices.add(trade.getPrice());

                vssaService.add(trade);

                closedMarketTrades.add(trade);
            }else{
                log.warn("trade price diff 1% {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
            }

            //spread
            spreadPrices.add(trade.getPrice().doubleValue());

            lastTrade.set(trade.getPrice());
        } catch (Exception e) {
            log.error("error onTrade", e);
        }
    }

    private AtomicReference<BigDecimal> depthSpread = new AtomicReference<>(Const.BD_0_25);

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();

        if (ask != null && bid != null && lastTrade.get().compareTo(ZERO) != 0 &&
                lastTrade.get().subtract(ask).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(Const.BD_0_01) < 0 &&
                lastTrade.get().subtract(bid).abs().divide(lastTrade.get(), 8, HALF_EVEN).compareTo(Const.BD_0_01) < 0) {
            depthSpread.set(ask.subtract(bid).abs());

            if (getSpotBalance()){
                actionPrices.add(ask);
            }else if (getForecast() < 0){
                actionPrices.add(bid);
            }
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            actionPrices.add(order.getAvgPrice());
        }
    }

//    public static void main(String... args){
//        Observable.range(0, 100).buffer(55, 1).forEach(l -> System.out.println(Arrays.toString(l.toArray())));
//    }
}

