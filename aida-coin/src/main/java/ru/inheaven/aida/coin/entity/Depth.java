package ru.inheaven.aida.coin.entity;

import javax.persistence.*;
import java.util.Date;

/**
 * @author inheaven on 01.05.2015 13:23.
 */
@Entity
public class Depth extends AbstractEntity {
    @Column(nullable = true)
    @Enumerated(EnumType.STRING)
    private SymbolType symbolType;

    @Column(nullable = false)
    private String symbol;

    @Lob
    @Column(nullable = false)
    private String data;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date created = new Date();

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

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
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
}
