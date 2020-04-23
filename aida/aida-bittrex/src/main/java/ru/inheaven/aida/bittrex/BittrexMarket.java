package ru.inheaven.aida.bittrex;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static java.util.concurrent.TimeUnit.SECONDS;
import static org.knowm.xchange.currency.Currency.BTC;

/**
 * @author Anatoly A. Ivanov
 * 22.04.2018 14:52
 */
@Singleton
public class BittrexMarket {
    private Logger log = LoggerFactory.getLogger(BittrexMarket.class);

    private Exchange exchange = ExchangeFactory.INSTANCE.createExchange(
            org.knowm.xchange.bittrex.BittrexExchange.class.getName(),
//            "bf0ab4a90ab049118432fb79bed25af3",
//            "a9a6a6cb24f64316a156bd38738d4926");
            "51631880ff7b4d3f9dcba8f007652c8c",
            "3d775f67d7f5429eaa91a59f5f1ae114");

    private Map<CurrencyPair, Ticker> tickerMap = new ConcurrentHashMap<>();

    private AtomicReference<AccountInfo> accountInfo = new AtomicReference<>();
    private AtomicReference<OpenOrders> openOrders = new AtomicReference<>();

    private ScheduledExecutorService scheduledExecutorService =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors());

    public BittrexMarket() {
        scheduler(updateAccount(), 0, 59, SECONDS);
        scheduler(updateOpenOrders(), 0, 61, SECONDS);
    }

    @SuppressWarnings("SameParameterValue")
    private void scheduler(Runnable command, long initialDelay, long delay, TimeUnit unit){
        scheduledExecutorService.scheduleWithFixedDelay(command, initialDelay, delay, unit);
    }

    private Runnable updateAccount(){
        return handle(() -> accountInfo.set(exchange.getAccountService().getAccountInfo()),
                e -> log.error("updateAccount {}", e.getMessage()));
    }

    private Runnable updateOpenOrders(){
        return handle(() -> openOrders.set(exchange.getTradeService().getOpenOrders()),
                e -> log.error("updateOrders {} ", e.getMessage()));
    }

    @SuppressWarnings("Duplicates")
    private Runnable handle(SimpleBittrexBitcoin.RunnableExp runnable, Consumer<Exception> log) {
        return () -> {
            Exception exception = null;

            int error = 0;

            while (error < 2) {
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

    AccountInfo getAccountInfo(){
        return accountInfo.get();
    }

    OpenOrders getOpenOrders(){
        return openOrders.get();
    }

    void startTicker(CurrencyPair currencyPair){
        scheduler(updateTicker(currencyPair), 0, 2, SECONDS);
    }

    private Runnable updateTicker(CurrencyPair currencyPair){
        return handle(() -> {
            tickerMap.put(currencyPair, exchange.getMarketDataService().getTicker(currencyPair));
        }, e -> log.error("updateTicker {} {}", currencyPair, e.getMessage()));
    }

    Ticker getTicker(CurrencyPair currencyPair){
        return tickerMap.get(currencyPair);
    }

    BigDecimal getBtcEquity(){
        List<Ticker> btcTicker = tickerMap.values().stream()
                .filter(t -> BTC.equals(t.getCurrencyPair().counter))
                .collect(Collectors.toList());

        BigDecimal equity = getAccountInfo().getWallet().getBalance(BTC).getTotal();

        for (Ticker ticker : btcTicker){
            equity = equity.add(getAccountInfo().getWallet().getBalance(ticker.getCurrencyPair().base).getTotal()
                    .multiply(ticker.getLast()));
        }

//        equity = equity.add(getAccountInfo().getWallet().getBalance(USDT).getTotal()
//                .divide(tickerMap.get(CurrencyPair.BTC_USDT).getLast(), 8, RoundingMode.HALF_EVEN));

        return equity;
    }

    BigDecimal getShare(){
        long count = tickerMap.values().stream()
                .filter(t -> BTC.equals(t.getCurrencyPair().counter))
                .count() + 1;

        return getBtcEquity().divide(new BigDecimal(count), 8, RoundingMode.HALF_UP);
    }

    BigDecimal getBtcShare(){
        return getShare();
    }
}
