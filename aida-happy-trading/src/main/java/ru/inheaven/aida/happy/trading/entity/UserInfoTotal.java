package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 29.07.2015 23:03.
 */
public class UserInfoTotal extends AbstractEntity{
    private Long accountId;
    private BigDecimal spotTotal;
    private BigDecimal futuresTotal;
    private BigDecimal spotVolume;
    private BigDecimal futuresVolume;
    private BigDecimal ltcPrice;
    private BigDecimal btcPrice;
    private Date created;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public BigDecimal getSpotTotal() {
        return spotTotal;
    }

    public void setSpotTotal(BigDecimal spotTotal) {
        this.spotTotal = spotTotal;
    }

    public BigDecimal getFuturesTotal() {
        return futuresTotal;
    }

    public void setFuturesTotal(BigDecimal futuresTotal) {
        this.futuresTotal = futuresTotal;
    }

    public BigDecimal getSpotVolume() {
        return spotVolume;
    }

    public void setSpotVolume(BigDecimal spotVolume) {
        this.spotVolume = spotVolume;
    }

    public BigDecimal getFuturesVolume() {
        return futuresVolume;
    }

    public void setFuturesVolume(BigDecimal futuresVolume) {
        this.futuresVolume = futuresVolume;
    }

    public BigDecimal getLtcPrice() {
        return ltcPrice;
    }

    public void setLtcPrice(BigDecimal ltcPrice) {
        this.ltcPrice = ltcPrice;
    }

    public BigDecimal getBtcPrice() {
        return btcPrice;
    }

    public void setBtcPrice(BigDecimal btcPrice) {
        this.btcPrice = btcPrice;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
