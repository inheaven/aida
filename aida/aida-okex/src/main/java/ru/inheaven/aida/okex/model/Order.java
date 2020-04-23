package ru.inheaven.aida.okex.model;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Date;

public class Order {
    private Long id;
    private Long strategyId;
    private BigDecimal avgPrice;
    private String clOrderId;
    private BigDecimal commission;
    private Integer totalQty;
    private String currency;
    private String execId;
    private String orderId;
    private Integer qty;
    private String status;
    private String type;
    private BigDecimal price;
    private String side;
    private String symbol;
    private String text;
    private Date txTime;
    private String execType;
    private Integer leavesQty;
    private Integer marginRatio;
    private Date created = new Date();
    private Date closed;

    public Order() {
    }

    public Order(String clOrderId, String currency, Integer qty, BigDecimal price, String symbol) {
        this.clOrderId = clOrderId;
        this.currency = currency;
        this.qty = qty;
        this.price = price;
        this.symbol = symbol;
    }

    public Order(String symbol, String currency, String side, String type, BigDecimal price, Integer qty) {
        this.currency = currency;
        this.qty = qty;
        this.type = type;
        this.price = price;
        this.side = side;
        this.symbol = symbol;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public String getClOrderId() {
        return clOrderId;
    }

    public void setClOrderId(String clOrderId) {
        this.clOrderId = clOrderId;
    }

    public BigDecimal getCommission() {
        return commission;
    }

    public void setCommission(BigDecimal commission) {
        this.commission = commission;
    }

    public Integer getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(Integer totalQty) {
        this.totalQty = totalQty;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getExecId() {
        return execId;
    }

    public void setExecId(String execId) {
        this.execId = execId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getSide() {
        return side;
    }

    public void setSide(String side) {
        this.side = side;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getTxTime() {
        return txTime;
    }

    public void setTxTime(Date txTime) {
        this.txTime = txTime;
    }

    public String getExecType() {
        return execType;
    }

    public void setExecType(String execType) {
        this.execType = execType;
    }

    public Integer getLeavesQty() {
        return leavesQty;
    }

    public void setLeavesQty(Integer leavesQty) {
        this.leavesQty = leavesQty;
    }

    public Integer getMarginRatio() {
        return marginRatio;
    }

    public void setMarginRatio(Integer marginRatio) {
        this.marginRatio = marginRatio;
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

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("strategyId", strategyId)
                .add("avgPrice", avgPrice)
                .add("clOrderId", clOrderId)
                .add("commission", commission)
                .add("totalQty", totalQty)
                .add("currency", currency)
                .add("execId", execId)
                .add("orderId", orderId)
                .add("qty", qty)
                .add("status", status)
                .add("type", type)
                .add("price", price)
                .add("side", side)
                .add("symbol", symbol)
                .add("text", text)
                .add("txTime", txTime)
                .add("execType", execType)
                .add("leavesQty", leavesQty)
                .add("marginRatio", marginRatio)
                .add("created", created)
                .add("closed", closed)
                .toString();
    }

    public String toSimpleString(){
        return "order " +
                orderId + " " +
                currency + " " +
                symbol + " " +
                price + " " +
                avgPrice + " " +
                qty + " " +
                totalQty + " " +
                commission + " " +
                status + " " +
                type + " ";
    }
}
