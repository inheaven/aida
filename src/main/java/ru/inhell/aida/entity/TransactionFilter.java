package ru.inhell.aida.entity;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 01.06.11 21:57
 */
public class TransactionFilter {
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    private String symbol;
    private String date;

    public TransactionFilter() {
    }

    public TransactionFilter(String symbol, String date) {
        this.symbol = symbol;
        this.date = date;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
