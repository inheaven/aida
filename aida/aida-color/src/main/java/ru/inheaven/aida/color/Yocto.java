package ru.inheaven.aida.color;

import com.yoctopuce.YoctoAPI.YAPI;
import com.yoctopuce.YoctoAPI.YColorLed;
import org.knowm.xchange.Exchange;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.dto.account.Balance;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.Executors;

import static java.lang.Math.PI;
import static java.math.BigDecimal.ZERO;
import static java.util.concurrent.TimeUnit.MINUTES;
import static org.knowm.xchange.ExchangeFactory.INSTANCE;
import static org.knowm.xchange.currency.Currency.BTC;
import static org.knowm.xchange.currency.Currency.USDT;
import static org.knowm.xchange.currency.CurrencyPair.BTC_USDT;

/**
 * @author Anatoly A. Ivanov
 * 01.01.2018 3:13
 */
public class Yocto {

    public static void main(String[] args)  {
        try {
            YAPI.RegisterHub("127.0.0.1");

            Exchange exchange = INSTANCE.createExchange(
                    org.knowm.xchange.bittrex.BittrexExchange.class.getName(),
                    "d49c9f61b1494137aa3ecfe046921fe4",
                    "43af1cbd201d4b948309e146682013d8");

            Set<CurrencyPair> currencyPairSet = new HashSet<>();

            currencyPairSet.add(new CurrencyPair("BCC", "BTC"));
            currencyPairSet.add(new CurrencyPair("LTC", "BTC"));
            currencyPairSet.add(new CurrencyPair("NXT", "BTC"));
            currencyPairSet.add(new CurrencyPair("DASH", "BTC"));
            currencyPairSet.add(new CurrencyPair("ETH", "BTC"));
            currencyPairSet.add(new CurrencyPair("ETC", "BTC"));
            currencyPairSet.add(new CurrencyPair("EXP", "BTC"));
            currencyPairSet.add(new CurrencyPair("XRP", "BTC"));
            currencyPairSet.add(new CurrencyPair("NEO", "BTC"));
            currencyPairSet.add(new CurrencyPair("GRC", "BTC"));
            currencyPairSet.add(new CurrencyPair("XMR", "BTC"));
            currencyPairSet.add(new CurrencyPair("ADA", "BTC"));
            currencyPairSet.add(new CurrencyPair("MCO", "BTC"));
            currencyPairSet.add(new CurrencyPair("SIB", "BTC"));
            currencyPairSet.add(new CurrencyPair("TRX", "BTC"));
            currencyPairSet.add(new CurrencyPair("GNT", "BTC"));

            List<Double> list = new ArrayList<>();

            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    AccountInfo accountInfo = exchange.getAccountService().getAccountInfo();

                    double equity = accountInfo.getWallet().getBalance(BTC).getTotal().doubleValue();

                    for (CurrencyPair currencyPair : currencyPairSet){
                        Balance balance = accountInfo.getWallet().getBalance(currencyPair.base);
                        if (balance.getTotal().compareTo(ZERO) > 0) {
                            equity += balance.getTotal().multiply(exchange.getMarketDataService()
                                    .getTicker(currencyPair).getLast()).doubleValue();
                        }
                    }

                    double price = exchange.getMarketDataService().getTicker(BTC_USDT).getLast().doubleValue();

                    equity *= price;

                    equity += accountInfo.getWallet().getBalance(USDT).getTotal().doubleValue();

                    list.add(equity);

                    if (list.size() > 1440){
                        list.remove(0);
                    }

                    double val = 0.5 + PI*(list.get(list.size() -1) - list.get(0))/(list.get(0));

                    System.out.println(new Date() + " " + equity + " " + equity/price + " " + val);

                    if (val > 1) val = 1;
                    if (val < 0) val = 0;

                    YColorLed led1 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed1");
                    YColorLed led2 = YColorLed.FindColorLed("YRGBLED1-018C9.colorLed2");

                    int r1 = 0;
                    int r2 = 0;
                    int g1 = 0;
                    int g2 = 0;
                    int b1 = 0;
                    int b2 = 0;

                    double x = 0;

                    int c = LocalDateTime.now().getHour() > 6 ? 255 : 8;

                    if (val < 0.2){
                        r1 = c;

                        r2 = c;
                        g2 = c;

                        x = val*2;
                    }else if (val > 0.8){
                        r1 = c;
                        g1 = c;

                        g2 = c;

                        x = val*2 - 1;
                    }

                    int r = (int)(r1*(1-x)+r2*x);
                    int g = (int)(g1*(1-x)+g2*x);
                    int b = (int)(b1*(1-x)+b2*x);

                    led1.setRgbColor(((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff));
                    led2.setRgbColor(((r&0x0ff)<<16)|((g&0x0ff)<<8)|(b&0x0ff));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }, 0, 1, MINUTES);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
