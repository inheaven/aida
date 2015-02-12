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
    @Column(nullable = false)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String pair;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(nullable = true, precision = 19, scale = 8)
    private BigDecimal bid;

    @Column(nullable = true, precision = 19, scale = 8)
    private BigDecimal ask;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal volume;

    @Column(nullable = true, precision = 19, scale = 8)
    private BigDecimal volatility;

    @Column(nullable = true, precision = 19, scale = 8)
    private BigDecimal prediction;

    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    public TickerHistory() {
    }

    public TickerHistory(ExchangeType exchangeType, String pair, BigDecimal price, BigDecimal bid, BigDecimal ask,
                         BigDecimal volume, BigDecimal volatility, BigDecimal prediction) {
        this.exchangeType = exchangeType;
        this.pair = pair;
        this.price = price;
        this.bid = bid;
        this.ask = ask;
        this.volume = volume;
        this.volatility = volatility;
        this.prediction = prediction;
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

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public BigDecimal getVolatility() {
        return volatility;
    }

    public void setVolatility(BigDecimal volatility) {
        this.volatility = volatility;
    }

    public BigDecimal getPrediction() {
        return prediction;
    }

    public void setPrediction(BigDecimal prediction) {
        this.prediction = prediction;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
