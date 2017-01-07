package ru.inheaven.aida.happy.mining.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.bittrex.v1.BittrexExchange;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov
 *         Date: 08.01.2017.
 */
@Singleton
public class MiningService {
    private Logger log = LoggerFactory.getLogger(MiningService.class);

    private BigDecimal TRANSACTION = new BigDecimal("0.005");

    public MiningService() {
        Exchange exchange = ExchangeFactory.INSTANCE.createExchange(new ExchangeSpecification(BittrexExchange.class) {{
            setApiKey("b19bc559298f4b41a4b5f78d81d15f4d");
            setSecretKey("db4b4cf44003457a9ec87dde5c37d5ee");
        }});

        log.info("start aida happy mining service");

        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                AccountInfo accountInfo = exchange.getPollingAccountService().getAccountInfo();
                Balance btc = accountInfo.getWallet().getBalance(Currency.BTC);

                log.info("balance {}", btc);

                if (btc.getAvailable().compareTo(TRANSACTION) > 0){
                    String id = exchange.getPollingAccountService().withdrawFunds(Currency.BTC, TRANSACTION, "1CXg1pr2KfLqA66GEJ6enbpeJpfqeGPqNP");

                    log.info("withdraw {}", id);
                }
            } catch (IOException e) {
                log.error("error", e);
            }

        },0, 1, TimeUnit.HOURS);
    }
}
