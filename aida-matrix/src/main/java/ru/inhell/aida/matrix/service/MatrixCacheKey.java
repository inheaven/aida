package ru.inhell.aida.matrix.service;

import ru.inhell.aida.common.entity.TransactionType;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 16:28
 */
public class MatrixCacheKey {
    private String symbol;
    private Date date;
    private TransactionType type;

    public MatrixCacheKey() {
    }

    public MatrixCacheKey(String symbol, Date date, TransactionType type) {
        this.symbol = symbol;
        this.date = date;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatrixCacheKey)) return false;

        MatrixCacheKey that = (MatrixCacheKey) o;

        if (date != null ? !date.equals(that.date) : that.date != null) return false;
        if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = symbol != null ? symbol.hashCode() : 0;
        result = 31 * result + (date != null ? date.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
