package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.03.11 16:43
 */
public class AlphaOracleData {
    public static enum PREDICTION {
        LONG, SHORT, STOP_BUY, STOP_SELL
    }

    private Long id;
    private Long alphaOracleId;
    private Date date;
    private float price;
    private PREDICTION prediction;
    private Date predicted;

    public AlphaOracleData() {
    }

    public AlphaOracleData(Long alphaOracleId, Date date, float price, PREDICTION prediction, Date predicted) {
        this.alphaOracleId = alphaOracleId;
        this.date = date;
        this.price = price;
        this.prediction = prediction;
        this.predicted = predicted;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public PREDICTION getPrediction() {
        return prediction;
    }

    public void setPrediction(PREDICTION prediction) {
        this.prediction = prediction;
    }

    public Date getPredicted() {
        return predicted;
    }

    public void setPredicted(Date predicted) {
        this.predicted = predicted;
    }
}
