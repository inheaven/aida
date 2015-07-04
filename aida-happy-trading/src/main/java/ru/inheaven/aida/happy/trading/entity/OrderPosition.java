package ru.inheaven.aida.happy.trading.entity;

/**
 * @author inheaven on 04.07.2015 22:23.
 */
public class OrderPosition {
    private OrderType type;
    private Integer count;

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }
}
