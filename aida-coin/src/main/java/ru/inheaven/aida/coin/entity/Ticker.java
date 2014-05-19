package ru.inheaven.aida.coin.entity;

import javax.xml.bind.annotation.XmlRootElement;
import java.math.BigDecimal;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 17:46
 *
 *  Ticker
 *  GET https://cex.io/api/ticker/GHS/BTC
 *  Returns JSON dictionary:
 *
 *  last - last BTC price
 *  high - last 24 hours price high
 *  low - last 24 hours price low
 *  volume - last 24 hours volume
 *  bid - highest buy order
 *  ask - lowest sell order
 */
@XmlRootElement
public class Ticker {
    private BigDecimal last;
    private BigDecimal high;
    private BigDecimal low;
    private BigDecimal volume;
    private BigDecimal bid;
    private BigDecimal ask;

    public BigDecimal getLast() {
        return last;
    }

    public void setLast(BigDecimal last) {
        this.last = last;
    }

    public BigDecimal getHigh() {
        return high;
    }

    public void setHigh(BigDecimal high) {
        this.high = high;
    }

    public BigDecimal getLow() {
        return low;
    }

    public void setLow(BigDecimal low) {
        this.low = low;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
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
}
