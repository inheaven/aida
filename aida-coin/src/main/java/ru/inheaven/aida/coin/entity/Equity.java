package ru.inheaven.aida.coin.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 20.12.2014 21:53.
 */
@Entity
public class Equity extends AbstractEntity{
    @Column @Enumerated(EnumType.STRING)
    private ExchangeType exchangeType;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal volume;

    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    public Equity() {
    }

    public Equity(BigDecimal volume) {
        this.volume = volume;
    }

    public Equity(ExchangeType exchangeType, BigDecimal volume) {
        this.exchangeType = exchangeType;
        this.volume = volume;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
