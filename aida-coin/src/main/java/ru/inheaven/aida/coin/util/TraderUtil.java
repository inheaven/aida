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
    public static SecureRandom random = new SecureRandom();

    public static CurrencyPair getCurrencyPair(String pair){
        String[] cp = pair.split("/");

        return cp.length == 2 ? new CurrencyPair(cp[0], cp[1]) : null;
    }

    public static String getCurrency(String pair){
        String[] cp = pair.split("/");

        return cp.length == 2 ? cp[0] : null;
    }

    public static String getCounterSymbol(String pair){
        String[] cp = pair.split("/");

        return cp.length == 2 ? cp[1] : null;
    }

    public static String getPair(CurrencyPair currencyPair){
        return currencyPair.baseSymbol + "/" + currencyPair.counterSymbol;
    }

    public static BigDecimal random10(BigDecimal decimal){
        return decimal.multiply(new BigDecimal(1 + random.nextDouble()/10)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal randomMinus10(BigDecimal decimal){
        return decimal.multiply(new BigDecimal(1 - random.nextDouble()/10)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal random20(BigDecimal decimal){
        return decimal.multiply(new BigDecimal(1 + random.nextDouble()/5)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal randomMinus20(BigDecimal decimal){
        return decimal.multiply(new BigDecimal(1 - random.nextDouble()/5)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal random30(BigDecimal decimal){
        return decimal.multiply(new BigDecimal(1 + random.nextDouble()/3)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal randomMinus30(BigDecimal decimal){
        return decimal.multiply(new BigDecimal(1 - random.nextDouble()/3)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal random50(BigDecimal decimal){
        return decimal.multiply(BigDecimal.valueOf(1 + random.nextDouble()/2)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal randomMinus50(BigDecimal decimal){
        return decimal.multiply(BigDecimal.valueOf(1 - random.nextDouble()/2)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal random80(BigDecimal decimal){
        return decimal.multiply(BigDecimal.valueOf(1 + random.nextDouble()/1.25)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal randomMinus80(BigDecimal decimal){
        return decimal.multiply(BigDecimal.valueOf(1 - random.nextDouble()/1.25)).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal random100(BigDecimal decimal){
        return decimal.multiply(BigDecimal.valueOf(1 + random.nextDouble())).setScale(8, ROUND_HALF_UP);
    }

    public static BigDecimal randomMinus100(BigDecimal decimal){
        return decimal.multiply(BigDecimal.valueOf(1 - random.nextDouble())).setScale(8, ROUND_HALF_UP);
    }
}
