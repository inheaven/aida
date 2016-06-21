package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

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

    public BigDecimal getTradeStdDevPtType(String symbol, int points, OrderType orderType){
        return sqlSession().selectOne("selectTradeStdDevPtType", of("symbol", symbol, "points", points, "orderType", orderType));
    }

    public BigDecimal getTradeAvgAmountPt(String symbol, int points){
        return sqlSession().selectOne("selectTradeAvgAmountPt", of("symbol", symbol, "points", points));
    }

    public BigDecimal getTradeAvgPricePt(String symbol, int points){
        return sqlSession().selectOne("selectTradeAvgPricePt", of("symbol", symbol, "points", points));
    }

    public List<Trade> getLightTrades(String symbol, Date startDate, Date endDate, int first, int count){
        return sqlSession().selectList("selectLightTrades", of("symbol", symbol, "startDate", startDate, "endDate", endDate, "first", first, "count", count));
    }

    public List<Trade> getLightTrades(String symbol, OrderType orderType, Date startDate, Date endDate){
        return sqlSession().selectList("selectLightTrades", of("symbol", symbol, "orderType", orderType, "startDate", startDate, "endDate", endDate));
    }
}
