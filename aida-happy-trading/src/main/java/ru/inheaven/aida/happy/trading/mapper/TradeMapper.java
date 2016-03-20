package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Trade;

import java.math.BigDecimal;

import static com.google.common.collect.ImmutableMap.of;

/**
 * @author inheaven on 02.08.2015 20:33.
 */
public class TradeMapper extends BaseMapper<Trade> {
    public BigDecimal getTradeStdDev(ExchangeType exchangeType, String symbol, int minute){
        return sqlSession().selectOne("selectTradeStdDev", of("exchangeType", exchangeType, "symbol", symbol,
                "minute", minute));
    }

    public BigDecimal getTradeStdDevPt(ExchangeType exchangeType, String symbol, int points){
        return sqlSession().selectOne("selectTradeStdDevPt", of("exchangeType", exchangeType, "symbol", symbol,
                "points", points));
    }

    public BigDecimal getTradeAvgAmountPt(ExchangeType exchangeType, String symbol, int points){
        return sqlSession().selectOne("selectTradeAvgAmountPt", of("exchangeType", exchangeType, "symbol", symbol,
                "points", points));
    }

    public BigDecimal getTradeAvgPricePt(ExchangeType exchangeType, String symbol, int points){
        return sqlSession().selectOne("selectTradeAvgPricePt", of("exchangeType", exchangeType, "symbol", symbol,
                "points", points));
    }
}
