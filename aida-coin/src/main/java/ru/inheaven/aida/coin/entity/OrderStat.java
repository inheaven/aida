package ru.inheaven.aida.coin.entity;

import com.xeiam.xchange.dto.Order;

import javax.persistence.*;
import java.math.BigDecimal;

/**
 * inheaven on 12.11.2014 23:40 23:41 23:41.
 */
@Entity
public class OrderStat extends AbstractEntity{
    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String pair;

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private Order.OrderType type;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal sumAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal avgPrice;

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public Order.OrderType getType() {
        return type;
    }

    public void setType(Order.OrderType type) {
        this.type = type;
    }

    public BigDecimal getSumAmount() {
        return sumAmount;
    }

    public void setSumAmount(BigDecimal sumAmount) {
        this.sumAmount = sumAmount;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

}
