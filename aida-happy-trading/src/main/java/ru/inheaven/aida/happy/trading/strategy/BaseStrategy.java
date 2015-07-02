package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.OrderService;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

/**
 * @author inheaven on 002 02.07.15 16:43
 */
public class BaseStrategy {
    private List<Order> orders = new ArrayList<>();

    @Inject
    public BaseStrategy(OrderMapper orderMapper, OrderService orderService, Strategy strategy) {
        orders.addAll(orderMapper.getOpenOrders(strategy.getId()));




    }
}
