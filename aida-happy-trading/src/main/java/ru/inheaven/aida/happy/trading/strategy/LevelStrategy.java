package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.service.UserInfoService;
import ru.inheaven.aida.happy.trading.util.BibleRandom;
import ru.inheaven.aida.happy.trading.util.QuranRandom;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.Deque;
import java.util.Objects;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static ru.inheaven.aida.happy.trading.entity.LevelParameter.*;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CLOSED;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.OPEN;
import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 08.07.2015 19:39.
 */
public class LevelStrategy extends BaseStrategy{
    private Logger log = LoggerFactory.getLogger(getClass());

    private UserInfoService userInfoService;

    private SecureRandom random = new SecureRandom();

    private static AtomicLong positionIdGen = new AtomicLong(System.nanoTime());

    private final static BigDecimal BD_0_01 = new BigDecimal("0.01");
    private final static BigDecimal BD_SQRT_TWO_PI = new BigDecimal("2.506628274631000502415765284811");

    public LevelStrategy(Strategy strategy) {
        super(strategy);

        userInfoService = Module.getInjector().getInstance(UserInfoService.class);
    }

    private Executor executor = Executors.newCachedThreadPool();
    private AtomicReference<BigDecimal> lastMarket = new AtomicReference<>(ZERO);

    private void closeByMarketAsync(BigDecimal price, Date time){
        if (lastMarket.get().compareTo(price) != 0) {
            executor.execute(() -> closeByMarket(price, time));
            lastMarket.set(price);
        }
    }

    private void closeByMarket(BigDecimal price, Date time){
        try {
            getOrderMap().get(price.add(getSideSpread(price)), BID, false).forEach((k,v) -> closeByMarket(v, price, time));
            getOrderMap().get(price.subtract(getSideSpread(price)), ASK, false).forEach((k,v) -> closeByMarket(v, price, time));
        } catch (Exception e) {
            log.error("error close by market", e);
        }
    }

    private void closeByMarket(Collection<Order> orders, BigDecimal price, Date time){
        orders.forEach(o -> {
            if (o.getStatus().equals(OPEN) && time.getTime() - o.getOpen().getTime() > 0){
                o.setStatus(CLOSED);
                o.setClosed(new Date());
                log.info("{} CLOSED by market {} {} {} {}", o.getStrategyId(), scale(o.getPrice()), price, o.getType(), time.getTime() - o.getOpen().getTime());
            }
        });
    }

    private Executor executor2 = Executors.newCachedThreadPool();
    private AtomicReference<BigDecimal> lastAction = new AtomicReference<>(ZERO);
    private Deque<BigDecimal> actionDeque = new ConcurrentLinkedDeque<>();

    private void actionAsync(String key, BigDecimal price, OrderType orderType){
        if (lastAction.get().compareTo(price) != 0) {
            lastAction.set(price);
            actionDeque.push(price);

            executor2.execute(() -> actionSync(key, orderType));
        }
    }

    private Semaphore semaphore = new Semaphore(1);

    private void actionSync(String key, OrderType orderType){
        try {
            semaphore.acquire();

            while (!actionDeque.isEmpty()) {
                actionLevel(key, actionDeque.pop(), orderType);
            }
        } catch (Exception e) {
            log.error("error action level", e);
        } finally {
            semaphore.release();
        }
    }

    private void actionLevel(String key, BigDecimal price, OrderType orderType){
        if (isBidRefused() || isAskRefused()) {
            return;
        }

        BigDecimal sideSpread = getSideSpread(price);
        Integer levelSize = getStrategy().getIntegerParameter(SIZE);

        for (int i = levelSize; i > 0 ; i--) {
            action(key, price.add(sideSpread), orderType, i);
            action(key, price.subtract(sideSpread), orderType, -i);
        }

        action(key, price, orderType, 0);
    }

    protected BigDecimal getBalance(){
        if ("total".equals(getStrategy().getStringParameter(BALANCE_TYPE))){
            String[] symbol = getStrategy().getSymbol().split("/");

            BigDecimal numerator = userInfoService.getVolume("subtotal", getStrategy().getAccountId(), symbol[0]);
            BigDecimal total = userInfoService.getVolume("total", getStrategy().getAccountId(), null);
            BigDecimal balance = getStrategy().getBigDecimalParameter(BALANCE);

            if (lastAction.get().equals(ZERO) || numerator.equals(ZERO) || total.equals(ZERO)){
                return ZERO;
            }

            return total.divide(numerator.multiply(lastAction.get()), HALF_EVEN).subtract(balance);
        }

        return ZERO;
    }

    private BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;
        BigDecimal sideSpread = getSideSpread(price);

