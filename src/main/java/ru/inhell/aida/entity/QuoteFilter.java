package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.03.11 13:44
 */
public class QuoteFilter {
    private String symbol;
    private int count;
    private Date startDate;
    private Date endDate;
    private Date date;

    public QuoteFilter() {
    }

    public QuoteFilter(String symbol, Date startDate, Date endDate) {
        this.symbol = symbol;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public QuoteFilter(String symbol, int count) {
        this.symbol = symbol;
        this.count = count;
    }

    public QuoteFilter(String symbol, Date date) {
        this.symbol = symbol;
        this.date = date;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
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

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
