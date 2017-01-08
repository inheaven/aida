package ru.inheaven.aida.happy.accumulation.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.Balance;
import org.knowm.xchange.dto.marketdata.Ticker;
import org.knowm.xchange.okcoin.OkCoinExchange;
import org.knowm.xchange.okcoin.dto.account.OKCoinWithdraw;
import org.knowm.xchange.okcoin.service.polling.OkCoinAccountServiceRaw;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov
 *         Date: 08.01.2017.
 */
@Singleton
public class AccumulationService {
    Logger log = LoggerFactory.getLogger(AccumulationService.class);

    private BigDecimal ACCUMULATION_MIN = new BigDecimal("0.01");
    private BigDecimal ACCUMULATION_PERCENT = new BigDecimal("0.15");

    private BigDecimal balance = null;

    public AccumulationService() {
        log.info("init aida accumulation service");

        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(OkCoinExchange.class) {{
            setApiKey("671b6588-8af1-4f2e-98d4-51d013c086c0");
            setSecretKey("F02140757CF5FFC71441A26A44B437E6");
            setExchangeSpecificParametersItem("Use_Intl", false);
            setExchangeSpecificParametersItem("tradepwd", "[tggbnhfqlbyu2017");
        }});

        try {
            Balance cny = exchange.getPollingAccountService().getAccountInfo().getWallet().getBalance(Currency.CNY);
            log.info("init {}", cny.toString());

            balance = cny.getAvailable().add(cny.getFrozen());

            ((OkCoinAccountServiceRaw)exchange.getPollingAccountService()).withdraw(null, "btc_cny",
                    "1HgYheSg64weQ177ZtKY9Tx75g9tAj89Y5",
                    new BigDecimal("0.01"));
        } catch (Exception e) {
            log.error("error init", e);
        }

        if (balance != null){
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    Balance cny = exchange.getPollingAccountService().getAccountInfo().getWallet().getBalance(Currency.CNY);
                    log.info(cny.toString());

                    Ticker ticker = exchange.getPollingMarketDataService().getTicker(CurrencyPair.BTC_CNY);
                    log.info(ticker.toString());

                    BigDecimal profit = balance.subtract(cny.getAvailable().add(cny.getFrozen()));

                    if (profit.compareTo(ACCUMULATION_MIN.multiply(ticker.getLast())) > 0){
                        OKCoinWithdraw withdraw = ((OkCoinAccountServiceRaw)exchange.getPollingAccountService()).withdraw(null, "btc_cny",
                                "1HgYheSg64weQ177ZtKY9Tx75g9tAj89Y5",
                                profit.multiply(ACCUMULATION_PERCENT).divide(ticker.getLast(), 8, RoundingMode.HALF_EVEN));

                        log.info("withdraw {}", withdraw.toString());

                        balance = cny.getAvailable().add(cny.getFrozen());
                    }
                } catch (Exception e) {
                    log.error("error", e);
                }
            }, 0, 1, TimeUnit.DAYS);
        }
    }

    public static void main(String[] args) {
        new AccumulationService();
    }
}
