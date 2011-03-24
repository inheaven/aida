package ru.inhell.aida.entity;

import java.util.Date;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 03.01.11 14:39
 */
public class AlphaTraderData {
    public static enum ORDER{BUY, SELL}

    private Long id;
    private Long alphaTraderId;
    private Date date;
    private float price;
    private ORDER order;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlphaTraderId() {
        return alphaTraderId;
    }

    public void setAlphaTraderId(Long alphaTraderId) {
        this.alphaTraderId = alphaTraderId;
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

    public ORDER getOrder() {
        return order;
    }

    public void setOrder(ORDER order) {
        this.order = order;
    }
}
