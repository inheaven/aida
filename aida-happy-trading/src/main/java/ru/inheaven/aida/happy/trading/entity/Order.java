package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 09.11.2014 14:06
 */
public class Order extends AbstractEntity {
    private String orderId;
    private Long strategyId;
    private ExchangeType exchangeType;
    private OrderType type;
    private String symbol;
    private SymbolType symbolType;
    private BigDecimal price;
    private BigDecimal amount;
    private BigDecimal filledAmount;
    private BigDecimal avgPrice;
    private BigDecimal fee;
    private Date created;
    private Date open;
    private Date closed;
    private OrderStatus status;

    public Order() {
    }

    public Order(Strategy strategy, OrderType type, BigDecimal price, BigDecimal amount) {
        this.strategyId = strategy.getId();
        this.exchangeType = strategy.getAccount().getExchangeType();
        this.symbol = strategy.getSymbol();
        this.symbolType = strategy.getSymbolType();

        this.type = type;
        this.price = price;
        this.amount = amount;
    }

    public void close(Order order){
        filledAmount = order.filledAmount;
        avgPrice = order.avgPrice;
        fee = order.fee;
        closed = order.closed != null ? order.closed : new Date();
        status = order.status;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public OrderType getType() {
        return type;
    }

    public void setType(OrderType type) {
        this.type = type;
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



}
