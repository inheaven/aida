package ru.inheaven.aida.okex.model;

import java.math.BigDecimal;

public class Depth {
    private String currency;
    private String symbol;
    private BigDecimal bid;
    private BigDecimal ask;

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    @Override
    public String toString() {
        return "Depth{" +
                "currency='" + currency + '\'' +
                ", symbol='" + symbol + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                '}';
    }
}
