package ru.inheaven.aida.coin.entity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 18.08.2014 23:26
 */
public class Volume {
    private BigDecimal volume;
    private Date date = new Date();

    public Volume(BigDecimal volume) {
        this.volume = volume;
    }

    public Volume(BigDecimal volume, Date date) {
        this.volume = volume;
        this.date = date;
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
