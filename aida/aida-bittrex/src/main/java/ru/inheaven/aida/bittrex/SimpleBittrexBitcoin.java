package ru.inheaven.aida.bittrex;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.account.Wallet;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static java.util.concurrent.TimeUnit.MINUTES;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.knowm.xchange.currency.Currency.BTC;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

/**
 * @author Anatoly A. Ivanov
 * 15.12.2017 22:23
 */
@SuppressWarnings("Duplicates")
@Singleton
public class SimpleBittrexBitcoin extends SimpleBittrexAbstract {
    private Logger log = LoggerFactory.getLogger(SimpleBittrexBitcoin.class);

    private Exchange exchange = ExchangeFactory.INSTANCE.createExchange(
            org.knowm.xchange.bittrex.BittrexExchange.class.getName(),
//            "bf0ab4a90ab049118432fb79bed25af3",
//            "a9a6a6cb24f64316a156bd38738d4926");
            "51631880ff7b4d3f9dcba8f007652c8c",
            "3d775f67d7f5429eaa91a59f5f1ae114");


    private Map<CurrencyPair, ConcurrentSkipListMap<BigDecimal, LimitOrder>> orderMap = new ConcurrentHashMap<>();
    private Map<CurrencyPair, BigDecimal> spreadMap = new ConcurrentHashMap<>();

    private Set<CurrencyPair> currencyPairSet = new HashSet<>();

    private final double GOLDEN_RATIO = 1.618033988749894848204586834365638117720309179805762862135448622705260462818902;
    private final double K = GOLDEN_RATIO;

    private final BigDecimal BD_0_00000001 = new BigDecimal("0.00000001");
    private final BigDecimal BD_100 = new BigDecimal(100);
    private final BigDecimal BD_K = new BigDecimal(K);
    private final BigDecimal BD_MIN_TRADE = new BigDecimal("0.001");
    private final BigDecimal BD_FIVE = new BigDecimal(5);

    @Inject
    public SimpleBittrexBitcoin(BittrexMarket bittrexMarket) {
        super(bittrexMarket);

        scheduler(updateOpenOrders(), 0, 1, SECONDS);

        start(new CurrencyPair("BCH", "BTC"));
        start(new CurrencyPair("LTC", "BTC"));
        start(new CurrencyPair("NXT", "BTC"));
        start(new CurrencyPair("DASH", "BTC"));
        start(new CurrencyPair("ETH", "BTC"));
        start(new CurrencyPair("ETC", "BTC"));
        start(new CurrencyPair("EXP", "BTC"));
        start(new CurrencyPair("XRP", "BTC"));
        start(new CurrencyPair("NEO", "BTC"));
        start(new CurrencyPair("GRC", "BTC"));
        start(new CurrencyPair("XMR", "BTC"));
        start(new CurrencyPair("ADA", "BTC"));
        start(new CurrencyPair("SIB", "BTC"));
        start(new CurrencyPair("TRX", "BTC"));
        start(new CurrencyPair("GNT", "BTC"));
        start(new CurrencyPair("WAVES", "BTC"));
    }

