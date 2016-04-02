package ru.inheaven.aida.happy.trading.strategy;

import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.service.*;
import ru.inheaven.aida.happy.trading.util.BibleRandom;
import ru.inheaven.aida.happy.trading.util.QuranRandom;
import rx.subjects.PublishSubject;

import java.io.IOException;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Collection;
import java.util.Date;
import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static ru.inheaven.aida.happy.trading.entity.LevelParameter.*;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;
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

    private PublishSubject<ActionPrice> actionPricePublishSubject = PublishSubject.create();

    public LevelStrategy(Strategy strategy) {
        super(strategy);

        userInfoService = Module.getInjector().getInstance(UserInfoService.class);
        OrderService orderService = Module.getInjector().getInstance(OrderService.class);
        XChangeService xChangeService = Module.getInjector().getInstance(XChangeService.class);
        TradeService tradeService = Module.getInjector().getInstance(TradeService.class);

        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> getOrderMap().forEach((id, o) -> {
            try {
                if (o.getStatus().equals(OPEN)) {
                    orderService.checkOrder(getAccount(), o);
                }else if ((o.getStatus().equals(CANCELED) || o.getStatus().equals(CLOSED)) && (o.getClosed() == null ||
                        System.currentTimeMillis() - o.getClosed().getTime() > 60000)) {
                    onOrder(o);
                    log.info("{} CLOSED by schedule {}", o.getStrategyId(), scale(o.getPrice()));
                }else if (o.getStatus().equals(CREATED) &&
                        System.currentTimeMillis() - o.getCreated().getTime() > 10000){
                    o.setStatus(CLOSED);
                    o.setClosed(new Date());
                    log.info("{} CLOSED by created {}", o.getStrategyId(), scale(o.getPrice()));
                }
            } catch (Exception e) {
                log.error("error check order -> ", e);
            }

        }), 0, 1, MINUTES);

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                PollingTradeService ts = xChangeService.getExchange(getAccount()).getPollingTradeService();

                BigDecimal stdDev = tradeService.getStdDev(getExchangeType(), strategy.getSymbol(), strategy.getInteger(VOLATILITY_SIZE));
                BigDecimal range = stdDev.multiply(strategy.getBigDecimal(CANCEL));

                orderService.getOpenOrders(getAccount().getId()).forEach(l -> {
                    if (lastAction.get() != null && l.getCurrencyPair().toString().equals(strategy.getSymbol()) &&
                            lastAction.get().subtract(l.getLimitPrice()).abs().compareTo(range) > 0){
                        try {
                            ts.cancelOrder(l.getId());

                            Order order = getOrderMap().get(l.getId());

                            if (order != null){
                                order.setStatus(CANCELED);
                            }

                            log.info("schedule cancel order {}", l);
                        } catch (IOException e) {
                            log.error("error schedule cancel order -> ", e);
                        }
                    }
                });
            } catch (Exception e) {
                log.error("error schedule cancel order -> ", e);
            }

        }, 0, 10, SECONDS);

        actionPricePublishSubject.distinctUntilChanged().onBackpressureLatest().subscribe(this::actionLevel);
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

    private AtomicReference<BigDecimal> lastAction = new AtomicReference<>(ZERO);

    private void actionAsync(BigDecimal price, BigDecimal amount, OrderType orderType, String key){
        actionPricePublishSubject.onNext(new ActionPrice(price, amount, orderType, key));
    }

    private void actionLevel(ActionPrice actionPrice){
        try {
            lastAction.set(actionPrice.getPrice());

            if (isBidRefused() || isAskRefused()) {
                return;
            }

            BigDecimal sideSpread = getSideSpread(actionPrice.getPrice());
            Integer levelSize = getStrategy().getInteger(SIZE);

            action(actionPrice.getKey(), actionPrice.getPrice(), actionPrice.getOrderType(), 0);

            for (int i = levelSize; i > 0 ; i--) {
                BigDecimal index = BigDecimal.valueOf(i);

                action(actionPrice.getKey(), actionPrice.getPrice().add(sideSpread.multiply(index)), actionPrice.getOrderType(), i);
                action(actionPrice.getKey(), actionPrice.getPrice().subtract(sideSpread.multiply(index)), actionPrice.getOrderType(), -i);
            }
        } catch (Exception e) {
            log.error("error actionLevel", e);
        }
    }

    protected BigDecimal getBalance(){
        if ("total".equals(getStrategy().getString(BALANCE_TYPE))){
            String[] symbol = getStrategy().getSymbol().split("/");

            BigDecimal total = userInfoService.getVolume("total", getAccount().getId(), null);
            BigDecimal subtotal = userInfoService.getVolume("subtotal", getAccount().getId(), symbol[0]);

            if (lastAction.get().equals(ZERO) || subtotal.equals(ZERO) || total.equals(ZERO)){
                return ZERO;
            }

            return total.divide(subtotal.multiply(lastAction.get()), HALF_EVEN).subtract(getStrategy().getBigDecimal(BALANCE));
        }

        return ZERO;
    }

    private BigDecimal getSpread(BigDecimal price){
        BigDecimal spread = ZERO;
        BigDecimal sideSpread = getSideSpread(price);

        switch (getStrategy().getString(SPREAD_TYPE)){
            case "fixed":
                spread = getStrategy().getBigDecimal(SPREAD);
                break;
            case "percent":
                spread = getStrategy().getBigDecimal(SPREAD).multiply(price);
                break;
            case "volatility":
                spread = getTradeService().getStdDev(getAccount().getExchangeType(), getStrategy().getSymbol(),
                        getStrategy().getInteger(VOLATILITY_SIZE)).divide(getStrategy().getBigDecimal(VOLATILITY), HALF_EVEN);

                break;
        }

        return spread.compareTo(sideSpread) > 0 ? spread : sideSpread;
    }


    private BigDecimal getSideSpread(BigDecimal price){
        BigDecimal sideSpread = ZERO;

        switch (getStrategy().getString(SIDE_SPREAD_TYPE)){
            case "fixed":
                sideSpread = getStrategy().getBigDecimal(SIDE_SPREAD);
                break;
            case "percent":
                sideSpread = getStrategy().getBigDecimal(SIDE_SPREAD).multiply(price);
                break;
        }

        return sideSpread.compareTo(getStep()) > 0 ? sideSpread : getStep();
    }

    private AtomicReference<BigDecimal> lastTrade = new AtomicReference<>(ZERO);

    @Override
    protected void onTrade(Trade trade) {
        BigDecimal lt = lastTrade.get();

        if (lt.compareTo(ZERO) != 0 && lt.subtract(trade.getPrice()).abs().divide(lt, 8, HALF_EVEN).compareTo(BD_0_01) < 0){
            actionAsync(trade.getPrice(), trade.getAmount(), trade.getOrderType(), "trade");
            closeByMarketAsync(trade.getPrice(), trade.getOrigTime());
        }else{
            log.warn("trade price diff 1% {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
        }

        lastTrade.set(trade.getPrice());
    }

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();
        BigDecimal lt = lastTrade.get();

        if (ask != null && bid != null && lt.compareTo(ZERO) != 0 &&
                lt.subtract(ask).abs().divide(lt, 8, HALF_EVEN).compareTo(BD_0_01) < 0 &&
                lt.subtract(bid).abs().divide(lt, 8, HALF_EVEN).compareTo(BD_0_01) < 0) {
            actionAsync(ask, null, ASK, "depth");
            actionAsync(bid, null, BID, "depth");
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            actionAsync(order.getBestPrice(), order.getAmount(), order.getType(), "real");
        }
    }

    private double nextDouble(){
        switch (getStrategy().getString(LOT_TYPE)){
            case "random_uniform":
                return random.nextDouble();
            case "random_gauss":
                double g = random.nextGaussian()/6 + 0.5;
                return g < 0 ? 0 : g > 1 ? 1 : g;
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

    private void action(String key, BigDecimal price, OrderType orderType, int priceLevel) {
        try {
            BigDecimal balance = getBalance();
            boolean up = balance.compareTo(ZERO)> 0;

            BigDecimal spread = scale(getSpread(price));
            BigDecimal priceF = up ? price.add(getStep()) : price.subtract(getStep());

            BigDecimal sideSpread = scale(getSideSpread(priceF));

            BigDecimal buyPrice = up ? priceF : priceF.subtract(spread);
            BigDecimal sellPrice = up ? priceF.add(spread) : priceF;

            if (!getOrderMap().contains(buyPrice, sideSpread, BID) && !getOrderMap().contains(sellPrice, sideSpread, ASK)){
                log.info("{} {} {} {} {} {} {} {}", getStrategy().getId(), key, price.setScale(3, HALF_EVEN),
                        orderType, sideSpread, spread, balance, priceLevel);

                double ra = nextDouble();
                double rb = nextDouble()*2;
                double rMax = ra > rb ? ra : rb;
                double rMin = ra > rb ? rb : ra;

                double a = getStrategy().getBigDecimal(LOT).doubleValue();
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
}

