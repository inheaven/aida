package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 26.05.11 22:09
 */
public class AllTradeFilter {
    private String symbol;

    private int start;
    private int count;

    public AllTradeFilter() {
    }

    public AllTradeFilter(String symbol, int start, int count) {
        this.symbol = symbol;
        this.start = start;
        this.count = count;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getStart() {
        return start;
    }

    public void setStart(int start) {
        this.start = start;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
