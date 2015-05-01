package ru.inheaven.aida.coin.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 01.05.2015 9:11.
 */
@Entity
public class Trade extends AbstractEntity{
    @Column(nullable = false)
    private String tradeId;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private SymbolType symbolType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OrderType orderType;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal amount;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

    public Trade() {
    }

    public Trade(String tradeId, BigDecimal price, BigDecimal amount, Date date, OrderType orderType, SymbolType symbolType) {
        this.tradeId = tradeId;
        this.price = price;
        this.amount = amount;
        this.date = date;
        this.orderType = orderType;
        this.symbolType = symbolType;
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }
}
