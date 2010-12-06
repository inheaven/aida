package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.12.10 19:06
 */
public class VectorForecastData {
    private Long id;
    private Long vectorForecastId;
    private Date now;
    private int index;
    private Date date;
    private float close;

    public VectorForecastData() {
    }

    public VectorForecastData(Long vectorForecastId, Date now, int index, Date date, float close) {
        this.vectorForecastId = vectorForecastId;
        this.now = now;
        this.index = index;
        this.date = date;
        this.close = close;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getVectorForecastId() {
        return vectorForecastId;
    }

    public void setVectorForecastId(Long vectorForecastId) {
        this.vectorForecastId = vectorForecastId;
    }

    public Date getNow() {
        return now;
    }

    public void setNow(Date now) {
        this.now = now;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getClose() {
        return close;
    }

    public void setClose(float close) {
        this.close = close;
    }
}
