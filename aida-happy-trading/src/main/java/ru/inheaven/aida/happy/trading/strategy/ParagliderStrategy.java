package ru.inheaven.aida.happy.trading.strategy;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Strategy;
import ru.inheaven.aida.happy.trading.service.OrderService;

import java.util.List;

/**
 * @author inheaven on 29.06.2015 23:38.
 */
public class ParagliderStrategy {
    private Strategy strategy;
    private OrderService orderService;

    private List<Order> orders;

    public ParagliderStrategy(Strategy strategy, OrderService orderService) {
        this.strategy = strategy;
        this.orderService = orderService;


    }


}
