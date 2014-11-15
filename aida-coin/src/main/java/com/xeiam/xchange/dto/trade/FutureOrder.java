package com.xeiam.xchange.dto.trade;

import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order;

import java.math.BigDecimal;
import java.util.Date;

/**
 * inheaven on 16.11.2014 0:02.
 */
public class FutureOrder extends Order {
    private final BigDecimal price;
    private final FutureOrderType futureOrderType;

    public FutureOrder(OrderType type, BigDecimal tradableAmount, CurrencyPair currencyPair, String id, Date timestamp,
                       BigDecimal price, FutureOrderType futureOrderType) {
        super(type, tradableAmount, currencyPair, id, timestamp);
        this.price = price;
        this.futureOrderType = futureOrderType;
    }
}
