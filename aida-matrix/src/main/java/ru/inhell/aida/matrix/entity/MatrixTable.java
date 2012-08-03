package ru.inhell.aida.matrix.entity;

import com.google.common.collect.TreeBasedTable;
import ru.inhell.aida.common.service.IProcessListener;

import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 16:21
 */
public class MatrixTable implements IProcessListener<List<Matrix>> {
    private TreeBasedTable<Float, Long, MatrixCell> table = TreeBasedTable.create();

    private float minPrice;
    private float maxPrice;

    private long maxTime;
    private long minTime;

    private MatrixControl control;
    private IMatrixLoader loader;

    public MatrixTable(MatrixControl control, IMatrixLoader loader){
        this.control = control;
        this.loader = loader;

        controlChanged();
    }

    public void controlChanged(){
        table.clear();

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

    private void crop(){
        //columns
        int deltaColumn = table.columnKeySet().size() - control.getColumns();

        if (deltaColumn > 0){
            Iterator<Long> iterator = table.columnKeySet().iterator();

            for (int i=0; i < deltaColumn; ++i){
                table.columnKeySet().remove(iterator.next());
            }
        }

        //rows
        int deltaRows = table.rowKeySet().size() - control.getRows();

        if (deltaRows > 0){
            Float last = table.rowKeySet().last();



        }
    }

    public MatrixTable add(List<Matrix> matrixList){
        //group matrix by cell
        for (Matrix matrix : matrixList){
            MatrixCell cell = table.get(getPrice(matrix), getTime(matrix));

            if (cell == null){
                cell = new MatrixCell();
                table.put(getPrice(matrix), getTime(matrix), cell);
            }

            cell.add(matrix);
        }

        //crop
        crop();

        //Max and Min
        updateMaxMin();

        return this;
    }

    private void updateMaxMin(){
        minPrice = Float.MAX_VALUE;
        maxPrice = Float.MIN_VALUE;

        maxTime = Long.MIN_VALUE;
        minTime = Long.MAX_VALUE;


        for (MatrixCell cell : table.values()){
            for (Matrix matrix : cell.values()){
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
        }
    }

    public MatrixCell get(Long date, Float price) {
        return table.get(date, price);
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
