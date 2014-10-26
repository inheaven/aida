package ru.inheaven.aida.coin.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 26.10.2014 21:02
 */
@Entity
@Table(name = "ticker_history")
public class TickerHistory extends AbstractEntity{
    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String pair;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal volume;

    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    public TickerHistory() {
    }

    public TickerHistory(ExchangeType exchangeType, String pair, BigDecimal price, BigDecimal volume) {
        this.exchangeType = exchangeType;
        this.pair = pair;
        this.price = price;
        this.volume = volume;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
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
