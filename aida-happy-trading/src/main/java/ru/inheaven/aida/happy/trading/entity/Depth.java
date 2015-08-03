package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 01.05.2015 13:23.
 */
public class Depth extends AbstractEntity {
    private ExchangeType exchangeType;
    private String symbol;
    private SymbolType symbolType;
    private BigDecimal bid;
    private BigDecimal ask;
    private String bidJson;
    private String askJson;
    private Date time;
    private Date created;

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public BigDecimal getBid() {
        return bid;
    }

    public void setBid(BigDecimal bid) {
        this.bid = bid;
    }

    public BigDecimal getAsk() {
        return ask;
    }

    public void setAsk(BigDecimal ask) {
        this.ask = ask;
    }

    public String getBidJson() {
        return bidJson;
    }

    public void setBidJson(String bidJson) {
        this.bidJson = bidJson;
    }

    public String getAskJson() {
        return askJson;
    }

    public void setAskJson(String askJson) {
        this.askJson = askJson;
    }

    public Date getTime() {
        return time;
    }

    public void setTime(Date time) {
        this.time = time;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
