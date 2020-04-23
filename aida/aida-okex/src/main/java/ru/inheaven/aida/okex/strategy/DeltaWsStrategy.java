package ru.inheaven.aida.okex.strategy;

import ru.inheaven.aida.okex.model.Info;
import ru.inheaven.aida.okex.ws.OkexWsExchange;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov
 * 09.01.2018 17:41
 */
public class DeltaWsStrategy {
    @Inject
    private OkexWsExchange okexWsExchange;

    public DeltaWsStrategy(FuturesWsStrategy longStrategy, FuturesWsStrategy shortStrategy, FuturesWsStrategy levelStrategy) {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                int ownQty = Math.max(longStrategy.getOwnQty(), shortStrategy.getOwnQty());

                Info info = longStrategy.getInfo();
                BigDecimal amount = info.getBalance().add(info.getProfit()).subtract(info.getMargin());

                if (longStrategy.getLongQty() > 8*ownQty && shortStrategy.getShortQty() > 8*ownQty &&
                        longStrategy.getLongEveningUp() > 1 && shortStrategy.getShortEveningUp() > 1){
                    okexWsExchange.futureTrade(longStrategy.getCurrency(), longStrategy.getSymbol(), null,
                            1, 3, 1, 20);
                    okexWsExchange.futureTrade(shortStrategy.getCurrency(), shortStrategy.getSymbol(), null,
                            1, 4, 1, 20);
                }else if ((longStrategy.getLongQty() < 2*ownQty || shortStrategy.getShortQty() < 2*ownQty) &&
                        amount.doubleValue()*longStrategy.getPrice() > 20){
                    okexWsExchange.futureTrade(longStrategy.getCurrency(), longStrategy.getSymbol(), null,
                            1, 1, 1, 20);
                    okexWsExchange.futureTrade(shortStrategy.getCurrency(), shortStrategy.getSymbol(), null,
                            1, 2, 1, 20);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, 1, 1, TimeUnit.MINUTES);
    }
}
