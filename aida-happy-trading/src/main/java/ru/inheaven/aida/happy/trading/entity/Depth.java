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

    private Map<BigDecimal, BigDecimal> askMap = new HashMap<>();
    private Map<BigDecimal, BigDecimal> bidMap = new HashMap<>();

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

    public Map<BigDecimal, BigDecimal> getAskMap() {
        return askMap;
    }

    public void setAskMap(Map<BigDecimal, BigDecimal> askMap) {
        this.askMap = askMap;
    }

    public Map<BigDecimal, BigDecimal> getBidMap() {
        return bidMap;
    }

    public void setBidMap(Map<BigDecimal, BigDecimal> bidMap) {
        this.bidMap = bidMap;
    }
}
