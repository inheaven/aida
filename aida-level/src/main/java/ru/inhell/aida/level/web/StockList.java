package ru.inhell.aida.level.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import ru.inhell.aida.level.entity.Level;
import ru.inhell.aida.level.entity.Stock;
import ru.inhell.aida.level.service.StockBean;

import javax.ejb.EJB;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 12.12.12 16:43
 */
public class StockList extends WebPage{
    @EJB
    private StockBean stockBean;

    public StockList() {
        final IModel<Stock> stockModel = Model.of(new Stock());

        WebMarkupContainer levelsContainer = new WebMarkupContainer("levels_container");
        add(levelsContainer);

        levelsContainer.add(new ListView<Level>("levels",
                new LoadableDetachableModel<List<? extends Level>>() {
                    @Override
                    protected List<? extends Level> load() {
                        return stockBean.getLevels(stockModel.getObject());
                    }
                }) {
            @Override
            protected void populateItem(ListItem<Level> item) {
                Level level = item.getModelObject();

                item.add(new Label("index", item.getIndex() + ""));
                item.add(new Label("lot", level.getLot() + ""));
                item.add(new Label("buyPrice", level.getBuyPrice() + ""));
                item.add(new Label("sellPrice", level.getSellPrice() + ""));
            }
        });

        add(new ListView<Stock>("stocks",
                new LoadableDetachableModel<List<? extends Stock>>() {
                    @Override
                    protected List<? extends Stock> load() {
                        return stockBean.getStocks();
                    }
                }) {
            @Override
            protected void populateItem(ListItem<Stock> item) {
                final Stock stock = item.getModelObject();

                AjaxLink link = new AjaxLink("link") {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        stockModel.setObject(stock);
                    }
                };
                item.add(link);

                link.add(new Label("symbol", stock.getSymbol()));

            }
        });


    }
}
