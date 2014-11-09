package com.xeiam.xchange.dto.trade;

import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 09.11.2014 15:57
 */
public class LimitOrderStatus {
    private final String orderId;
    private final Order.OrderType type;
    private final CurrencyPair currencyPair;
    private final BigDecimal price;
    private final BigDecimal tradableAmount;
    private final BigDecimal filledAmount;
    private final Date timestamp;
    private final boolean isOpen;
    private final boolean isCanceled;

    public LimitOrderStatus(String orderId, Order.OrderType type, CurrencyPair currencyPair, BigDecimal price,
                            BigDecimal tradableAmount, BigDecimal filledAmount, Date timestamp, boolean isOpen, boolean isCanceled) {
        this.orderId = orderId;
        this.type = type;
        this.currencyPair = currencyPair;
        this.price = price;
        this.tradableAmount = tradableAmount;
        this.filledAmount = filledAmount;
        this.timestamp = timestamp;
        this.isOpen = isOpen;
        this.isCanceled = isCanceled;
    }

    public String getOrderId() {
        return orderId;
    }

    public Order.OrderType getType() {
        return type;
    }

    public CurrencyPair getCurrencyPair() {
        return currencyPair;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public BigDecimal getTradableAmount() {
        return tradableAmount;
    }

    public BigDecimal getFilledAmount() {
        return filledAmount;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public boolean isOpen() {
        return isOpen;
    }

    public boolean isCanceled() {
        return isCanceled;
    }
}
