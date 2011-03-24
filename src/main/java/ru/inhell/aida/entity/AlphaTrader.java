package ru.inhell.aida.entity;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 03.01.11 14:35
 */
public class AlphaTrader {
    private Long id;
    private Long alphaOracleId;
    private String symbol;
    private int quantity;
    private float balance;

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

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }
}
