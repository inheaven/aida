package ru.inheaven.aida.coin.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 022 22.08.14 13:52
 */
public class OrderVolume {
    private BigDecimal volume = BigDecimal.ZERO;
    private BigDecimal askVolume = BigDecimal.ZERO;
    private BigDecimal bidVolume = BigDecimal.ZERO;

    private Date date;

    public OrderVolume(Date date) {
        this.date = date;
    }

    public void addVolume(BigDecimal v){
        volume = volume.add(v);
    }

    public void addAskVolume(BigDecimal a){
        askVolume = askVolume.add(a);
    }

    public void addBidVolume(BigDecimal b){
        bidVolume = bidVolume.add(b);
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public BigDecimal getAskVolume() {
        return askVolume;
    }

    public void setAskVolume(BigDecimal askVolume) {
        this.askVolume = askVolume;
    }

    public BigDecimal getBidVolume() {
        return bidVolume;
    }

    public void setBidVolume(BigDecimal bidVolume) {
        this.bidVolume = bidVolume;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
