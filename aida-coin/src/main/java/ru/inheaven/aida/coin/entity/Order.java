package ru.inheaven.aida.coin.entity;

import ru.inhell.aida.common.util.DateUtil;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 09.11.2014 14:06
 */
@Entity
@Table(name = "\"order\"")
@XmlRootElement
public class Order extends AbstractEntity {
    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String pair;

    @Column(nullable = false)
    private OrderType type;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal tradableAmount;

    @Column(nullable = true, precision = 19, scale = 8)
    private BigDecimal filledAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date opened;

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date closed;

    @Column(nullable = false)
    private OrderStatus status;

    @ManyToOne
    @JoinColumn(name = "trader_id")
    private Trader trader;

    public Order() {
    }

    public Order(String orderId, ExchangeType exchangeType, String pair, OrderType type,
                 BigDecimal tradableAmount, BigDecimal price, Date opened) {
        this.orderId = orderId;
        this.exchangeType = exchangeType;
        this.pair = pair;
        this.type = type;
        this.tradableAmount = tradableAmount;
        this.price = price;
        this.opened = opened;

        this.status = OrderStatus.OPENED;
    }

    public void setPriceScale(int scale){
        price = price.setScale(scale, RoundingMode.HALF_UP);
    }

    public void setFilledAmountScale(int scale){
        filledAmount = filledAmount.setScale(scale, RoundingMode.HALF_UP);
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
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

    public void setTradableAmount(BigDecimal tradableAmount) {
        this.tradableAmount = tradableAmount;
    }

    public BigDecimal getFilledAmount() {
        return filledAmount;
    }

    public void setFilledAmount(BigDecimal filledAmount) {
        this.filledAmount = filledAmount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getOpened() {
        return opened;
    }

    public void setOpened(Date opened) {
        this.opened = opened;
    }

    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public Trader getTrader() {
        return trader;
    }

    public void setTrader(Trader trader) {
        this.trader = trader;
    }

    @Override
    public String toString() {
        return DateUtil.getTimeString(closed) + " " + exchangeType.getShortName() + " " +
                pair + " " + filledAmount.toString() + " @ " +
                price.toString() + " " + type.name() + " " +
                (!status.equals(OrderStatus.CLOSED) ? status.name() : "");
    }
}
