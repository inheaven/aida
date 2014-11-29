package ru.inheaven.aida.coin.entity;

import com.xeiam.xchange.dto.Order;
import ru.inhell.aida.common.util.DateUtil;

import javax.persistence.*;
import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 09.11.2014 14:06
 */
@Entity
@Table(name = "order_history")
@XmlRootElement
public class OrderHistory extends AbstractEntity {
    @Column(nullable = false)
    private String orderId;

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String pair;

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private Order.OrderType type;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal tradableAmount;

    @Column(nullable = true, precision = 19, scale = 8)
    private BigDecimal filledAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP)
    private Date opened;

    @Column(nullable = true) @Temporal(TemporalType.TIMESTAMP)
    private Date closed;

    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private OrderStatus status;

    public OrderHistory() {
    }

    public OrderHistory(String orderId, ExchangeType exchangeType, String pair, com.xeiam.xchange.dto.Order.OrderType type,
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

    public com.xeiam.xchange.dto.Order.OrderType getType() {
        return type;
    }

    public void setType(com.xeiam.xchange.dto.Order.OrderType type) {
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

    @Override
    public String toString() {
        return DateUtil.getTimeString(closed) + " " + exchangeType.getShortName() + " " +
                pair + " " + tradableAmount.toPlainString() + " @ " +
                price.toPlainString() + " " + type.name() + " " +
                (!status.equals(OrderStatus.CLOSED) ? status.name() : "");
    }
}
