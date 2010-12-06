package ru.inhell.aida.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.12.10 19:04
 */
public class VectorForecastEntity {
    private Long id;
    private String contract;
    private String period;
    private Date start;
    private Date end;
    private int N;
    private int L;
    private int P;
    private int M;
    private Date created;

    public VectorForecastEntity() {
    }

    public VectorForecastEntity(String contract, String period, Date start, Date end, int n, int l, int p, int m, Date created) {
        this.id = id;
        this.contract = contract;
        this.period = period;
        this.start = start;
        this.end = end;
        N = n;
        L = l;
        P = p;
        M = m;
        this.created = created;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getContract() {
        return contract;
    }

    public void setContract(String contract) {
        this.contract = contract;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public Date getEnd() {
        return end;
    }

    public void setEnd(Date end) {
        this.end = end;
    }

    public int getN() {
        return N;
    }

    public void setN(int n) {
        N = n;
    }

    public int getL() {
        return L;
    }

    public void setL(int l) {
        L = l;
    }

    public int getP() {
        return P;
    }

    public void setP(int p) {
        P = p;
    }

    public int getM() {
        return M;
    }

    public void setM(int m) {
        M = m;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }
}
