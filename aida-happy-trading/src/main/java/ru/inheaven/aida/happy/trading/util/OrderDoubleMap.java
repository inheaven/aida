package ru.inheaven.aida.happy.trading.util;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;

import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 07.10.2015 11:59.
 */
public class OrderDoubleMap {
    private ConcurrentHashMap<String, Order> idMap = new ConcurrentHashMap<>();

    private ConcurrentNavigableMap<Double, Map<String, Order>> bidMap = new ConcurrentSkipListMap<>();
    private ConcurrentNavigableMap<Double, Map<String, Order>> askMap = new ConcurrentSkipListMap<>();

    private ConcurrentSkipListSet<Long> bidPositionMap = new ConcurrentSkipListSet<>();
    private ConcurrentSkipListSet<Long> askPositionMap = new ConcurrentSkipListSet<>();

    private final static double DELTA = 0.1;

    public void put(Order order){
        idMap.put(order.getOrderId(), order);

        Map<Double, Map<String, Order>> map = order.getType().equals(BID) ? bidMap : askMap;

        Double price = order.getPrice().doubleValue();

        Map<String, Order> subMap = map.get(price);

        if (subMap == null){
            subMap = new ConcurrentHashMap<>();
            map.put(price, subMap);
        }

        subMap.put(order.getOrderId(), order);

        if (order.getPositionId() != null) {
            (order.getType().equals(BID) ? bidPositionMap : askPositionMap).add(order.getPositionId());
        }
    }

    public void forEach(BiConsumer<String, Order> action){
        idMap.forEach(action);
    }

    public Collection<Order> values(){
        return idMap.values();
    }

    public void update(Order order){
        update(order, null);
    }

    public void update(Order order, String removeOrderId){
        idMap.put(order.getOrderId(), order);

        if (removeOrderId != null){
            idMap.remove(removeOrderId);
        }

        if (order.getInternalId() != null && !order.getInternalId().equals(order.getOrderId())){
            idMap.remove(order.getInternalId());
        }
    }

    public void remove(String orderId){
        Order order = idMap.remove(orderId);

        if (order != null){
            if (order.getInternalId() != null) {
                idMap.remove(order.getInternalId());
            }

            Double price = order.getPrice().doubleValue();

            ConcurrentNavigableMap<Double, Map<String, Order>> subMap = (order.getType().equals(BID) ? bidMap : askMap)
                    .subMap(price - DELTA, price + DELTA);

            subMap.forEach((k, v) -> v.values()
                    .removeIf(o -> order.getOrderId().equals(o.getOrderId()) ||
                            Objects.equals(order.getInternalId(), o.getInternalId())));

            if (order.getPositionId() != null) {
                (order.getType().equals(BID) ? bidPositionMap : askPositionMap).remove(order.getPositionId());
            }
        }
    }

    public Order get(String orderId){
        return idMap.get(orderId);
    }

    public boolean contains(String orderId){
        return idMap.containsKey(orderId);
    }

    public NavigableMap<Double, Map<String, Order>> get(Double price, OrderType type, boolean inclusive){
        return type.equals(BID) ? bidMap.tailMap(price, inclusive) : askMap.headMap(price, inclusive);
    }

    public NavigableMap<Double, Map<String, Order>> get(Double price, OrderType type){
        return get(price, type, true);
    }

    public boolean contains(Double price, Double spread, OrderType type){
        ConcurrentNavigableMap<Double, Map<String, Order>> map =  type.equals(BID)
                ? bidMap.subMap(price - spread, true, price + spread, true)
                : askMap.subMap(price - spread, true, price + spread, true);

        Set<Map.Entry<Double, Map<String, Order>>> set = map.entrySet();

        for (Map.Entry<Double, Map<String, Order>> entry : set) {
            if (!entry.getValue().isEmpty()){
                return true;
            }
        }

        return false;
    }

    public boolean containsBid(Long positionId){
        return bidPositionMap.contains(positionId);
    }

    public boolean containsAsk(Long positionId){
        return askPositionMap.contains(positionId);
    }


    public ConcurrentNavigableMap<Double, Map<String, Order>> getBidMap() {
        return bidMap;
    }

    public ConcurrentNavigableMap<Double, Map<String, Order>> getAskMap() {
        return askMap;
    }
}
