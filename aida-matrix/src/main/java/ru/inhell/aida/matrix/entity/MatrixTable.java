package ru.inhell.aida.matrix.entity;

import com.google.common.collect.HashBasedTable;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 16:21
 */
public class MatrixTable {
    private HashBasedTable<Long, Float, MatrixQuantity> hashBasedTable = HashBasedTable.create();

    private float minPrice = Float.MAX_VALUE;
    private float maxPrice = Float.MIN_VALUE;

    private long maxTime = Long.MIN_VALUE;
    private long minTime = Long.MAX_VALUE;

    private long timeStep;
    private float priceStep;

    public MatrixTable(List<Matrix> matrixList, long timeStep, float priceStep){
        this.timeStep = timeStep;
        this.priceStep = priceStep;

        populate(matrixList);
    }

    public void populate(List<Matrix> matrixList){
        if (!hashBasedTable.isEmpty()){
            hashBasedTable.clear();
        }

        for (Matrix matrix : matrixList){
            updateMaxMin(matrix);

            long time = (matrix.getDate().getTime()/ timeStep)*(timeStep + 1);
            float price = ((long)(matrix.getPrice()/priceStep))*(priceStep + 1);

            MatrixQuantity quantity = hashBasedTable.get(time, price);

            if (quantity != null){
                switch (matrix.getTransaction()) {
                    case BUY:
                        quantity.setBuyQuantity(quantity.getBuyQuantity() + matrix.getSumQuantity());
                        break;
                    case SELL:
                        quantity.setSellQuantity(quantity.getSellQuantity() + matrix.getSumQuantity());
                        break;
                }
            }else {
                switch (matrix.getTransaction()) {
                    case BUY:
                        quantity = new MatrixQuantity(matrix.getSumQuantity(), 0);
                        break;
                    case SELL:
                        quantity = new MatrixQuantity(0, matrix.getSumQuantity());
                        break;
                }

                hashBasedTable.put(time, price, quantity);
            }
        }
    }

    public static MatrixTable of(List<Matrix> matrixList, long dateStep, float priceStep){
        return new MatrixTable(matrixList, dateStep, priceStep);
    }

    public static MatrixTable of(List<Matrix> matrixList){
        return of(matrixList, 1, 1);
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

    public MatrixQuantity get(Long date, Float price) {
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
