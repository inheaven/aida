package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Depth;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.SymbolType;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

/**
 * @author inheaven on 04.08.2015 0:38.
 */
public class DepthMapper extends BaseMapper<Depth>{
    public List<Depth> getDepths(ExchangeType exchangeType, String symbol, SymbolType symbolType, Date startDate){
        return sqlSession().selectList("selectDepths", new HashMap<String, Object>(){{
            put("exchangeType", exchangeType);
            put("symbol", symbol);
            put("symbolType", symbolType);
            put("startDate", startDate);
        }});
    }
}
