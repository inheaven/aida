package ru.inheaven.aida.happy.trading.mapper;

import com.google.common.collect.ImmutableMap;
import ru.inheaven.aida.happy.trading.entity.Trade;

import java.math.BigDecimal;
import java.util.HashMap;

import static com.google.common.collect.ImmutableMap.of;

/**
 * @author inheaven on 02.08.2015 20:33.
 */
public class TradeMapper extends BaseMapper<Trade> {
    public BigDecimal getTradeStdDev(String symbol, int minute){
        return sqlSession().selectOne("selectTradeStdDev", of("symbol", symbol, "minute", minute));

    }

    public BigDecimal getTradeStdDevPt(String symbol, int points){
        return sqlSession().selectOne("selectTradeStdDevPt", of("symbol", symbol, "points", points));
    }

    public BigDecimal getTradeAvgAmountPt(String symbol, int points){
        return sqlSession().selectOne("selectTradeAvgAmountPt", of("symbol", symbol, "points", points));
    }
}
