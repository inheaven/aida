package ru.inheaven.aida.backtest;

import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.stream.IntStream;

/**
 * inheaven on 04.05.2016.
 */
public class LevelBacktest {
    public static void main(String[] args){
        LevelBacktest levelBacktest = new LevelBacktest();

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        Date startDate = Date.from(LocalDateTime.of(2016, 5, 4, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        int count = 32;

        IntStream.range(0, 1000).parallel().forEach(i ->
                tradeMapper.getLightTrades("BTC/CNY", startDate, i*count, count).parallelStream()
                        .forEach(levelBacktest::action));

        System.out.println(levelBacktest.bidVolume + " " + levelBacktest.askVolume);
    }

    private BigDecimal bidVolume = BigDecimal.ZERO;
    private BigDecimal askVolume = BigDecimal.ZERO;

    private void action(Trade trade){
        if (OrderType.BID.equals(trade.getOrderType())){
            bidVolume = bidVolume.add(trade.getAmount());
        }else{
            askVolume = askVolume.add(trade.getAmount());
        }
    }
}