        switch (getStrategy().getStringParameter(SPREAD_TYPE)){
            case "fixed":
                spread = getStrategy().getBigDecimalParameter(SPREAD);
                break;
            case "percent":
                spread = getStrategy().getBigDecimalParameter(SPREAD).multiply(price);
                break;
            case "volatility":
                spread = getTradeService().getStdDev(getStrategy().getSymbol(), "_1").divide(BD_SQRT_TWO_PI, HALF_EVEN);

                break;
        }

        return spread.compareTo(sideSpread) > 0 ? spread : sideSpread;
    }


    private BigDecimal getSideSpread(BigDecimal price){
        BigDecimal sideSpread = ZERO;

        switch (getStrategy().getStringParameter(SIDE_SPREAD_TYPE)){
            case "fixed":
                sideSpread = getStrategy().getBigDecimalParameter(SIDE_SPREAD);
                break;
            case "percent":
                sideSpread = getStrategy().getBigDecimalParameter(SIDE_SPREAD).multiply(price);
                break;
        }

        return sideSpread.compareTo(getStep()) > 0 ? sideSpread : getStep();
    }

    private void action(String key, BigDecimal price, OrderType orderType, int priceLevel) {
        try {
            BigDecimal balance = getBalance();
            boolean up = balance.compareTo(ZERO)> 0;

            BigDecimal spread = scale(getSpread(price));
            BigDecimal priceF = up ? price.add(getStep()) : price.subtract(getStep());

            BigDecimal sideSpread = "volatility".equals(getStrategy().getStringParameter(SPREAD_TYPE))
                    ? spread
                    : scale(getSideSpread(priceF));

            BigDecimal buyPrice = up ? priceF : priceF.subtract(spread);
            BigDecimal sellPrice = up ? priceF.add(spread) : priceF;

            if (!getOrderMap().contains(buyPrice, sideSpread, BID) && !getOrderMap().contains(sellPrice, sideSpread, ASK)){
                log.info("{} "  + key + " {} {} {} {} {} {}", getStrategy().getId(), price.setScale(3, HALF_EVEN), orderType,
                        sideSpread, spread, balance, priceLevel);

                double ra = nextDouble();
                double rb = nextDouble();
                double rMax = ra > rb ? ra : rb;
                double rMin = ra > rb ? rb : ra;

                double a = getStrategy().getBigDecimalParameter(LOT).doubleValue();
                double rBuy = a * (up ? rMax : rMin);
                double rSell = a * (up ? rMin : rMax);

                BigDecimal buyAmount = BigDecimal.valueOf(rBuy).setScale(3, HALF_EVEN);
                BigDecimal sellAmount = BigDecimal.valueOf(rSell).setScale(3, HALF_EVEN);

                BigDecimal minAmount = getMinAmount();
                if (buyAmount.compareTo(minAmount) < 0){
                    buyAmount = minAmount;
                }
                if (sellAmount.compareTo(minAmount) < 0){
                    sellAmount = minAmount;
                }

                Long positionId = positionIdGen.incrementAndGet();
                Order buyOrder = new Order(getStrategy(), positionId, BID, buyPrice, buyAmount);
                Order sellOrder = new Order(getStrategy(), positionId, ASK, sellPrice, sellAmount);

                if ((ra > rb && rBuy > rSell) || (ra < rb && rBuy < rSell)){
                    createOrderSync(buyOrder);
                    createOrderSync(sellOrder);
                }else {
                    createOrderSync(sellOrder);
                    createOrderSync(buyOrder);
                }
            }
        } catch (Exception e) {
            log.error("action error -> ", e);
        }
    }

    private double nextDouble(){
        switch (getStrategy().getStringParameter(LOT_TYPE)){
            case "random_uniform":
                return random.nextDouble();
            case "random_gauss":
                return random.nextGaussian()/2 + 1;
            case "random_bible":
                return BibleRandom.nextDouble();
            case "random_quran":
                return QuranRandom.nextDouble();
            default:
                return 1;
        }
    }

    private BigDecimal getMinAmount(){
        return BD_0_01;
    }

    private volatile BigDecimal lastTrade = ZERO;

    @Override
    protected void onTrade(Trade trade) {
        if (lastTrade.compareTo(ZERO) != 0 &&
                lastTrade.subtract(trade.getPrice()).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(BD_0_01) < 0){
            actionAsync("TRADE", trade.getPrice(), trade.getOrderType());
            closeByMarketAsync(trade.getPrice(), trade.getOrigTime());
        }else{
            log.warn("trade price diff 1% {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
        }

        lastTrade = trade.getPrice();
    }

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();

        if (ask != null && bid != null && lastTrade.compareTo(ZERO) != 0 &&
                lastTrade.subtract(ask).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(BD_0_01) < 0 &&
                lastTrade.subtract(bid).abs().divide(lastTrade, 8, HALF_EVEN).compareTo(BD_0_01) < 0) {
            actionAsync("DEPTH", ask, ASK);
            actionAsync("DEPTH", bid, BID);
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            actionAsync("REAL", order.getAvgPrice(), order.getType());
        }
    }
}

