package ru.inheaven.aida.happy.trading.mapper;

import ru.inheaven.aida.happy.trading.entity.Order;

import java.util.List;

/**
 * @author inheaven on 29.06.2015 23:56.
 */
public class OrderMapper extends BaseMapper<Order>{
    public List<Order> getOpenOrders(){
        return sqlSession().selectList("selectOpenOrders");
    }
}
