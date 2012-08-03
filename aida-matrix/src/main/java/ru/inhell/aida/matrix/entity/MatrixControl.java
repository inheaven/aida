package ru.inhell.aida.matrix.entity;

import java.io.Serializable;
import java.util.Date;

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

    public MatrixControl(String symbol, Date start, int rows, int columns, MatrixPeriodType periodType,
                         long timeStep, float priceStep) {
        matrixType = new MatrixType(symbol, periodType);
        this.start = start;
        this.columns = columns;
        this.rows = rows;
        this.timeStep = timeStep;
        this.priceStep = priceStep;
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
