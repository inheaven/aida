package ru.inheaven.aida.happy.trading.entity;

import java.math.BigDecimal;

/**
 * @author inheaven on 04.07.2015 22:23.
 */
public class OrderPosition {
    private OrderType type;
    private Integer count;
    private BigDecimal avg;
    private BigDecimal price;

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

    public BigDecimal getAvg() {
        return avg;
    }

    public void setAvg(BigDecimal avg) {
        this.avg = avg;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
