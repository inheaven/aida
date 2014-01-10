package ru.inheaven.aida.cexio.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.IconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarAjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.datetime.markup.html.basic.DateLabel;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.*;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.ListDataProvider;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.time.Duration;
import ru.inheaven.aida.cexio.entity.Trader;
import ru.inheaven.aida.cexio.service.TraderBean;
import ru.inheaven.aida.cexio.service.TraderService;
import ru.inheaven.aida.cexio.util.SignatureUtil;

import javax.ejb.EJB;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *          Date: 07.01.14 20:54
 */
public class TraderList extends AbstractPage{
    @EJB
    private TraderBean traderBean;

    @EJB
    private TraderService traderService;

    public TraderList() {
        add(new BootstrapLink<String>("add", Buttons.Type.Link) {
            @Override
            public void onClick() {
                setResponsePage(TraderEdit.class);
            }
        }.setIconType(IconType.plus).setInverted(false).setLabel(Model.of("Добавить")));

        List<IColumn<Trader, String>> list = new ArrayList<>();

        list.add(new PropertyColumn<Trader, String>(Model.of("Рынок"), "market"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Товар"), "name"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Открыто"), "open"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Верх"), "high"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Низ"), "low"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Объем"), "volume"));
        list.add(new PropertyColumn<Trader, String>(Model.of("Спред"), "spread"));
        list.add(new AbstractColumn<Trader, String>(Model.of("Дата")){

            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, IModel<Trader> rowModel) {
                cellItem.add(DateLabel.forDateStyle(componentId, new PropertyModel<Date>(rowModel, "date"), "MM"));
            }
        });
        list.add(new AbstractColumn<Trader, String>(Model.of("Цена")) {
            @Override
            public void populateItem(Item<ICellPopulator<Trader>> cellItem, String componentId, final IModel<Trader> rowModel) {
                Label label = new Label(componentId, new LoadableDetachableModel<String>() {
                    @Override
                    protected String load() {
                        return traderService.getTicker(rowModel.getObject().getName()).getLast().setScale(8).toPlainString();
                    }
                });
                label.add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(1)));
                label.setOutputMarkupId(true);
                cellItem.add(label);
            }
        });
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
        table.setOutputMarkupId(true);
        table.addTopToolbar(new HeadersToolbar<>(table, null));
        add(table);



        add(new BootstrapLink<String>("test", Buttons.Type.Link) {
            @Override
            public void onClick() {
                System.out.println(SignatureUtil.getSignature(SignatureUtil.getNonce()));
            }
        }.setIconType(IconType.warningsign).setInverted(false).setLabel(Model.of("test")));
    }
}
