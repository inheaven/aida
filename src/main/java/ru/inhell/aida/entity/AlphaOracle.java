package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.03.11 18:21
 */
public class AlphaOracle {
    private Long id;
    private VectorForecast vectorForecast;
    private Date created;
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

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public boolean isEnable() {
        return enable;
    }

    public void setEnable(boolean enable) {
        this.enable = enable;
    }
}
