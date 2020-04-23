package ru.inheaven.aida.bittrex;

import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import static java.math.RoundingMode.HALF_EVEN;
import static java.math.RoundingMode.HALF_UP;
import static org.knowm.xchange.dto.Order.OrderType.ASK;
import static org.knowm.xchange.dto.Order.OrderType.BID;

/**
 * @author Anatoly A. Ivanov
 * 24.11.2018 15:57
 */
public abstract class SimpleBittrexAbstract {
    private Logger log = LoggerFactory.getLogger(SimpleBittrexAbstract.class);

    private Random random = new Random();

    private BittrexMarket bittrexMarket;

    private Map<CurrencyPair, Long> errorMap = new HashMap<>();

    public SimpleBittrexAbstract(BittrexMarket bittrexMarket) {
        this.bittrexMarket = bittrexMarket;
    }

    protected void trade(CurrencyPair currencyPair, BigDecimal spread, BigDecimal share, BigDecimal price, BigDecimal amount) {
        if (errorMap.get(currencyPair) != null && System.currentTimeMillis() - errorMap.get(currencyPair) < 1000*60*60){
            return;
        }

        BigDecimal buyPrice = price.setScale(8, HALF_UP);
        BigDecimal sellPrice = price.add(spread).setScale(8, HALF_UP);

        BigDecimal buyPrice2 = price.subtract(spread).setScale(8, HALF_UP);
        BigDecimal sellPrice2 = price.add(spread).add(spread).setScale(8, HALF_UP);

        boolean buy = share.signum() > 0;

        if (isTrade(currencyPair, buyPrice, spread) && isTrade(currencyPair, sellPrice, spread) &&
                isTrade(currencyPair, buy ? buyPrice2 : sellPrice2, spread)){
            log.info("{} {} {}", currencyPair, share.setScale(8, HALF_UP), bittrexMarket.getBtcEquity().setScale(8, HALF_EVEN));

            double r = random.nextDouble()/Math.PI;

            BigDecimal buyAmount = amount
                    .multiply(BigDecimal.valueOf(r))
                    .add(amount)
                    .setScale(8, HALF_UP);

            BigDecimal sellAmount = amount
                    .multiply(BigDecimal.valueOf(r))
                    .add(amount)
                    .setScale(8, HALF_UP);

            trade(currencyPair, buyPrice, buyAmount, BID);
            trade(currencyPair, sellPrice, sellAmount, ASK);

            if (buy){
                trade(currencyPair, buyPrice2, buyAmount, BID);
            }else {
                trade(currencyPair, sellPrice2, sellAmount, ASK);
            }
        }
    }

    abstract void trade(CurrencyPair currencyPair, BigDecimal price, BigDecimal amount, Order.OrderType orderType);

    abstract boolean isTrade(CurrencyPair currencyPair, BigDecimal price, BigDecimal spread);

    public BittrexMarket getBittrexMarket() {
        return bittrexMarket;
    }

    public void error(CurrencyPair currencyPair){
        errorMap.put(currencyPair, System.currentTimeMillis());
    }
}
