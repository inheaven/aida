package ru.inhell.aida.entity;

import java.lang.reflect.Type;
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

    private int n;
    private int l;

    private Date now;
    private int index;
    private Date date;
    private float price;

    private TYPE type;

    @Deprecated
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

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public int getL() {
        return l;
    }

    public void setL(int l) {
        this.l = l;
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
                ", n=" + n +
                ", l=" + l +
                ", now=" + now +
                ", index=" + index +
                ", date=" + date +
                ", price=" + price +
                ", type=" + type +
                ", close=" + close +
                '}';
    }
}
