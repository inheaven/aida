package ru.inheaven.aida.happy.trading.entity;

import java.math.BigDecimal;

/**
 * @author inheaven on 04.09.2015 4:35.
 */
public class PriceAmount {
    private final BigDecimal price;
    private final BigDecimal amount;

    public PriceAmount(BigDecimal price, BigDecimal amount) {
        this.price = price;
        this.amount = amount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    @Override
    public String toString() {
        return "[" + price + "," + amount + "]";
    }
}
