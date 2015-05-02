package ru.inheaven.aida.coin.entity;

/**
 * @author inheaven on 13.02.2015 2:22.
 */
public enum OrderType {
    BID, ASK, OPEN_LONG(1), OPEN_SHORT(2), CLOSE_LONG(3), CLOSE_SHORT(4);

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
