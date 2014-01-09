package ru.inheaven.aida.cexio.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarAjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import ru.inheaven.aida.cexio.entity.Trader;
import ru.inheaven.aida.cexio.service.TraderBean;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *          Date: 07.01.14 20:54
 */
public class TraderList extends AbstractPage{
    @EJB
    private TraderBean traderBean;

    public TraderList() {
        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new PropertyColumn<Trader, String>(Model.of("Рынок"), "market"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Товар"), "name"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Цена"), "open"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Верх"), "high"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Низ"), "low"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Объем"), "volume"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Спред"), "spread"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Дата"), "date"));
        list.add(new AbstractColumn<Trader, String>(Model.of("")) {
            @Override
            public void populateItem(final Item<ICellPopulator<Trader>> cellItem, String componentId, final IModel<Trader> rowModel) {
                cellItem.add(new NavbarAjaxLink(componentId, Model.of("Редактировать")) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        setResponsePage(TraderEdit.class, new PageParameters().add("id", rowModel.getObject().getId()));
                    }
                }.setIconType(IconType.edit));
            }
        });

        DataTable<Trader, String> table = new DataTable<>("traders", list, new ListDataProvider<Trader>(){
            @Override
            protected List<Trader> getData() {
                return traderBean.getTraders();
            }
        }, 100);
        table.addTopToolbar(new HeadersToolbar<>(table, null));

        add(table);
    }
}
