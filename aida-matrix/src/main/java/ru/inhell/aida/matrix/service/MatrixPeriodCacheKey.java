package ru.inhell.aida.matrix.service;

import ru.inhell.aida.matrix.entity.MatrixPeriodType;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 16:16
 */
public class MatrixPeriodCacheKey {
    private String symbol;
    private Date start;
    private Date end;
    private MatrixPeriodType type;


    public MatrixPeriodCacheKey(String symbol, Date start, Date end, MatrixPeriodType type) {
        this.symbol = symbol;
        this.start = start;
        this.end = end;
        this.type = type;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public MatrixPeriodType getType() {
        return type;
    }

    public void setType(MatrixPeriodType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatrixPeriodCacheKey)) return false;

        MatrixPeriodCacheKey that = (MatrixPeriodCacheKey) o;

        if (end != null ? !end.equals(that.end) : that.end != null) return false;
        if (start != null ? !start.equals(that.start) : that.start != null) return false;
        if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;
        if (type != that.type) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = symbol != null ? symbol.hashCode() : 0;
        result = 31 * result + (start != null ? start.hashCode() : 0);
        result = 31 * result + (end != null ? end.hashCode() : 0);
        result = 31 * result + (type != null ? type.hashCode() : 0);
        return result;
    }
}
