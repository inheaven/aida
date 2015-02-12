package ru.inheaven.aida.coin.web;

import org.apache.wicket.Component;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.Trader;

import java.util.Map;

/**
 * @author Anatoly Ivanov
 *         Date: 006 06.08.14 12:16
 */
public class TraderColumn extends AbstractColumn<Trader, String> {
    private Map<ExchangePair, Component> map;

    public TraderColumn(IModel<String> displayModel, Map<ExchangePair, Component> map) {
        super(displayModel);

        this.map = map;
    }

    @Override
    public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
        Trader trader = rowModel.getObject();

        Label label = new Label(componentId, Model.of(getInitValue(trader)));
        label.setOutputMarkupId(true);

        cellItem.add(label);

        map.put(new ExchangePair(trader.getExchangeType(), trader.getPair(), trader.getType()), label);
    }

    protected String getInitValue(Trader trader){
        return "0";
    }
}
