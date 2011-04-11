package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.03.11 18:21
 */
public class AlphaOracle {
    public static enum PRICE_TYPE{AVERAGE, CLOSE}

    private Long id;
    private VectorForecast vectorForecast;
    private PRICE_TYPE priceType;
    private boolean enable = true;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VectorForecast getVectorForecast() {
        return vectorForecast;
    }

    public void setVectorForecast(VectorForecast vectorForecast) {
        this.vectorForecast = vectorForecast;
    }

    public PRICE_TYPE getPriceType() {
        return priceType;
    }

    public void setPriceType(PRICE_TYPE priceType) {
        this.priceType = priceType;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
