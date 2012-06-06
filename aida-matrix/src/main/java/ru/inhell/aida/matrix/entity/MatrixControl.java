package ru.inhell.aida.matrix.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.06.12 16:11
 */
public class MatrixControl implements Serializable {
    private String symbol;
    private Date start;
    private int columnCount;
    private int rowCount;
    private MatrixPeriodType periodType;
    private long timeStep;
    private float priceStep;

    public MatrixControl() {
    }

    public MatrixControl(String symbol, Date start, int columnCount, int rowCount, MatrixPeriodType periodType,
                         long timeStep, float priceStep) {
        this.symbol = symbol;
        this.start = start;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.periodType = periodType;
        this.timeStep = timeStep;
        this.priceStep = priceStep;
    }

    public long getZeroStart(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        switch (periodType) {
            case ONE_MINUTE:
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
            case ONE_HOUR:
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.SECOND, 0);
                calendar.set(Calendar.MILLISECOND, 0);
                break;
        }

        return calendar.getTimeInMillis();
    }

    public List<Long> getDateSeries(){
        List<Long> series = new ArrayList<>(columnCount);

        long time = getZeroStart();

        for (int i = 0; i < columnCount; ++i){
            series.add(time);

            time += timeStep;
        }

        return series;
    }

    public List<Float> getPriceSeries(float minPrice, float maxPrice){
        List<Float> series = new ArrayList<>();

        int row = (int) ((maxPrice - minPrice) / priceStep);

        float price;

        if (row < rowCount){
            price = minPrice - priceStep*(rowCount - row)/2;
        }else {
            price = maxPrice - priceStep*rowCount;
        }

        price = ((long)(price/priceStep))*priceStep;

        for (int i = 0; i < rowCount; ++i){
            series.add(price);

            price += priceStep;
        }

        return series;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public int getColumnCount() {
        return columnCount;
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public int getRowCount() {
        return rowCount;
    }

    public void setRowCount(int rowCount) {
        this.rowCount = rowCount;
    }

    public MatrixPeriodType getPeriodType() {
        return periodType;
    }

    public void setPeriodType(MatrixPeriodType periodType) {
        this.periodType = periodType;
    }

    public long getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(long timeStep) {
        this.timeStep = timeStep;
    }

    public float getPriceStep() {
        return priceStep;
    }

    public void setPriceStep(float priceStep) {
        this.priceStep = priceStep;
    }
}
