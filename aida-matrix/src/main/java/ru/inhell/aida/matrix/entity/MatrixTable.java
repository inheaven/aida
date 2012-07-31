package ru.inhell.aida.matrix.entity;

import com.google.common.collect.HashBasedTable;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 16:21
 */
public class MatrixTable {
    private HashBasedTable<Long, Float, MatrixCell> hashBasedTable = HashBasedTable.create();

    private float minPrice = Float.MAX_VALUE;
    private float maxPrice = Float.MIN_VALUE;

    private long maxTime = Long.MIN_VALUE;
    private long minTime = Long.MAX_VALUE;

    private long timeStep;
    private float priceStep;

    public MatrixTable(long timeStep, float priceStep){
        this.timeStep = timeStep;
        this.priceStep = priceStep;
    }

    public MatrixTable add(List<Matrix> matrixList){
        for (Matrix matrix : matrixList){
            updateMaxMin(matrix);

            long time = (matrix.getDate().getTime()/ timeStep)*timeStep;
            float price = ((long)(matrix.getPrice()*100/(priceStep*100)))*priceStep;

            MatrixCell cell = hashBasedTable.get(time, price);

            if (cell != null){
                switch (matrix.getTransaction()) {
                    case BUY:
                        cell.setBuyQuantity(cell.getBuyQuantity() + matrix.getSumQuantity());
                        break;
                    case SELL:
                        cell.setSellQuantity(cell.getSellQuantity() + matrix.getSumQuantity());
                        break;
                }
            }else {
                switch (matrix.getTransaction()) {
                    case BUY:
                        cell = new MatrixCell(matrix.getSumQuantity(), 0);
                        break;
                    case SELL:
                        cell = new MatrixCell(0, matrix.getSumQuantity());
                        break;
                }

                hashBasedTable.put(time, price, cell);
            }
        }

        return this;
    }

    public static MatrixTable of(List<Matrix> matrixList, long timeStep, float priceStep){
        return new MatrixTable(timeStep, priceStep).add(matrixList);
    }

    private void updateMaxMin(Matrix matrix){
        float price = matrix.getPrice();
        if (price > maxPrice){
            maxPrice = price;
        }
        if (price < minPrice){
            minPrice = price;
        }

        long time = matrix.getDate().getTime();
        if (time > maxTime){
            maxTime = time;
        }
        if (time < minTime){
            minTime = time;
        }
    }

    public MatrixCell get(Long date, Float price) {
        return hashBasedTable.get(date, price);
    }

    public float getMinPrice() {
        return minPrice;
    }

    public float getMaxPrice() {
        return maxPrice;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public long getMinTime() {
        return minTime;
    }

    public long getTimeStep() {
        return timeStep;
    }

    public float getPriceStep() {
        return priceStep;
    }
}
