package ru.inheaven.aida.cexio.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 07.01.14 19:41
 */
@Entity
public class Trader extends AbstractEntity{
    @Column(nullable = false)
    private String marker;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal high;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal low;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal open;

    @Column(nullable = false, precision = 19,  scale = 8)
    private BigDecimal volume;

    @Column(nullable = false, precision = 19,  scale = 8)
    private BigDecimal spread;

    @Column(nullable = false)
    private Date date;

    public String getMarker() {
        return marker;
    }

    public void setMarker(String marker) {
        this.marker = marker;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public BigDecimal getOpen() {
        return open;
    }

    public void setOpen(BigDecimal open) {
        this.open = open;
    }

    public BigDecimal getVolume() {
        return volume;
    }

    public void setVolume(BigDecimal volume) {
        this.volume = volume;
    }

    public BigDecimal getSpread() {
        return spread;
    }

    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
