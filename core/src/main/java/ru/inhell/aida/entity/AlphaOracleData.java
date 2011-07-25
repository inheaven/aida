package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.03.11 16:43
 */
public class AlphaOracleData {
    private Long id;
    private Long alphaOracleId;
    private Date date;
    private float price;
    private Prediction prediction;

    public AlphaOracleData() {
    }

    public AlphaOracleData(Long alphaOracleId, Date date, float price, Prediction prediction) {
        this.alphaOracleId = alphaOracleId;
        this.date = date;
        this.price = price;
        this.prediction = prediction;
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

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }
}
