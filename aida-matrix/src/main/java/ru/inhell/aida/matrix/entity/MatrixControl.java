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
    private MatrixType matrixType;
    private Date start;
    private int columns;
    private int rows;
    private long timeStep;
    private float priceStep;

    public MatrixControl() {
    }

    public MatrixControl(String symbol, Date start, int columns, int rows, MatrixPeriodType periodType,
                         long timeStep, float priceStep) {
        matrixType = new MatrixType(symbol, periodType);
        this.start = start;
        this.columns = columns;
        this.rows = rows;
        this.timeStep = timeStep;
        this.priceStep = priceStep;
    }

    public long getZeroStart(){
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(start);

        switch (matrixType.getPeriodType()) {
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
        List<Long> series = new ArrayList<>(columns);

        long time = getZeroStart();

        for (int i = 0; i < columns; ++i){
            series.add(time);

            time += timeStep;
        }

        return series;
    }

    public List<Float> getPriceSeries(float minPrice, float maxPrice){
        List<Float> series = new ArrayList<>();

        int row = (int) ((maxPrice - minPrice) / priceStep);

        float price;

        if (row < rows){
            price = minPrice - priceStep*(rows - row)/2;
        }else {
            price = maxPrice - priceStep* rows;
        }

        price = ((long)(price/priceStep))*priceStep + priceStep* rows;

        for (int i = 0; i < rows; ++i){
            series.add(price);

            price -= priceStep;
        }

        return series;
    }

    public MatrixType getMatrixType() {
        return matrixType;
    }

    public void setMatrixType(MatrixType matrixType) {
        this.matrixType = matrixType;
    }

    public Date getStart() {
        return start;
    }

    public void setStart(Date start) {
        this.start = start;
    }

    public int getColumns() {
        return columns;
    }

    public void setColumns(int columns) {
        this.columns = columns;
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        this.rows = rows;
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
