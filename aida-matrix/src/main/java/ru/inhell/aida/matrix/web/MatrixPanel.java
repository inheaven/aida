package ru.inhell.aida.matrix.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.atmosphere.Subscribe;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import ru.inhell.aida.common.util.DateUtil;
import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.matrix.entity.MatrixControl;
import ru.inhell.aida.matrix.entity.MatrixQuantity;
import ru.inhell.aida.matrix.entity.MatrixTable;
import ru.inhell.aida.matrix.service.MatrixBean;

import javax.ejb.EJB;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 16:54
 */
public class MatrixPanel extends Panel {
    @EJB
    private MatrixBean matrixBean;

    private MatrixControl control;
    private IModel<MatrixTable> tableModel;

    public MatrixPanel(String id, final MatrixControl control) {
        super(id);
        this.control = control;

        tableModel = new LoadableDetachableModel<MatrixTable>() {
            @Override
            protected MatrixTable load() {
                long start = (control.getStart().getTime()/control.getTimeStep())* control.getTimeStep();
                long end = start + control.getColumnCount()*control.getTimeStep();

                List<Matrix> matrixList = matrixBean.getMatrixList(control.getSymbol(), new Date(start), new Date(end),
                        control.getPeriodType());

                return MatrixTable.of(matrixList, control.getTimeStep(), control.getPriceStep());
            }
        };
        setDefaultModel(tableModel);

        init();
    }

    private void init(){
        ListView prices = new ListView<Float>("prices",
                new LoadableDetachableModel<List<? extends Float>>() {
                    @Override
                    protected List<? extends Float> load() {
                        MatrixTable matrixTable = tableModel.getObject();

                        return control.getPriceSeries(matrixTable.getMinPrice(), matrixTable.getMaxPrice());
                    }
                }) {
            @Override
            protected void populateItem(ListItem<Float> priceItem) {
                final Float price = priceItem.getModelObject();

                priceItem.add(new Label("price_left", price + ""));
                priceItem.add(new Label("price_right", price + ""));

                ListView dates = new ListView<Long>("dates", control.getDateSeries()) {
                    @Override
                    protected void populateItem(ListItem<Long> dateItem) {
                        Long date = dateItem.getModelObject();

                        MatrixQuantity quantity = tableModel.getObject().get(date, price);

                        String buy = "";
                        String sell = "";

                        if (quantity != null){
                            buy = quantity.getBuyQuantity() + "/";
                            sell = quantity.getSellQuantity() + "";
                        }

                        dateItem.add(new Label("buy", buy));
                        dateItem.add(new Label("sell", sell));
                    }
                };
                priceItem.add(dates);
            }
        };
        add(prices);

        add(new ListView<Long>("dates_label",
                new LoadableDetachableModel<List<? extends Long>>() {
                    @Override
                    protected List<? extends Long> load() {
                        return control.getDateSeries();
                    }
                }) {
            @Override
            protected void populateItem(ListItem<Long> item) {
                item.add(new Label("label", DateUtil.getString(new Date(item.getModelObject()))));
            }
        });
    }

    @Subscribe
    public void receiveMessage(AjaxRequestTarget target, String message){
        target.add(this);
    }
}
