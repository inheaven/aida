package ru.inheaven.aida.okex.model;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Date;

public class Position {
    private Long id;
    private Long accountId;
    private String currency;
    private BigDecimal avgPrice;
    private Integer qty;
    private BigDecimal price;
    private String symbol;
    private BigDecimal profit;
    private BigDecimal frozen;
    private BigDecimal marginCash;
    private String positionId;
    private String type;
    private BigDecimal eveningUp;
    private Date created;

    public int getEup() {
        return eveningUp.intValue();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public BigDecimal getAvgPrice() {
        return avgPrice;
    }

    public void setAvgPrice(BigDecimal avgPrice) {
        this.avgPrice = avgPrice;
    }

    public Integer getQty() {
        return qty;
    }

    public void setQty(Integer qty) {
        this.qty = qty;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getFrozen() {
        return frozen;
    }

    public void setFrozen(BigDecimal frozen) {
        this.frozen = frozen;
    }

    public BigDecimal getMarginCash() {
        return marginCash;
    }

    public void setMarginCash(BigDecimal marginCash) {
        this.marginCash = marginCash;
    }

    public String getPositionId() {
        return positionId;
    }

    public void setPositionId(String positionId) {
        this.positionId = positionId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getEveningUp() {
        return eveningUp;
    }

    public void setEveningUp(BigDecimal eveningUp) {
        this.eveningUp = eveningUp;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("id", id)
                .add("accountId", accountId)
                .add("currency", currency)
                .add("avgPrice", avgPrice)
                .add("qty", qty)
                .add("price", price)
                .add("symbol", symbol)
                .add("profit", profit)
                .add("frozen", frozen)
                .add("marginCash", marginCash)
                .add("positionId", positionId)
                .add("type", type)
                .add("eveningUp", eveningUp)
                .add("created", created)
                .toString();
    }

    public String toSimpleString(){
        return "position " +
                currency + " " +
                symbol + " " +
                positionId + " " +
                type + " " +
                avgPrice + " " +
                qty + " " +
                eveningUp + " " +
                marginCash + " " +
                profit + " ";
    }
}
