package ru.inheaven.aida.coin.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * inheaven on 26.11.2014 5:35.
 */
public class Futures {
    private List<Position> margins = new ArrayList<>();
    private List<Position> bids = new ArrayList<>();
    private List<Position> asks = new ArrayList<>();
    private List<Position> equity = new ArrayList<>();

    private BigDecimal margin = BigDecimal.ZERO;
    private BigDecimal realProfit = BigDecimal.ZERO;
    private BigDecimal avgPosition = BigDecimal.ZERO;

    public List<Position> getMargins() {
        return margins;
    }

    public void setMargins(List<Position> margins) {
        this.margins = margins;
    }

    public List<Position> getBids() {
        return bids;
    }

    public void setBids(List<Position> bids) {
        this.bids = bids;
    }

    public List<Position> getAsks() {
        return asks;
    }

    public void setAsks(List<Position> asks) {
        this.asks = asks;
    }

    public List<Position> getEquity() {
        return equity;
    }

    public void setEquity(List<Position> equity) {
        this.equity = equity;
    }

    public BigDecimal getMargin() {
        return margin;
    }

    public void setMargin(BigDecimal margin) {
        this.margin = margin;
    }

    public BigDecimal getRealProfit() {
        return realProfit;
    }

    public void setRealProfit(BigDecimal realProfit) {
        this.realProfit = realProfit;
    }

    public BigDecimal getAvgPosition() {
        return avgPosition;
    }

    public void setAvgPosition(BigDecimal avgPosition) {
        this.avgPosition = avgPosition;
    }
}
