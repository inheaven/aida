package ru.inhell.aida.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.03.11 13:44
 */
public class QuoteFilter {
    private String symbol;
    private int count;

    public QuoteFilter(String symbol, int count) {
        this.symbol = symbol;
        this.count = count;
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
}
