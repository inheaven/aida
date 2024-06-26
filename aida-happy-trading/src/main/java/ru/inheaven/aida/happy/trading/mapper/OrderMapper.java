package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderPosition;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Strategy;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author inheaven on 29.06.2015 23:56.
 */
public class OrderMapper extends BaseMapper<Order>{
    public List<Order> getOpenOrders(Long strategyId){
        return sqlSession().selectList("selectOpenOrders", strategyId);
    }

    public Map<OrderType, OrderPosition> getOrderPositionMap(Strategy strategy){
        return sqlSession().selectMap("selectOrderPosition", strategy, "type");
    }

    public Integer getOrderCount(Strategy strategy, OrderType orderType){
        return sqlSession().selectOne("selectOrderCount", new HashMap<String, Object>() {{
            put("id", strategy.getId());
            put("sessionStart", strategy.getSessionStart());
            put("orderType", orderType);
        }});
    }

    public BigDecimal getOrderVolume(Strategy strategy, OrderType orderType, Integer first, Integer count){
        return sqlSession().selectOne("selectOrderVolume", new HashMap<String, Object>(){{
            put("id", strategy.getId());
            put("sessionStart", strategy.getSessionStart());
            put("orderType", orderType);
            put("first", first);
            put("count", count);
        }});
    }

    public Long getAllOrderRate(){
        return sqlSession().selectOne("selectAllOrderRate");
    }

    public List<Date> getLast100OrderTimes(){
        return sqlSession().selectList("selectLast100OrderTimes");
    }

    public List<Date> getLast6HourOrderTimes(){
        return sqlSession().selectList("selectLast6HourOrderTimes");
    }

    public BigDecimal getMinTradeProfit(Long accountId, Long strategyId, Date startDate, Date endDate){
        return sqlSession().selectOne("selectMinTradeProfit", getMap(accountId, strategyId, startDate, endDate));
    }

    public BigDecimal getRandomTradeProfit(Long accountId, Long strategyId, Date startDate, Date endDate){
        return sqlSession().selectOne("selectRandomTradeProfit", getMap(accountId, strategyId, startDate, endDate));
    }

    public BigDecimal getMinTradeVolume(Long accountId, Long strategyId, Date startDate, Date endDate){
        return sqlSession().selectOne("selectMinTradeVolume", getMap(accountId, strategyId, startDate, endDate));
    }

    public BigDecimal getRandomTradeVolume(Long accountId, Long strategyId, Date startDate, Date endDate){
        return sqlSession().selectOne("selectRandomTradeVolume",getMap(accountId, strategyId, startDate, endDate));
    }

    public BigDecimal getTradeCount(Long accountId,  Long strategyId, Date startDate, Date endDate){
        return sqlSession().selectOne("selectTradeCount", getMap(accountId, strategyId, startDate, endDate));
    }

    private Map<String, Object> getMap(Long accountId,  Long strategyId, Date startDate, Date endDate){
        return new HashMap<String, Object>(){{
            put("accountId", accountId);
            put("strategyId", strategyId);
            put("startDate", startDate);
            put("endDate", endDate);
        }};
    }
}
