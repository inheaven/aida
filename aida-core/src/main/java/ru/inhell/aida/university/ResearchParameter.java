package ru.inhell.aida.university;

import ru.inhell.aida.entity.Interval;
import ru.inhell.aida.entity.PriceType;
import ru.inhell.aida.entity.StopType;
import ru.inhell.aida.entity.SvdType;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.08.11 18:40
 */
public class ResearchParameter {
    private String symbolForecast;
    private String symbolTrade;
    private int n;
    private int l;
    private int p;
    private int m;
    private PriceType priceType;
    private StopType stopType;
    private float stopFactor;
    private Interval interval;
    private boolean anti = false;
    private int delta;
    private boolean useMA = false;
    private int ma;
    private int t;
    private SvdType svdType;

    private Date startDate;
    private Date endDate;

    public String getSymbolForecast() {
        return symbolForecast;
    }

    public void setSymbolForecast(String symbolForecast) {
        this.symbolForecast = symbolForecast;
    }

    public String getSymbolTrade() {
        return symbolTrade;
    }

    public void setSymbolTrade(String symbolTrade) {
        this.symbolTrade = symbolTrade;
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

    public PriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(PriceType priceType) {
        this.priceType = priceType;
    }

    public StopType getStopType() {
        return stopType;
    }

    public void setStopType(StopType stopType) {
        this.stopType = stopType;
    }

    public float getStopFactor() {
        return stopFactor;
    }

    public void setStopFactor(float stopFactor) {
        this.stopFactor = stopFactor;
    }

    public Interval getInterval() {
        return interval;
    }

    public void setInterval(Interval interval) {
        this.interval = interval;
    }

    public boolean isAnti() {
        return anti;
    }

    public void setAnti(boolean anti) {
        this.anti = anti;
    }

    public int getDelta() {
        return delta;
    }

    public void setDelta(int delta) {
        this.delta = delta;
    }

    public boolean isUseMA() {
        return useMA;
    }

    public void setUseMA(boolean useMA) {
        this.useMA = useMA;
    }

    public int getMa() {
        return ma;
    }

    public void setMa(int ma) {
        this.ma = ma;
    }

    public int getT() {
        return t;
    }

    public void setT(int t) {
        this.t = t;
    }

    public SvdType getSvdType() {
        return svdType;
    }

    public void setSvdType(SvdType svdType) {
        this.svdType = svdType;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
