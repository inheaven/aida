package ru.inhell.aida.entity;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 03.01.11 14:35
 */
public class AlphaTrader {
    private Long id;
    private Long alphaOracleId;
    private Long vectorForecastId;
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

    public Long getVectorForecastId() {
        return vectorForecastId;
    }

    public void setVectorForecastId(Long vectorForecastId) {
        this.vectorForecastId = vectorForecastId;
    }

    public float getBalance() {
        return balance;
    }

    public void setBalance(float balance) {
        this.balance = balance;
    }
}
