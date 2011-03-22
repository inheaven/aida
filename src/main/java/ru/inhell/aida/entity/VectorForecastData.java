package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.12.10 19:06
 */
public class VectorForecastData {
    public static enum TYPE{
        MIN5, MIN10, MIN15, MIN20, MIN30,
        MAX5, MAX10, MAX15, MAX20, MAX30
    }

    private Long id;
    private Long vectorForecastId;
    private Date date;
    private int index;
    private Date indexDate;
    private float price;
    private TYPE type;

    public VectorForecastData() {
    }

    public VectorForecastData(Long vectorForecastId, Date date, int index, Date indexDate, float price) {
        this.vectorForecastId = vectorForecastId;
        this.index = index;
        this.date = date;
        this.indexDate = indexDate;
        this.price = price;
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

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Date getIndexDate() {
        return indexDate;
    }

    public void setIndexDate(Date indexDate) {
        this.indexDate = indexDate;
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

    public TYPE getType() {
        return type;
    }

    public void setType(TYPE type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "VectorForecastData{" +
                "id=" + id +
                ", vectorForecastId=" + vectorForecastId +
                ", date=" + date +
                ", index=" + index +
                ", indexDate=" + indexDate +
                ", price=" + price +
                ", type=" + type +
                '}';
    }
}
