package ru.inhell.aida.matrix.entity;

import com.google.common.collect.TreeBasedTable;
import ru.inhell.aida.common.service.IProcessListener;

import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 16:21
 */
public class MatrixTable implements IProcessListener<List<Matrix>> {
    private TreeBasedTable<Long, Float, MatrixCell> table = TreeBasedTable.create();

    private float minPrice = Float.MAX_VALUE;
    private float maxPrice = Float.MIN_VALUE;

    private long maxTime = Long.MIN_VALUE;
    private long minTime = Long.MAX_VALUE;

    private MatrixControl control;
    private IMatrixLoader loader;

    private int tableColumns;
    private int tableRows;

    private int tableColumnsDelta;
    private int tableRowsDelta;

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

    public MatrixTable add(List<Matrix> matrixList){
        //clear cell if updated
        for (Matrix matrix : matrixList){
            MatrixCell cell = table.get(getTime(matrix), getPrice(matrix));

            if (cell != null){
                cell.clear();
            }
        }

        //group matrix by cell
        for (Matrix matrix : matrixList){
            updateMaxMin(matrix);

            MatrixCell cell = table.get(getTime(matrix), getPrice(matrix));

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

                table.put(getTime(matrix), getPrice(matrix), cell);
            }
        }

        //update table size
        tableColumns = table.columnKeySet().size();
        tableRows = table.rowKeySet().size();

        //update table delta
        tableColumnsDelta = tableColumns - control.getColumns();
        tableRowsDelta = tableRows - control.getRows();

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
