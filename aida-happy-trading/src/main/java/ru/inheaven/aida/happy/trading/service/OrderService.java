package ru.inheaven.aida.happy.trading.service;

import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CANCELED;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.CLOSED;

/**
 * @author inheaven on 29.06.2015 23:49.
 */
@Singleton
public class OrderService {
    private Map<Long, Map<String, Order>> orderMap;
    private Stream<Map<String, Order>> orderMapStream;

    @Inject
    public OrderService(OrderMapper orderMapper, OkcoinService okcoinService) {
        orderMap = new ConcurrentHashMap<>();
        orderMapStream = orderMap.values().parallelStream();

        //init
        orderMapper.getOpenOrders().forEach(order -> {
            getOrders(order.getStrategyId()).put(order.getOrderId(), order);
        });

        //order
        okcoinService.getOrderObservable().subscribe(o -> {
            if (o.getStatus().equals(CLOSED) || o.getStatus().equals(CANCELED)){
                Order order = getOrder(ExchangeType.OKCOIN_FUTURES, o.getOrderId());

                if (order != null){
                    order.update(o);
                    orderMap.get(order.getStrategyId()).remove(order.getOrderId());
                    orderMapper.save(order);
                }
            }
        });

        //trade
        okcoinService.getTradeObservable().subscribe(t -> {

        });
    }

    public Map<String, Order> getOrders(Long strategyId){
        Map<String, Order> map = orderMap.get(strategyId);

        if (map == null){
            map = new ConcurrentHashMap<>();
            orderMap.put(strategyId, map);
        }

        return map;
    }

    public Order getOrder(ExchangeType exchangeType, String orderId){
        return orderMapStream
                .filter(m -> m.containsKey(orderId))
                .map(m -> m.get(orderId))
                .filter(o -> o.getExchangeType().equals(exchangeType))
                .findAny()
                .orElse(null);
    }



}
