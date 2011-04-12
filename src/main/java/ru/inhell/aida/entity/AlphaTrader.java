package ru.inhell.aida.entity;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 03.01.11 14:35
 */
public class AlphaTrader {
    public static enum STOP_TYPE{NONE, M_STOP}

    private Long id;
    private Long alphaOracleId;
    private String symbol;
    private String futureSymbol;
    private int quantity;
    private int orderQuantity;
    private float stopPrice;
    private float balance;
    private STOP_TYPE stopType;
    private int stopCount;
    private float stopFactor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlphaOracleId() {
        return alphaOracleId;
    }

    public void setAlphaOracleId(Long alphaOracleId) {
        this.alphaOracleId = alphaOracleId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getFutureSymbol() {
        return futureSymbol;
    }

    public void setFutureSymbol(String futureSymbol) {
        this.futureSymbol = futureSymbol;
    }

    public float getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(float stopPrice) {
        this.stopPrice = stopPrice;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public int getOrderQuantity() {
        return orderQuantity;
    }

    public void setOrderQuantity(int orderQuantity) {
        this.orderQuantity = orderQuantity;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }

    public STOP_TYPE getStopType() {
        return stopType;
    }

    public void setStopType(STOP_TYPE stopType) {
        this.stopType = stopType;
    }

    public int getStopCount() {
        return stopCount;
    }

    public void setStopCount(int stopCount) {
        this.stopCount = stopCount;
    }

    public float getStopFactor() {
        return stopFactor;
    }

    public void setStopFactor(float stopFactor) {
        this.stopFactor = stopFactor;
    }
}
