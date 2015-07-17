package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author inheaven on 01.05.2015 13:23.
 */
public class Depth extends AbstractEntity {
    private ExchangeType exchangeType;
    private SymbolType symbolType;
    private String symbol;
    private Date date;
    private Date created;

    private Map<BigDecimal, BigDecimal> ask = new HashMap<>();
    private Map<BigDecimal, BigDecimal> bid = new HashMap<>();

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Map<BigDecimal, BigDecimal> getAsk() {
        return ask;
    }

    public void setAsk(Map<BigDecimal, BigDecimal> ask) {
        this.ask = ask;
    }

    public Map<BigDecimal, BigDecimal> getBid() {
        return bid;
    }

    public void setBid(Map<BigDecimal, BigDecimal> bid) {
        this.bid = bid;
    }
}
