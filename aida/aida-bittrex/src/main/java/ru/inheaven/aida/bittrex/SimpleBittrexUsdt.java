package ru.inheaven.aida.bittrex;

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
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Consumer;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.knowm.xchange.currency.Currency.BTC;
import static org.knowm.xchange.currency.Currency.USDT;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

/**
 * @author Anatoly A. Ivanov
 * 17.04.2018 19:27
 */
@SuppressWarnings("Duplicates")
@Singleton
public class SimpleBittrexUsdt extends SimpleBittrexAbstract{
    private Logger log = LoggerFactory.getLogger(SimpleBittrexUsdt.class);

    private Exchange exchange = ExchangeFactory.INSTANCE.createExchange(
            org.knowm.xchange.bittrex.BittrexExchange.class.getName(),
            "bf0ab4a90ab049118432fb79bed25af3",
            "a9a6a6cb24f64316a156bd38738d4926");
//            "51631880ff7b4d3f9dcba8f007652c8c",
//            "3d775f67d7f5429eaa91a59f5f1ae114");


    private Map<CurrencyPair, ConcurrentSkipListMap<BigDecimal, LimitOrder>> orderMap = new ConcurrentHashMap<>();
    private Map<CurrencyPair, BigDecimal> spreadMap = new ConcurrentHashMap<>();

    private Set<CurrencyPair> currencyPairSet = new HashSet<>();

    private final double GOLDEN_RATIO = 1.618033988749894848204586834365638117720309179805762862135448622705260462818902;
    private final double K = GOLDEN_RATIO*2;

    @Inject
    public SimpleBittrexUsdt(BittrexMarket bittrexMarket) {
        super(bittrexMarket);

        scheduler(updateOpenOrders(), 0, 1, SECONDS);

        start(new CurrencyPair("BTC", "USDT"));
        start(new CurrencyPair("BCH", "USDT"));
        start(new CurrencyPair("XRP", "USDT"));
        start(new CurrencyPair("ETH", "USDT"));
        start(new CurrencyPair("ETC", "USDT"));
        start(new CurrencyPair("ADA", "USDT"));
        start(new CurrencyPair("LTC", "USDT"));
        start(new CurrencyPair("NEO", "USDT"));
        start(new CurrencyPair("DASH", "USDT"));
        start(new CurrencyPair("XMR", "USDT"));
        start(new CurrencyPair("NXT", "USDT"));
    }

    private void start(CurrencyPair currencyPair){

        orderMap.put(currencyPair, new ConcurrentSkipListMap<>());

        getBittrexMarket().startTicker(currencyPair);
        scheduler(updateTicker(currencyPair), 0, 2, SECONDS);
        scheduler(updateOrderMap(currencyPair));
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
                }), e -> log.error("updateOrders {} ", e.getMessage()));
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

    private final BigDecimal FIVE = new BigDecimal(5);

    private Runnable updateOrderMap(CurrencyPair currencyPair){
        return () -> {
            for (Iterator<Map.Entry<BigDecimal, LimitOrder>> it = orderMap.get(currencyPair).entrySet().iterator();it.hasNext();){
                Map.Entry<BigDecimal, LimitOrder> entry = it.next();

                LimitOrder o = entry.getValue();

                if (Objects.equals(o.getStatus(), Order.OrderStatus.PENDING_NEW)){
                    continue;
                }

                BigDecimal spread;// = spreadMap.get(currencyPair);
                BigDecimal price = getBittrexMarket().getTicker(currencyPair).getLast();

                spread = new BigDecimal(price.doubleValue() * K / 100);

                if (entry.getKey().subtract(price).abs().compareTo(spread.multiply(FIVE)) > 0){
                    try {
                        it.remove();

                        if (o.getId() != null) {
                            exchange.getTradeService().cancelOrder(o.getId());

                            log.info("cancel {} {} {} {}", o.getCurrencyPair(), price, o.getLimitPrice(), o.getOriginalAmount());
                        }
                    } catch (Exception e) {
                        log.error("updateOrderMap {} {} {}", o, price, e.getMessage());
                    }
                }
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

    private BigDecimal getShare(CurrencyPair currencyPair){
        if (BTC.equals(currencyPair.base)){
            return getBittrexMarket().getBtcShare().multiply(getBittrexMarket().getTicker(CurrencyPair.BTC_USDT).getLast());
        }else{
            return getBittrexMarket().getShare().multiply(getBittrexMarket().getTicker(CurrencyPair.BTC_USDT).getLast());
        }
    }

    private Random random = new SecureRandom();

    private BigDecimal TWO = new BigDecimal("2");

    private Runnable action(CurrencyPair currencyPair){
        return () -> {
            try {
                Ticker ticker = getBittrexMarket().getTicker(currencyPair);

                BigDecimal price = ticker.getLast();

                Balance balance = getBittrexMarket().getAccountInfo().getWallet().getBalance(currencyPair.base);

                if (ticker.getLast().compareTo(ZERO) == 0 || balance.getTotal().compareTo(ZERO) == 0){
                    return;
                }

                BigDecimal share = BigDecimal.ONE.subtract(balance.getTotal().multiply(price)
                        .divide(getShare(currencyPair), 8, HALF_EVEN));

//                double stddev = Stats.of(queueMap.get(currencyPair)).populationStandardDeviation();
                double minSpread = ticker.getLast().doubleValue() * K / 100;

                double stdSpread = 0;
//                double stdSpread = balance.getTotal().compareTo(ZERO) > 0
//                        ? 7*0.001*stddev/(ticker.getLast().doubleValue()*balance.getTotal().doubleValue())
//                        : 0;

                BigDecimal spread = new BigDecimal(stdSpread > minSpread ? stdSpread : minSpread);
                spreadMap.put(currencyPair, spread);

                BigDecimal amount = new BigDecimal(5/price.doubleValue());

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

            if (BID.equals(orderType) && amount.multiply(price).compareTo(wallet.getBalance(USDT).getAvailable()) > 0){
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
