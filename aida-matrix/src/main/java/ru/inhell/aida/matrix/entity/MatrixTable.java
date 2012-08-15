package ru.inhell.aida.matrix.entity;

import com.google.common.collect.TreeBasedTable;
import ru.inhell.aida.common.service.IProcessListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private float lastPrice;

    private List<IMatrixTableListener> listeners;

    public MatrixTable(MatrixControl control, IMatrixLoader loader){
        this.control = control;
        this.loader = loader;

        controlChanged();
    }

    public void addListener(IMatrixTableListener listener){
        if (listeners == null){
            listeners = new ArrayList<>();
        }

        listeners.add(listener);
    }

    public void removeListener(IMatrixTableListener listener){
        listeners.remove(listener);
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

    private boolean cropColumns(){
        //columns
        int deltaColumn = table.columnKeySet().size() - control.getColumns();

        boolean cropped = false;

        if (deltaColumn > 0){
            List<Long> times = getTimes();
            List<Long> remove = new ArrayList<>();

            for (int i=0; i < deltaColumn; ++i){
                remove.add(times.get(i));
            }

            for (Long time : remove){
                table.columnKeySet().remove(time);
            }

            cropped = true;
        }

        return cropped;
    }

    public MatrixTable add(List<Matrix> matrixList){
        //skip if null or empty
        if (matrixList == null || matrixList.isEmpty()){
            return this;
        }

        List<MatrixCell> cells = new ArrayList<>();

        //group matrix by cell
        for (Matrix matrix : matrixList){
            float price = getPrice(matrix);
            long time = getTime(matrix);

            MatrixCell cell = table.get(price, time);

            if (cell == null){
                cell = new MatrixCell(price, time);
                table.put(price, time, cell);
            }

            cell.add(matrix);

            //changed cells
            cells.add(cell);
        }

        //last price
        lastPrice = matrixList.get(matrixList.size() - 1).getPrice();

        //crop columns
        boolean cropped = cropColumns();

        //max and min
        updateMaxMin();

        //notify listeners
        if (listeners != null){
            for(IMatrixTableListener listener : listeners){
                listener.onChange(new MatrixEvent(cells, cropped));
            }
        }

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

    public List<Long> getTimes(){
        return new ArrayList<>(table.columnKeySet());
    }

    public List<Float> getPrices(){
        int count = (int) ((maxPrice - minPrice) / control.getPriceStep());

        float startPrice;

        if (count < control.getRows()){
            //center
            startPrice = minPrice - control.getPriceStep()*(control.getRows() - count)/2;
        }else{
            if (maxPrice - lastPrice < lastPrice - minPrice){
                //long
                startPrice = maxPrice - control.getPriceStep()*control.getRows();
            }else {
                //short
                startPrice = minPrice;
            }
        }

        startPrice = table.rowKeySet().tailSet(startPrice).first();

        List<Float> prices = new ArrayList<>(control.getRows());

        for (int i=0; i < control.getRows(); ++i){
            prices.add(startPrice + i*control.getPriceStep());
        }

        return prices;
    }

    public MatrixCell get(Float price, Long date) {
        return table.get(price, date);
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
