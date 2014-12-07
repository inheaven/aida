package ru.inheaven.aida.coin.entity;

import java.math.BigDecimal;

/**
 * inheaven on 26.11.2014 5:37.
 */
public class Position {
    private BigDecimal amount;
    private BigDecimal price;

    public Position(BigDecimal amount, BigDecimal price) {
        this.amount = amount;
        this.price = price;
    }

    public Position(double amount, double price) {
        this(BigDecimal.valueOf(amount), BigDecimal.valueOf(price));
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }
}
