package ru.inhell.aida.matrix.entity;

import java.io.Serializable;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 30.07.12 16:36
 */
public class MatrixType implements Serializable {
    private String symbol;
    private MatrixPeriodType periodType;

    public MatrixType() {
    }

    public MatrixType(String symbol, MatrixPeriodType periodType) {
        this.symbol = symbol;
        this.periodType = periodType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public MatrixPeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(MatrixPeriodType periodType) {
        this.periodType = periodType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MatrixType)) return false;

        MatrixType that = (MatrixType) o;

        if (periodType != that.periodType) return false;
        if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = symbol != null ? symbol.hashCode() : 0;
        result = 31 * result + (periodType != null ? periodType.hashCode() : 0);
        return result;
    }
}
