package ru.inhell.aida.matrix.web;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.LoadableDetachableModel;
import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;
import ru.inhell.aida.matrix.entity.MatrixQuantity;
import ru.inhell.aida.matrix.entity.MatrixTable;
import ru.inhell.aida.matrix.service.MatrixService;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 16:54
 */
public class MatrixPanel extends Panel {
    @EJB
    private MatrixService matrixService;

    private String symbol;
    private Date start;
    private int columnCount;
    private int rowCount;
    private MatrixPeriodType periodType;
    private long timeStep;
    private float priceStep;

    public MatrixPanel(String id, String symbol, Date start, int columnCount, int rowCount, MatrixPeriodType periodType,
                       long timeStep, float priceStep) {
        super(id);
        this.symbol = symbol;
        this.start = start;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.periodType = periodType;
        this.timeStep = timeStep;
        this.priceStep = priceStep;

        init();
    }

    private void init(){
        List<Matrix> matrixList = new ArrayList<>(); //todo get from service

        final MatrixTable matrixTable = MatrixTable.of(matrixList);

        ListView prices = new ListView<Float>("prices",
                new LoadableDetachableModel<List<? extends Float>>() {
                    @Override
                    protected List<? extends Float> load() {
                        return getPriceSeries(matrixTable.getMinPrice(), matrixTable.getMaxPrice());
                    }
                }) {
            @Override
            protected void populateItem(ListItem<Float> priceItem) {
                final Float price = priceItem.getModelObject();

                ListView dates = new ListView<Long>("dates",
                        new LoadableDetachableModel<List<? extends Long>>() {
                            @Override
                            protected List<? extends Long> load() {
                                return getDateSeries();
                            }
                        }) {
                    @Override
                    protected void populateItem(ListItem<Long> dateItem) {
                        Long date = dateItem.getModelObject();

                        MatrixQuantity quantity = matrixTable.get(date, price);

                        dateItem.add(new Label("buy", quantity.getBuyQuantity() + ""));
                        dateItem.add(new Label("sell", quantity.getSellQuantity() + ""));
                    }
                };
                priceItem.add(dates);



            }
        };
        add(prices);


    }

    private List<Long> getDateSeries(){
        List<Long> series = new ArrayList<>(columnCount);

        long time = start.getTime();

        for (int i = 0; i < columnCount; ++i){
            series.add(time);

            time += timeStep;
        }

        return series;
    }

    private List<Float> getPriceSeries(float minPrice, float maxPrice){
        List<Float> series = new ArrayList<>();

        int row = (int) ((maxPrice - minPrice) / priceStep);

        float price;

        if (row < rowCount){
            price = minPrice - priceStep*(rowCount - row)/2;
        }else {
            price = maxPrice - priceStep*rowCount;
        }

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
}
