package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderPosition;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Strategy;

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
}
