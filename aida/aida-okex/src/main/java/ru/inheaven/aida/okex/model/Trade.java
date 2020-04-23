package ru.inheaven.aida.okex.model;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Date;

public class Trade {
    private Long id;
    private String currency;
    private Date origTime;
    private String symbol;
    private Long orderId;
    private String side;
    private BigDecimal price;
    private Integer qty;
    private Date created;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public Date getOrigTime() {
        return origTime;
    }

    public void setOrigTime(Date origTime) {
        this.origTime = origTime;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", id)
                .add("currency", currency)
                .add("origTime", origTime)
                .add("symbol", symbol)
                .add("orderId", orderId)
                .add("side", side)
                .add("price", price)
                .add("qty", qty)
                .add("created", created)
                .toString();
    }
}
