package ru.inheaven.aida.coin.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 09.11.2014 12:23
 */
@Entity
@Table(name = "trade_history")
public class TradeHistory extends AbstractEntity {
    @Column(nullable = false)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String pair;

    @Column(nullable = false)
    private OrderType type;

    @Column(nullable = false, precision = 19, scale = 8)
    protected BigDecimal tradableAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    protected BigDecimal price;

    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP)
    protected Date date;

    @Column(nullable = true)
    protected String tradeId;

    @Column(nullable = true)
    private String orderId;

    @Column(nullable = true, precision = 19, scale = 8)
    private BigDecimal feeAmount;

    @Column(nullable = true)
    private String feeCurrency;

    public TradeHistory() {
    }

    public TradeHistory(ExchangeType exchangeType, String pair, OrderType type, BigDecimal tradableAmount,
                        BigDecimal price, Date date, String tradeId, String orderId, BigDecimal feeAmount,
                        String feeCurrency) {
        this.exchangeType = exchangeType;
        this.pair = pair;
        this.type = type;
        this.tradableAmount = tradableAmount;
        this.price = price;
        this.date = date;
        this.tradeId = tradeId;
        this.orderId = orderId;
        this.feeAmount = feeAmount;
        this.feeCurrency = feeCurrency;
    }

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

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
    }

    public BigDecimal getTradableAmount() {
        return tradableAmount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Date getDate() {
        return date;
    }

    public String getTradeId() {
        return tradeId;
    }

    public String getOrderId() {
        return orderId;
    }

    public BigDecimal getFeeAmount() {
        return feeAmount;
    }

    public String getFeeCurrency() {
        return feeCurrency;
    }
}
