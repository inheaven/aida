package ru.inheaven.aida.coin.entity;

import ru.inheaven.aida.coin.util.TraderUtil;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 07.01.14 19:41
 */
@Entity
public class Trader extends AbstractEntity{
    @Column(nullable = false)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String pair;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal high;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal low;

    @Column(nullable = false, precision = 19,  scale = 8)
    private BigDecimal volume = BigDecimal.ZERO;

    @Column(nullable = false, precision = 19,  scale = 8)
    private BigDecimal spread = BigDecimal.ZERO;

    @Column(nullable = true, precision = 19,  scale = 8)
    private BigDecimal lot;

    @Column(nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column
    private boolean running = false;

    @Column
    private boolean predicting = false;

    @Column(nullable = true)
    private TraderType type = TraderType.LONG;

    @Transient
    private BigDecimal weekProfit;

    @PrePersist
    @PreUpdate
    protected void preUpdate(){
        date = new Date();
    }

    public boolean isFuture(){
        return ExchangeType.OKCOIN_FUTURES.equals(exchangeType);
    }

    public ExchangePair getExchangePair(){
        return new ExchangePair(exchangeType, pair, type);
    }

    public String getCurrency(){
        return TraderUtil.getCurrency(pair);
    }

    public String getCounterSymbol(){
        return TraderUtil.getCounterSymbol(pair);
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchange) {
        this.exchangeType = exchange;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
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

    public BigDecimal getSpread() {
        return spread;
    }

    public void setSpread(BigDecimal spread) {
        this.spread = spread;
    }

    public BigDecimal getLot() {
        return lot;
    }

    public void setLot(BigDecimal lot) {
        this.lot = lot;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public boolean isRunning() {
        return running;
    }

    public void setRunning(boolean running) {
        this.running = running;
    }

    public TraderType getType() {
        return type;
    }

    public void setType(TraderType traderType) {
        this.type = traderType;
    }

    public BigDecimal getWeekProfit() {
        return weekProfit;
    }

    public void setWeekProfit(BigDecimal weekProfit) {
        this.weekProfit = weekProfit;
    }

    public boolean isPredicting() {
        return predicting;
    }

    public void setPredicting(boolean prediction) {
        this.predicting = prediction;
    }
}
