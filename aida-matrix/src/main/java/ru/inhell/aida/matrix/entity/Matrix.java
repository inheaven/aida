package ru.inhell.aida.matrix.entity;

import ru.inhell.aida.common.entity.AbstractEntity;
import ru.inhell.aida.common.entity.TransactionType;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.05.12 17:13
 */
public class Matrix extends AbstractEntity{
    private Date date;
    private String symbol;
    private float price;
    private int sumQuantity;
    private float sumVolume;
    private TransactionType transaction;
    private Date created;

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public int getSumQuantity() {
        return sumQuantity;
    }

    public void setSumQuantity(int sumQuantity) {
        this.sumQuantity = sumQuantity;
    }

    public float getSumVolume() {
        return sumVolume;
    }

    public void setSumVolume(float sumVolume) {
        this.sumVolume = sumVolume;
    }

    public TransactionType getTransaction() {
        return transaction;
    }

    public void setTransaction(TransactionType transaction) {
        this.transaction = transaction;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
