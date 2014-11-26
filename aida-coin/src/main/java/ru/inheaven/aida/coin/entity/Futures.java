package ru.inheaven.aida.coin.entity;

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
}
