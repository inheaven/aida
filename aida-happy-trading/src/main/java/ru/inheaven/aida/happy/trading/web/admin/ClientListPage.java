package ru.inheaven.aida.happy.trading.web.admin;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import ru.inheaven.aida.happy.trading.entity.Client;
import ru.inheaven.aida.happy.trading.mapper.ClientMapper;
import ru.inheaven.aida.happy.trading.web.BasePage;

import javax.inject.Inject;
import java.util.List;

/**
 * @author inheaven on 018 18.06.15 17:59
 */
public class ClientListPage extends BasePage {
    @Inject
    private ClientMapper clientMapper;

    public ClientListPage() {
        add(new ListView<Client>("clients",
                new LoadableDetachableModel<List<? extends Client>>() {
                    @Override
                    protected List<? extends Client> load() {
                        return clientMapper.getClients();
                    }
                }) {
            @Override
            protected void populateItem(ListItem<Client> item) {
                Client client = item.getModelObject();

                item.add(new Label("name", client.getName()));
                item.add(new Label("login", client.getLogin()));
                item.add(new BootstrapLink<Client>("edit", item.getModel()) {
                    @Override
                    public void onClick() {
                        setResponsePage(ClientEditPage.class, new PageParameters().add("id", getModelObject().getId()));
                    }
                }.setLabel(Model.of("Edit")));
            }
        });

        add(new BootstrapLink<Object>("add", Buttons.Type.Primary) {
            @Override
            public void onClick() {
                setResponsePage(ClientEditPage.class);
            }
        }.setLabel(Model.of("Add")));

    }
}
