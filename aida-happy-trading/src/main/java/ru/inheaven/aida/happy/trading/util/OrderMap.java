package ru.inheaven.aida.happy.trading.util;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentNavigableMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

import static java.lang.Boolean.TRUE;
import static java.math.RoundingMode.HALF_EVEN;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 07.10.2015 11:59.
 */
public class OrderMap {
    private ConcurrentHashMap<String, Order> idMap = new ConcurrentHashMap<>();

    private ConcurrentSkipListMap<BigDecimal, Collection<Order>> bidMap = new ConcurrentSkipListMap<>();
    private ConcurrentSkipListMap<BigDecimal, Collection<Order>> askMap = new ConcurrentSkipListMap<>();

    private ConcurrentHashMap<Long, Boolean> bidPositionMap = new ConcurrentHashMap<>();

    private int scale;

    public OrderMap(int scale) {
        this.scale = scale;
    }

    public void put(Order order){
        idMap.put(order.getOrderId(), order);

        Map<BigDecimal, Collection<Order>> map = order.getType().equals(BID) ? bidMap : askMap;
        BigDecimal price = scale(order.getPrice());

        Collection<Order> collection = map.get(price);

        if (collection == null){
            collection = new CopyOnWriteArrayList<>();
            map.put(price, collection);
        }

        collection.add(order);

        if (order.getType().equals(BID)){
            bidPositionMap.put(order.getPositionId(), TRUE);
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
            idMap.remove(order.getInternalId());

            Collection<Order> collection = (order.getType().equals(BID) ? bidMap : askMap).get(scale(order.getPrice()));

            if (collection != null){
                collection.removeIf(o -> order.getOrderId().equals(o.getOrderId()) ||
                        order.getInternalId().equals(o.getInternalId()));
            }

            if (order.getType().equals(BID)){
                bidPositionMap.remove(order.getPositionId());
            }
        }
    }

    public Order get(String orderId){
        return idMap.get(orderId);
    }

    public boolean contains(String orderId){
        return idMap.containsKey(orderId);
    }

    public NavigableMap<BigDecimal, Collection<Order>> get(BigDecimal price, OrderType type, boolean inclusive){
        return type.equals(BID) ? bidMap.tailMap(scale(price), inclusive) : askMap.headMap(scale(price), inclusive);
    }

    public NavigableMap<BigDecimal, Collection<Order>> get(BigDecimal price, OrderType type){
        return get(price, type, true);
    }

    public boolean contains(BigDecimal price, BigDecimal spread, OrderType type){
        ConcurrentNavigableMap<BigDecimal, Collection<Order>> map =  type.equals(BID)
                ? bidMap.subMap(scale(price).subtract(scale(spread)), false, scale(price).add(scale(spread)), false)
                : askMap.subMap(scale(price).subtract(scale(spread)), false, scale(price).add(scale(spread)), false);

        boolean contains = false;

        for (Map.Entry<BigDecimal, Collection<Order>> entry : map.entrySet()) {
            for (Order o : entry.getValue()){
                if (OPEN.equals(o.getStatus()) || CREATED.equals(o.getStatus()) || WAIT.equals(o.getStatus())){
                    contains = true;

                    break;
                }

                if (contains){
                    break;
                }
            }
        }

        return contains;
    }

    public boolean containsBid(Long positionId){
        return bidPositionMap.containsKey(positionId);
    }

    private BigDecimal scale(BigDecimal value){
        return value.setScale(scale, HALF_EVEN);
    }
}
