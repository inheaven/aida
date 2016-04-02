package ru.inheaven.aida.happy.trading.entity;

import java.math.BigDecimal;

/**
 * inheaven on 02.04.2016.
 */
public class ActionPrice {
    private BigDecimal price;
    private BigDecimal amount;
    private OrderType orderType;
    private String key;

    public ActionPrice() {
    }

    public ActionPrice(BigDecimal price, BigDecimal amount, OrderType orderType, String key) {
        this.price = price;
        this.amount = amount;
        this.orderType = orderType;
        this.key = key;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
}
