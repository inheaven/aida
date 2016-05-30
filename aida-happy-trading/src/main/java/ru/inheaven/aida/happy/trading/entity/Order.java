package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 09.11.2014 14:06
 */
public class Order extends AbstractEntity {
    private volatile String orderId;
    private volatile String internalId;
    private volatile Long strategyId;
    private volatile Long positionId;
    private volatile ExchangeType exchangeType;
    private volatile OrderType type;
    private volatile String symbol;
    private volatile SymbolType symbolType;
    private volatile BigDecimal price;
    private volatile BigDecimal amount;
    private volatile BigDecimal filledAmount;
    private volatile BigDecimal avgPrice;
    private volatile BigDecimal fee;
    private volatile Date created;
    private volatile Date open;
    private volatile Date closed;
    private volatile OrderStatus status;
    private volatile String text;

    private BigDecimal buyPrice;
    private BigDecimal sellPrice;
    private BigDecimal buyVolume;
    private BigDecimal sellVolume;
    private BigDecimal spotBalance;
    private BigDecimal spread;

    private BigDecimal profit;
    private double forecast;
    private boolean balance;

    private volatile Long accountId;

    public Order() {
    }

    public Order(String orderId, OrderType type, BigDecimal price, BigDecimal amount) {
        this.orderId = orderId;
        this.type = type;
        this.price = price;
        this.amount = amount;
    }

    public Order(Strategy strategy, OrderType type, BigDecimal price, BigDecimal amount) {
        if (strategy != null) {
            this.strategyId = strategy.getId();
            this.exchangeType = strategy.getAccount().getExchangeType();
            this.symbol = strategy.getSymbol();
            this.symbolType = strategy.getSymbolType();
        }

        this.type = type;
        this.price = price;
        this.amount = amount;
    }

    public Order(Strategy strategy, Long positionId, OrderType type, BigDecimal price, BigDecimal amount) {
        this(strategy, type, price, amount);

        this.positionId = positionId;
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

    public Long getPositionId() {
        return positionId;
    }

    public void setPositionId(Long positionId) {
        this.positionId = positionId;
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

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getInternalId() {
        return internalId;
    }

    public void setInternalId(String internalId) {
        this.internalId = internalId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public BigDecimal getBuyPrice() {
        return buyPrice;
    }

    public void setBuyPrice(BigDecimal buyPrice) {
        this.buyPrice = buyPrice;
    }

    public BigDecimal getSellPrice() {
        return sellPrice;
    }

    public void setSellPrice(BigDecimal sellPrice) {
        this.sellPrice = sellPrice;
    }

    public BigDecimal getBuyVolume() {
        return buyVolume;
    }

    public void setBuyVolume(BigDecimal buyVolume) {
        this.buyVolume = buyVolume;
    }

    public BigDecimal getSellVolume() {
        return sellVolume;
    }

    public void setSellVolume(BigDecimal sellVolume) {
        this.sellVolume = sellVolume;
    }

    public BigDecimal getSpotBalance() {
        return spotBalance;
    }

    public void setSpotBalance(BigDecimal spotBalance) {
        this.spotBalance = spotBalance;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getSpread() {
        return spread;
    }

    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }

    public double getForecast() {
        return forecast;
    }

    public void setForecast(double forecast) {
        this.forecast = forecast;
    }

    public boolean isBalance() {
        return balance;
    }

    public void setBalance(boolean balance) {
        this.balance = balance;
    }

    @Override
    public String toString() {
        return "Order{" +
                "orderId='" + orderId + '\'' +
                ", strategyId=" + strategyId +
                ", positionId=" + positionId +
                ", exchangeType=" + exchangeType +
                ", type=" + type +
                ", symbol='" + symbol + '\'' +
                ", symbolType=" + symbolType +
                ", price=" + price +
                ", amount=" + amount +
                ", filledAmount=" + filledAmount +
                ", avgPrice=" + avgPrice +
                ", fee=" + fee +
                ", created=" + created +
                ", open=" + open +
                ", closed=" + closed +
                ", status=" + status +
                ", accountId=" + accountId +
                ", internalId='" + internalId + '\'' +
                '}';
    }
}
