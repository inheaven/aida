package ru.inheaven.aida.coin.util;

import com.xeiam.xchange.currency.CurrencyPair;

import java.math.BigDecimal;
import java.security.SecureRandom;

import static java.math.BigDecimal.ROUND_HALF_UP;

/**
 * @author Anatoly Ivanov
 *         Date: 006 06.08.14 11:25
 */
public class TraderUtil {
    private static SecureRandom random = new SecureRandom();

    public static CurrencyPair getCurrencyPair(String pair){
        String[] cp = pair.split("/");

        return cp.length == 2 ? new CurrencyPair(cp[0], cp[1]) : null;
    }

    public static BigDecimal random20(BigDecimal decimal){
        return decimal.multiply(new BigDecimal(1 - random.nextDouble()/5)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal random50(BigDecimal decimal){
        return decimal.multiply(new BigDecimal(1 - random.nextDouble()/2)).setScale(8, ROUND_HALF_UP);
    }
}
