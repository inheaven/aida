package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 29.04.11 23:26
 */
public class AlphaOracleScore {
    private Long id;
    private Long alphaOracleId;
    private Date day;
    private float score;
    private float min;
    private float max;

    public AlphaOracleScore() {
    }

    public AlphaOracleScore(Long alphaOracleId, Date day, float score, float min, float max) {
        this.alphaOracleId = alphaOracleId;
        this.day = day;
        this.score = score;
        this.min = min;
        this.max = max;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlphaOracleId() {
        return alphaOracleId;
    }

    public void setAlphaOracleId(Long alphaOracleId) {
        this.alphaOracleId = alphaOracleId;
    }

    public Date getDay() {
        return day;
    }

    public void setDay(Date day) {
        this.day = day;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getMin() {
        return min;
    }

    public void setMin(float min) {
        this.min = min;
    }

    public float getMax() {
        return max;
    }

    public void setMax(float max) {
        this.max = max;
    }
}
