package ru.inheaven.aida.okex.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.fix.OkexExchange;
import ru.inheaven.aida.okex.model.Info;
import ru.inheaven.aida.okex.model.Position;
import ru.inheaven.aida.okex.model.Trade;

import javax.inject.Inject;
import java.math.BigDecimal;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

import static quickfix.field.PositionEffect.CLOSE;
import static quickfix.field.PositionEffect.OPEN;
import static quickfix.field.Side.BUY;
import static quickfix.field.Side.SELL;

/**
 * @author Anatoly A. Ivanov
 * 06.10.2017 22:01
 */
public class DeltaStrategy {
    private Logger log = LoggerFactory.getLogger(DeltaStrategy.class);

    @Inject
    private OkexExchange okexExchange;

    private String symbol;
    private String currency;

    private FuturesStrategy futuresStrategy1;
    private FuturesStrategy futuresStrategy2;

    private AtomicReference<Position> longPosition  = new AtomicReference<>();
    private AtomicReference<Position> shortPosition  = new AtomicReference<>();

    private AtomicReference<Info> info = new AtomicReference<>();

    private AtomicReference<Trade> trade = new AtomicReference<>();

    private AtomicLong count = new AtomicLong();

    private AtomicReference<BigDecimal> cash = new AtomicReference<>(BigDecimal.ZERO);

    private boolean init = true;

    public DeltaStrategy(String symbol, String currency, FuturesStrategy futuresStrategy1, FuturesStrategy futuresStrategy2) {
        this.symbol = symbol;
        this.currency = currency;
        this.futuresStrategy1 = futuresStrategy1;
        this.futuresStrategy2 = futuresStrategy2;
    }

    @Inject
    private void init(){
        //noinspection Duplicates
        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            try {
                okexExchange.createMarketOrder(1,  SELL, OPEN, symbol, currency, "i-s-o-" + System.nanoTime());
                okexExchange.createMarketOrder(1,  BUY, OPEN, symbol, currency, "i-b-o-" + System.nanoTime());
                Thread.sleep(5000);
                okexExchange.createMarketOrder(1,  SELL, CLOSE, symbol, currency, "i-s-c-" + System.nanoTime());
                okexExchange.createMarketOrder(1,  BUY, CLOSE, symbol, currency, "i-b-c-" + System.nanoTime());
            } catch (Exception e) {
                log.error("aida-init create order", e);
            }
        }, 10, TimeUnit.SECONDS);
    }

    @Inject
    public void subscribe(){
        //position
        //noinspection Duplicates
        okexExchange.getPositions()
                .filter(p -> symbol.equals(p.getSymbol()) && currency.equals(p.getCurrency()))
                .subscribe(p -> {
                    if ("long".equals(p.getType())){
                        longPosition.set(p);
                    }else if ("short".equals(p.getType())){
                        shortPosition.set(p);
                    }
                });

        okexExchange.getInfos().subscribe(i -> info.set(i));

        okexExchange.getTrades()
                .filter(t -> currency.equals(t.getCurrency()) && "quarter".equals(t.getSymbol()))
                .subscribe(t -> {
                    count.incrementAndGet();
                    trade.set(t);
                });
    }

    @Inject
    public void schedule(){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                if (init || count.get() > 10000) {
                    count.set(0);
                    init = false;

//                    int delta = futuresStrategy1.getShortQty() + futuresStrategy2.getShortQty() -
//                            futuresStrategy1.getLongQty() - futuresStrategy2.getLongQty() -
//                            cash.divide(new BigDecimal("100"), RoundingMode.HALF_EVEN).intValue();

                    int delta = 0;

                    if (delta != 0) {
                        if (delta > 0){
                            if (shortPosition.get().getQty() > 0){
                                okexExchange.createMarketOrder(shortPosition.get().getQty(),  SELL, CLOSE, symbol, currency, "d-s-c-" + System.nanoTime());
                            }

                            int longDelta = delta - longPosition.get().getQty();

                            if (longDelta != 0) {
                                if (longDelta > 0){
                                    okexExchange.createMarketOrder(longDelta,  BUY, OPEN, symbol, currency, "d-l-o-" + System.nanoTime());
                                }else{
                                    okexExchange.createMarketOrder(-longDelta,  BUY, CLOSE, symbol, currency, "d-l-c-" + System.nanoTime());
                                }
                            }
                        }else{
                            if (longPosition.get().getQty() > 0){
                                okexExchange.createMarketOrder(longPosition.get().getQty(),  BUY, CLOSE, symbol, currency, "d-l-c-" + System.nanoTime());
                            }

                            int shortDelta = -delta - shortPosition.get().getQty();

                            if (shortDelta != 0) {
                                if (shortDelta > 0){
                                    okexExchange.createMarketOrder(shortDelta,  SELL, OPEN, symbol, currency, "d-s-o-" + System.nanoTime());
                                }else{
                                    okexExchange.createMarketOrder(-shortDelta,  SELL, CLOSE, symbol, currency, "d-s-c-" + System.nanoTime());
                                }
                            }
                        }
                    }else{
                        if (shortPosition.get().getQty() > 0){
                            okexExchange.createMarketOrder(shortPosition.get().getQty(),  SELL, CLOSE, symbol, currency, "d-s-c-" + System.nanoTime());
                        }
                        if (longPosition.get().getQty() > 0){
                            okexExchange.createMarketOrder(longPosition.get().getQty(),  BUY, CLOSE, symbol, currency, "d-l-c-" + System.nanoTime());
                        }
                    }
                }
            } catch (Exception e) {
                log.error("error delta scheduler", e);
            }
        }, 1, 1, TimeUnit.MINUTES);

//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            try {
//                FuturesStrategy highStrategy = futuresStrategy1.getLastTrade().getPrice().compareTo(futuresStrategy2.getLastTrade().getPrice()) > 0
//                        ? futuresStrategy1 : futuresStrategy2;
//                FuturesStrategy lowStrategy = futuresStrategy1.getLastTrade().getPrice().compareTo(futuresStrategy2.getLastTrade().getPrice()) < 0
//                        ? futuresStrategy1 : futuresStrategy2;
//
//                if (highStrategy == lowStrategy){
//                    log.error("error longStrategy == shortStrategy");
//
//                    return;
//                }
//
//                //todo
//
//            } catch (Exception e) {
//                log.error("error delta scheduler", e);
//            }
//        }, 1, 1, TimeUnit.MINUTES);
//
//        //todo close created
    }
}
