package ru.inheaven.aida.backtest;

import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;

import java.lang.instrument.Instrumentation;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.IntStream;

import static java.math.RoundingMode.HALF_UP;

/**
 * inheaven on 04.05.2016.
 */
public class LevelBacktest {
    public static void main(String[] args){
        Long time = System.currentTimeMillis();

        LevelBacktest levelBacktest = new LevelBacktest();

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        Date startDate = Date.from(LocalDateTime.of(2016, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 5, 5, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        int count = 1000000;

        IntStream.range(0, 4).parallel().forEach(i ->
                tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, i*count, count).parallelStream()
                        .forEach(levelBacktest::action));

        System.out.println("volume: " + levelBacktest.bidVolume.get().setScale(2, HALF_UP) + " " + levelBacktest.askVolume.get().setScale(2, HALF_UP));
        System.out.println("time: " + (System.currentTimeMillis() - time)/1000 + "s");
        System.out.println("memory: " + levelBacktest.memory.get()/1024/1024 + "mb");
    }

    private AtomicReference<BigDecimal> bidVolume = new AtomicReference<>(BigDecimal.ZERO);
    private AtomicReference<BigDecimal> askVolume = new AtomicReference<>(BigDecimal.ZERO);

    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }

    private AtomicLong memory = new AtomicLong(0);

    private void action(Trade trade){
        memory.addAndGet(instrumentation.getObjectSize(trade));

        if (OrderType.BID.equals(trade.getOrderType())){
            bidVolume.set(bidVolume.get().add(trade.getAmount()));
        }else{
            askVolume.set(askVolume.get().add(trade.getAmount()));
        }
    }
}
