package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 01.05.2015 9:11.
 */
public class Trade extends AbstractEntity {
    private String tradeId;
    private ExchangeType exchangeType;
    private String symbol;
    private SymbolType symbolType;
    private OrderType orderType;
    private BigDecimal price;
    private BigDecimal amount;
    private String time;
    private Date created;
    private Date origTime;

    public Trade() {
    }

    public String getTradeId() {
        return tradeId;
    }

    public void setTradeId(String tradeId) {
        this.tradeId = tradeId;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
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

    public OrderType getOrderType() {
        return orderType;
    }

    public void setOrderType(OrderType orderType) {
        this.orderType = orderType;
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

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getOrigTime() {
        return origTime;
    }

    public void setOrigTime(Date origTime) {
        this.origTime = origTime;
    }

    @Override
    public String toString() {
        return "Trade{" +
                "tradeId='" + tradeId + '\'' +
                ", exchangeType=" + exchangeType +
                ", symbol='" + symbol + '\'' +
                ", symbolType=" + symbolType +
                ", orderType=" + orderType +
                ", price=" + price +
                ", amount=" + amount +
                ", time='" + time + '\'' +
                ", created=" + created +
                '}';
    }
}
