package ru.inhell.aida.entity;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 03.01.11 14:35
 */
public class AlphaTrader {
    private Long id;
    private Long alphaOracleId;
    private String symbol;
    private String futureSymbol;
    private float price;
    private int quantity;
    private int orderQuantity;
    private float balance;

    public void addBalance(float balance){
        this.balance += balance;
    }

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

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
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

}
