package ru.inheaven.aida.coin.entity;

import java.util.EnumSet;

/**
 * @author inheaven on 13.02.2015 2:22.
 */
public enum OrderType {
    BID, ASK, OPEN_LONG(1), OPEN_SHORT(2), CLOSE_LONG(3), CLOSE_SHORT(4);

    public final static EnumSet<OrderType> SELL = EnumSet.of(ASK, OPEN_SHORT, CLOSE_LONG);
    public final static EnumSet<OrderType> BUY = EnumSet.of(BID, OPEN_LONG, CLOSE_SHORT);

    private int code;

    OrderType() {
    }

    OrderType(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}

