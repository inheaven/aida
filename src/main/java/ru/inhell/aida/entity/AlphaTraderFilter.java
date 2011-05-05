package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.04.11 15:11
 */
public class AlphaTraderFilter {
    private Long alphaTraderId;
    private Date startDate;
    private Date endDate;

    public AlphaTraderFilter() {
    }

    public AlphaTraderFilter(Long alphaTraderId, Date startDate, Date endDate) {
        this.alphaTraderId = alphaTraderId;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public Long getAlphaTraderId() {
        return alphaTraderId;
    }

    public void setAlphaTraderId(Long alphaTraderId) {
        this.alphaTraderId = alphaTraderId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
