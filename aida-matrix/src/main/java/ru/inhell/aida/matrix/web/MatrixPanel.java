package ru.inhell.aida.matrix.web;

import com.google.common.collect.HashBasedTable;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.atmosphere.AtmosphereBehavior;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.atmosphere.Subscribe;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import ru.inhell.aida.common.util.DateUtil;
import ru.inhell.aida.common.util.EjbUtil;
import ru.inhell.aida.common.util.OsgiUtil;
import ru.inhell.aida.matrix.entity.*;
import ru.inhell.aida.matrix.service.MatrixBean;
import ru.inhell.aida.matrix.service.MatrixTimerService;

import javax.ejb.EJB;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 16:54
 */
public class MatrixPanel extends Panel {
    @EJB
    private MatrixBean matrixBean;

    @EJB
    private MatrixTimerService matrixTimerService;

    private  HashBasedTable<Float, Long, Component> componentTable = HashBasedTable.create();

    private WebMarkupContainer container;

    public MatrixPanel(String id, final MatrixControl control, boolean realtime) {
        super(id);

        setVersioned(false);

        //Matrix Table
        final IModel<MatrixTable> tableModel = new LoadableDetachableModel<MatrixTable>() {
            @Override
            protected MatrixTable load() {
                MatrixTable matrixTable = new MatrixTable(control,  new IMatrixLoader() {
                    @Override
                    public List<Matrix> load(MatrixType matrixType, Date start, Date end) {
                        return EjbUtil.getEjb(OsgiUtil.getModuleName(MatrixBean.class), MatrixBean.class)
                                .getMatrixList(matrixType.getSymbol(), start, end, matrixType.getPeriodType());
                    }
                });

                //Matrix Table Listener
                matrixTable.addListener(new IMatrixTableListener() {
                    EventBus eventBus = EventBus.get();

                    @Override
                    public void onChange(MatrixEvent event) {
                        eventBus.post(event);
                    }
                });

                return matrixTable;
            }
        };

        //Ajax Container
        container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        //Matrix
        ListView prices = new ListView<Float>("prices",
                new LoadableDetachableModel<List<Float>>() {
                    @Override
                    protected List<Float> load() {
                        componentTable.clear();

                        List<Float> prices = tableModel.getObject().getPrices();
                        Collections.reverse(prices);

                        return prices;
                    }
                }) {
            @Override
            protected void populateItem(ListItem<Float> priceItem) {
                final Float price = priceItem.getModelObject();

                priceItem.add(new Label("price_left", price + ""));
                priceItem.add(new Label("price_right", price + ""));

                ListView dates = new ListView<Long>("times", tableModel.getObject().getTimes()) {
                    @Override
                    protected void populateItem(ListItem<Long> dateItem) {
                        final Long time = dateItem.getModelObject();

                        WebMarkupContainer container = new WebMarkupContainer("container");
                        container.setOutputMarkupId(true);
                        dateItem.add(container);

                        //component table
                        componentTable.put(price, time, container);

                        //buy
                        container.add(new Label("buy", new LoadableDetachableModel<String>() {
                            @Override
                            protected String load() {
                                MatrixCell cell = tableModel.getObject().get(price, time);

                                return cell != null ? cell.getBuyQuantity() + "" : "";
                            }
                        }));

                        //sell
                        container.add(new Label("sell", new LoadableDetachableModel<String>() {
                            @Override
                            protected String load() {
                                MatrixCell cell = tableModel.getObject().get(price, time);

                                return cell != null ? cell.getSellQuantity() + "" : "";
                            }
                        }));
                    }
                };
                priceItem.add(dates);
            }
        };
        container.add(prices);

        //Date Labels
        ListView dateLabels = new ListView<Long>("date_labels",
                new LoadableDetachableModel<List<Long>>() {
                    @Override
                    protected List<Long> load() {
                        return tableModel.getObject().getTimes();
                    }
                }) {
            @Override
            protected void populateItem(ListItem<Long> item) {
                item.add(new Label("label", DateUtil.getString(new Date(item.getModelObject()))));
            }
        };
        container.add(dateLabels);

        //Matrix Timer Service
        if (realtime){
            add(new AtmosphereBehavior(){
                @Override
                public void onResourceRequested() {
                    super.onResourceRequested();

                    matrixTimerService.addListener(control.getMatrixType(), tableModel.getObject());
                }

                @Override
                public void onDisconnect(AtmosphereResourceEvent event) {
                    super.onDisconnect(event);

                    matrixTimerService.removeListener(control.getMatrixType(), tableModel.getObject());
                }
            });
        }
    }

    @Subscribe
    public void matrixTableChanged(AjaxRequestTarget target, MatrixEvent event){
        if (event.isCropped()){
            target.add(container);
        }else {
            for (MatrixCell cell : event.getMatrixCells()){
                Component component = componentTable.get(cell.getPrice(), cell.getTime());

                if (component != null) {
                    target.add(component);
                }
            }
        }
    }
}
