package ru.inhell.aida.matrix.entity;

import com.google.common.collect.HashBasedTable;
import ru.inhell.aida.common.service.IProcessListener;

import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 16:21
 */
public class MatrixTable implements IProcessListener<List<Matrix>> {
    private HashBasedTable<Long, Float, MatrixCell> hashBasedTable = HashBasedTable.create();

    private float minPrice = Float.MAX_VALUE;
    private float maxPrice = Float.MIN_VALUE;

    private long maxTime = Long.MIN_VALUE;
    private long minTime = Long.MAX_VALUE;

    private MatrixControl control;
    private IMatrixLoader loader;

    public MatrixTable(MatrixControl control, IMatrixLoader loader){
        this.control = control;

        controlChanged();
    }

    public void controlChanged(){
        hashBasedTable.clear();

        long start = (control.getStart().getTime()/control.getTimeStep())* control.getTimeStep();
        long end = start + control.getColumns()*control.getTimeStep();

        add(loader.load(control.getMatrixType(), new Date(start), new Date(end)));
    }

    private long getTime(Matrix matrix){
        return  (matrix.getDate().getTime()/ control.getTimeStep())*control.getTimeStep();
    }

    private float getPrice(Matrix matrix){
        return ((long)(matrix.getPrice()*100/(control.getPriceStep()*100)))*control.getPriceStep();
    }

    public MatrixTable add(List<Matrix> matrixList){
        //set zero if updated
        for (Matrix matrix : matrixList){
            MatrixCell cell = hashBasedTable.get(getTime(matrix), getPrice(matrix));

            if (cell != null){
                cell.clear();
            }
        }

        for (Matrix matrix : matrixList){
            updateMaxMin(matrix);

            MatrixCell cell = hashBasedTable.get(getTime(matrix), getPrice(matrix));

            if (cell != null){
                switch (matrix.getTransaction()) {
                    case BUY:
                        cell.setBuyQuantity(cell.getBuyQuantity() + matrix.getQuantity());
                        break;
                    case SELL:
                        cell.setSellQuantity(cell.getSellQuantity() + matrix.getQuantity());
                        break;
                }
            }else {
                switch (matrix.getTransaction()) {
                    case BUY:
                        cell = new MatrixCell(matrix.getQuantity(), 0);
                        break;
                    case SELL:
                        cell = new MatrixCell(0, matrix.getQuantity());
                        break;
                }

                hashBasedTable.put(getTime(matrix), getPrice(matrix), cell);
            }
        }

        return this;
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

    @Override
    public void processed(List<Matrix> matrixList) {
        add(matrixList);
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

    public MatrixControl getControl() {
        return control;
    }
}
