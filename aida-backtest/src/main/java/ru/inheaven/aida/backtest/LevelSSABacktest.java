package ru.inheaven.aida.backtest;

import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;

/**
 * @author inheaven on 12.06.2016.
 */
public class LevelSSABacktest {
    public static void main(String... args){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 11, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 12, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);



    }
}
