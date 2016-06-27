package ru.inheaven.aida.happy.trading.util;

import ru.inheaven.aida.happy.trading.entity.Trade;

import java.math.BigDecimal;
import java.util.List;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;

/**
 * @author inheaven on 27.06.2016.
 */
public class TradeUtil {
    public static BigDecimal avg(List<Trade> trades){
        BigDecimal priceSum = ZERO;
        BigDecimal volumeSum = ZERO;

        for (Trade t : trades){
            priceSum = priceSum.add(t.getPrice().multiply(t.getAmount()));
            volumeSum = volumeSum.add(t.getAmount());
        }

        return priceSum.divide(volumeSum, 8, HALF_EVEN);
    }
}
