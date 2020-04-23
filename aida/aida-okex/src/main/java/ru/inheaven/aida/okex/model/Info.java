package ru.inheaven.aida.okex.model;

import com.google.common.base.MoreObjects;

import java.math.BigDecimal;
import java.util.Date;

public class Info {
    private Long id;
    private Long accountId;
    private String currency;
    private BigDecimal balance;
    private BigDecimal profit;
    private BigDecimal unrealized;
    private BigDecimal margin;
    private BigDecimal risk;
    private Date created;

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

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getProfit() {
        return profit;
    }

    public void setProfit(BigDecimal profit) {
        this.profit = profit;
    }

    public BigDecimal getUnrealized() {
        return unrealized;
    }

    public void setUnrealized(BigDecimal unrealized) {
        this.unrealized = unrealized;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public BigDecimal getRisk() {
        return risk;
    }

    public void setRisk(BigDecimal risk) {
        this.risk = risk;
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
                .add("balance", balance)
                .add("profit", profit)
                .add("margin", margin)
                .add("created", created)
                .toString();
    }

    public String toSimpleString(){
        return "info " +
                currency + " " +
                balance + " " +
                margin + " " +
                profit + " ";
    }
}
