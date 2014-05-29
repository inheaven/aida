package ru.inheaven.aida.coin.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.image.GlyphIconType;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarAjaxLink;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.atmosphere.Subscribe;
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
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.service.ManagedService;
import ru.inheaven.aida.coin.service.TraderBean;
import ru.inheaven.aida.coin.service.TraderService;

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
    private ManagedService managedService;

    public TraderList() {
        add(new BootstrapLink<String>("add", Buttons.Type.Link) {
            @Override
            public void onClick() {
                setResponsePage(TraderEdit.class);
            }
        }.setIconType(GlyphIconType.plus).setLabel(Model.of("Добавить")));

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
                Label label = new Label(componentId, "event bus test");
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
                }.setIconType(GlyphIconType.edit));
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

        add( new Label("test_label", Model.of("subscribe")){
            @Subscribe
            public void onEvent(AjaxRequestTarget target, String ticker){
                setDefaultModelObject(ticker);

                target.add(this);
            }
        }.setOutputMarkupId(true));

        add( new Label("test_label_date", Model.of("")){
            @Subscribe
            public void onEvent(AjaxRequestTarget target, Date date){
                setDefaultModelObject(date.toString());

                target.add(this);
            }
        }.setOutputMarkupId(true));

        add(new BootstrapLink<String>("test", Buttons.Type.Link) {
            @Override
            public void onClick() {
                managedService.startTestTickerUpdateManagedService(EventBus.get());
            }
        }.setIconType(GlyphIconType.warningsign).setLabel(Model.of("test")));
    }


}
