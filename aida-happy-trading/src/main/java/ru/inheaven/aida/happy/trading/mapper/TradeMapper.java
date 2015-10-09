package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Trade;

import java.math.BigDecimal;
import java.util.HashMap;

/**
 * @author inheaven on 02.08.2015 20:33.
 */
public class TradeMapper extends BaseMapper<Trade> {
    public BigDecimal getTradeStdDev(String symbol, int minute){
        return sqlSession().selectOne("selectTradeStdDev", new HashMap<String, Object>(){{
            put("symbol", symbol);
            put("minute", minute);
        }}) ;
    }
}
