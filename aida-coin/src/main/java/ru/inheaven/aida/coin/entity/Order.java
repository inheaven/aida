package ru.inheaven.aida.coin.entity;

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
@Table(name = "\"order\"")
@XmlRootElement
public class Order extends AbstractEntity {
    @Column(nullable = false, unique = true)
    private String orderId;

    @ManyToOne(optional = false)
    private Strategy strategy;

    @ManyToOne(optional = false)
    private Account account;

    @Column(nullable = false)
    private OrderType type;

    @Column(nullable = false)
    private String symbol;

    @Column(nullable = true)
    private SymbolType symbolType;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal amount;

    @Column(nullable = true, precision = 19, scale = 8)
    private BigDecimal filledAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal avgPrice;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal fee;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date open;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created;

    @Column(nullable = true)
    @Temporal(TemporalType.TIMESTAMP)
    private Date closed;

    @Column(nullable = false)
    private OrderStatus status;

    @Column(nullable = true)
    private String name;

    public Order() {
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Strategy getStrategy() {
        return strategy;
    }

    public void setStrategy(Strategy strategy) {
        this.strategy = strategy;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
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

    public BigDecimal getFilledAmount() {
        return filledAmount;
    }

    public void setFilledAmount(BigDecimal filledAmount) {
        this.filledAmount = filledAmount;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public void setFee(BigDecimal fee) {
        this.fee = fee;
    }

    public Date getOpen() {
        return open;
    }

    public void setOpen(Date open) {
        this.open = open;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return DateUtil.getTimeString(closed) + " " + account.getExchangeType().getShortName() + " " +
                symbol + " " + filledAmount.toString() + " @ " +
                price.toString() + " " + type.name() + " " +
                (!status.equals(OrderStatus.CLOSED) ? status.name() : "");
    }
}
