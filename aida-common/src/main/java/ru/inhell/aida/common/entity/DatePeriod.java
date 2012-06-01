package ru.inhell.aida.common.entity;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 16:02
 */
public class DatePeriod {
    private Date start;
    private Date end;

    public DatePeriod() {
    }

    public DatePeriod(Date start, Date end) {
        this.start = start;
        this.end = end;
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
}