    private void start(CurrencyPair currencyPair){

        orderMap.put(currencyPair, new ConcurrentSkipListMap<>());

        getBittrexMarket().startTicker(currencyPair);
        scheduler(updateTicker(currencyPair), 0, 2, SECONDS);
        scheduler(updateOrderMap(currencyPair), 10, 1, SECONDS);
        scheduler(action(currencyPair), 10, 1, SECONDS);

        currencyPairSet.add(currencyPair);
    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

    @SuppressWarnings("SameParameterValue")
    private void scheduler(Runnable command, long initialDelay, long delay, TimeUnit unit){
        scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    private void scheduler(Runnable command){
        scheduler(command, 0, 1, SECONDS);
    }

    private Runnable updateOpenOrders(){
        return handle(() -> getBittrexMarket().getOpenOrders()
                .getOpenOrders()
                .forEach(o -> {
                    if (orderMap.containsKey(o.getCurrencyPair())) {
                        orderMap.get(o.getCurrencyPair()).put(o.getLimitPrice(), o);
                    }
                }), e -> log.error("updateOrders {}", e.getMessage()));
    }

    private Runnable updateTicker(CurrencyPair currencyPair){
        return handle(() -> {
            Ticker ticker = getBittrexMarket().getTicker(currencyPair);

            if (ticker != null) {


                orderMap.get(currencyPair).values().removeIf(o ->
                        (o.getType().equals(ASK) && o.getLimitPrice().compareTo(ticker.getLast()) < 0) ||
                                (o.getType().equals(BID) && o.getLimitPrice().compareTo(ticker.getLast()) > 0) ||
                                (o.getId() == null && (System.currentTimeMillis() - o.getTimestamp().getTime()) > 3600000));
            }
        }, e -> log.error("updateTicker {} {}", currencyPair, e.getMessage()));
    }

    private Cache<String, LimitOrder> cancelCache = CacheBuilder.newBuilder().expireAfterWrite(10, MINUTES).build();

    private Runnable updateOrderMap(CurrencyPair currencyPair){
        return () -> {
            try {
                for (Iterator<Map.Entry<BigDecimal, LimitOrder>> it = orderMap.get(currencyPair).entrySet().iterator();it.hasNext();){
                    Map.Entry<BigDecimal, LimitOrder> entry = it.next();

                    LimitOrder o = entry.getValue();

                    BigDecimal price = getBittrexMarket().getTicker(currencyPair).getLast();

                    BigDecimal range = entry.getKey().multiply(BD_K).multiply(BD_FIVE).divide(BD_100, 8, HALF_UP);

                    if (entry.getKey().subtract(price).abs().compareTo(range) > 0){
                        try {
                            it.remove();

                            if (o.getId() != null && cancelCache.getIfPresent(o.getId()) == null) {
                                cancelCache.put(o.getId(), o);

                                exchange.getTradeService().cancelOrder(o.getId());

                                log.info("cancel {} {} {} {}", o.getCurrencyPair(), price, o.getLimitPrice(), o.getOriginalAmount());
                            }
                        } catch (Exception e) {
                            log.error("error cancel {} {} {}", o, price, e.getMessage());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("updateOrderMap {}", e.getMessage());
            }
        };
    }

    interface RunnableExp {
        void run() throws Exception;
    }

    private Runnable handle(RunnableExp runnable, Consumer<Exception> log) {
        return () -> {
            Exception exception = null;

            int error = 0;

            while (error < 10) {
                try {
                    runnable.run();

                    return;
                } catch (Exception e) {
                    exception = e;

                    error++;
                }
            }

            log.accept(exception);
        };
    }

    private Runnable action(CurrencyPair currencyPair){
        return () -> {
            try {
                Ticker ticker = getBittrexMarket().getTicker(currencyPair);

                BigDecimal price = ticker.getBid().add(BD_0_00000001);

                Balance balance = getBittrexMarket().getAccountInfo().getWallet().getBalance(currencyPair.base);

                if (price == null || price.compareTo(ZERO) == 0 || balance.getTotal().compareTo(ZERO) == 0){
                    return;
                }

                BigDecimal share = BigDecimal.ONE.subtract(balance.getTotal().multiply(price)
                        .divide(getBittrexMarket().getShare(), 8, HALF_EVEN));

                BigDecimal spread = price.multiply(BD_K).divide(BD_100, 8, HALF_UP);

                spreadMap.put(currencyPair, spread);

                BigDecimal amount = BD_MIN_TRADE.divide(price, 8, HALF_UP);

                trade(currencyPair, spread, share, price, amount);
            }catch (Exception e){
                log.warn("action {} {}", currencyPair, e.getMessage());
            }
        };
    }

    void trade(CurrencyPair currencyPair, BigDecimal price, BigDecimal amount, Order.OrderType orderType) {

        try {
            LimitOrder order  = new LimitOrder(orderType, amount, currencyPair, null, new Date(), price);

            orderMap.get(currencyPair).put(order.getLimitPrice(), order);

            Wallet wallet = getBittrexMarket().getAccountInfo().getWallet();

            if (BID.equals(orderType) && amount.multiply(price).compareTo(wallet.getBalance(BTC).getAvailable()) > 0){
                return;
            }

            if (ASK.equals(orderType) && amount.compareTo(wallet.getBalance(currencyPair.base).getAvailable()) > 0){
                return;
            }

            try {
                exchange.getTradeService().placeLimitOrder(order);

                log.info("{} {} {} {}", currencyPair, price, amount, orderType);
            } catch (Exception e) {
                log.error("{} {} {}", currencyPair, orderType, e.getMessage());

                error(currencyPair);
            }
        } catch (Exception e){
            log.error("{} {} {}", currencyPair, orderType, e.getMessage());

            error(currencyPair);
        }
    }

    boolean isTrade(CurrencyPair currencyPair, BigDecimal price, BigDecimal spread){
        BigDecimal min = orderMap.get(currencyPair).floorKey(price);

        if (min != null && price.subtract(min).abs().compareTo(spread) < 0){
            return false;
        }

        BigDecimal max = orderMap.get(currencyPair).ceilingKey(price);

        return max == null || max.subtract(price).abs().compareTo(spread) > 0;
    }
}
