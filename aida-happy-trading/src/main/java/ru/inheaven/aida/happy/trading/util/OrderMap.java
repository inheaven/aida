package ru.inheaven.aida.happy.trading.util;

import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.function.BiConsumer;

import static java.math.RoundingMode.HALF_EVEN;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * @author inheaven on 07.10.2015 11:59.
 */
public class OrderMap {
    private ConcurrentHashMap<String, Order> idMap = new ConcurrentHashMap<>();

    private ConcurrentSkipListMap<BigDecimal, Map<String, Order>> bidMap = new ConcurrentSkipListMap<>();
    private ConcurrentSkipListMap<BigDecimal, Map<String, Order>> askMap = new ConcurrentSkipListMap<>();

    private ConcurrentSkipListSet<Long> bidPositionMap = new ConcurrentSkipListSet<>();
    private ConcurrentSkipListSet<Long> askPositionMap = new ConcurrentSkipListSet<>();

    private int scale;

    public OrderMap(int scale) {
        this.scale = scale;
    }

    public void put(Order order){
        idMap.put(order.getOrderId(), order);

        Map<BigDecimal, Map<String, Order>> map = order.getType().equals(BID) ? bidMap : askMap;
        BigDecimal price = scale(order.getPrice());

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

            Map<String, Order> subMap = (order.getType().equals(BID) ? bidMap : askMap).get(scale(order.getPrice()));

            if (subMap != null){
                subMap.values().removeIf(o -> order.getOrderId().equals(o.getOrderId()) ||
                        Objects.equals(order.getInternalId(), o.getInternalId()));
            }

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

    public NavigableMap<BigDecimal, Map<String, Order>> get(BigDecimal price, OrderType type, boolean inclusive){
        return type.equals(BID) ? bidMap.tailMap(scale(price), inclusive) : askMap.headMap(scale(price), inclusive);
    }

    public NavigableMap<BigDecimal, Map<String, Order>> get(BigDecimal price, OrderType type){
        return get(price, type, true);
    }

    public boolean contains(BigDecimal price, BigDecimal spread, OrderType orderType){
        Map<BigDecimal, Map<String, Order>> bid = bidMap.subMap(scale(price.subtract(spread)), true, scale(price.add(spread)), true);

        for (Map.Entry<BigDecimal, Map<String, Order>> entry : bid.entrySet()) {
            if (!entry.getValue().isEmpty()){
                return true;
            }
        }

        Map<BigDecimal, Map<String, Order>> ask = askMap.subMap(scale(price.subtract(spread)), true, scale(price.add(spread)), true);

        for (Map.Entry<BigDecimal, Map<String, Order>> entry : ask.entrySet()) {
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

    private BigDecimal scale(BigDecimal value){
        return value.setScale(scale, HALF_EVEN);
    }

    public ConcurrentSkipListMap<BigDecimal, Map<String, Order>> getBidMap() {
        return bidMap;
    }

    public ConcurrentSkipListMap<BigDecimal, Map<String, Order>> getAskMap() {
        return askMap;
    }
}
