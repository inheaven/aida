package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.03.11 18:00
 */
public class VectorForecastFilter {
    private String symbol;
    private VectorForecast.INTERVAL interval;
    private int n;
    private int l;
    private int p;
    private int m;
    private Date created;

    private int first;
    private int size;

    public VectorForecastFilter() {
    }

    public VectorForecastFilter(String symbol, VectorForecast.INTERVAL interval, int n, int l, int p, int m) {
        this.symbol = symbol;
        this.interval = interval;
        this.n = n;
        this.l = l;
        this.p = p;
        this.m = m;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public VectorForecast.INTERVAL getInterval() {
        return interval;
    }

    public void setInterval(VectorForecast.INTERVAL interval) {
        this.interval = interval;
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

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getM() {
        return m;
    }

    public void setM(int m) {
        this.m = m;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public int getFirst() {
        return first;
    }

    public void setFirst(int first) {
        this.first = first;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }
}
