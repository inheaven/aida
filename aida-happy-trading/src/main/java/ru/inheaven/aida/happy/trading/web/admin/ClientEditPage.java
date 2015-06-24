package ru.inheaven.aida.happy.trading.web.admin;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapLink;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.BootstrapForm;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.FormGroup;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.OnChangeAjaxBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.Client;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.mapper.ClientMapper;
import ru.inheaven.aida.happy.trading.web.BasePage;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author inheaven on 018 18.06.15 17:59
 */
public class ClientEditPage extends BasePage{
    @Inject
    private ClientMapper clientMapper;

    public ClientEditPage() {
        this(new PageParameters());
    }

    public ClientEditPage(PageParameters parameters) {
        Long id = parameters.get("id").toOptionalLong();

        Client client = id != null ? clientMapper.getClient(id) : new Client();

        if (client == null){
            throw new RuntimeException("client not found");
        }

        if (client.getId() == null){
            client.setAccounts(new ArrayList<>());
            client.setStrategies(new ArrayList<>());
        }

        BootstrapForm<Client> form = new BootstrapForm<>("form", new CompoundPropertyModel<>(client));
        add(form);

        form.add(new FormGroup("login", Model.of("Login")).add(new RequiredTextField<>("login")));
        form.add(new FormGroup("name", Model.of("Name")).add(new RequiredTextField<>("name")));
        form.add(new FormGroup("password", Model.of("Password")).add(new RequiredTextField<>("password")));

        form.add(new BootstrapButton("save", Buttons.Type.Primary) {
            @Override
            public void onSubmit() {
                clientMapper.save(client);

                setResponsePage(ClientListPage.class);
            }
        }.setLabel(Model.of("Save")));

        form.add(new BootstrapLink<Object>("cancel", Buttons.Type.Default) {
            @Override
            public void onClick() {
                setResponsePage(ClientListPage.class);
            }
        }.setLabel(Model.of("Cancel")));

        WebMarkupContainer accountsContainer = new WebMarkupContainer("accountsContainer");
        accountsContainer.setOutputMarkupId(true);
        form.add(accountsContainer);

        accountsContainer.add(new ListView<Account>("accounts") {
            @Override
            protected void populateItem(ListItem<Account> item) {
                item.add(new DropDownChoice<>("exchangeType", Arrays.asList(ExchangeType.values())).setRequired(true));
                item.add(new FormGroup("apiKey", Model.of("API key")).add(new RequiredTextField("apiKey")));
                item.add(new FormGroup("secretKey", Model.of("Secret key")).add(new RequiredTextField("secretKey")));
                item.add(new BootstrapAjaxLink<Account>("delete", item.getModel(), Buttons.Type.Link) {
                    @Override
                    public void onClick(AjaxRequestTarget target) {
                        client.getAccounts().remove(getModelObject());

                        target.add(accountsContainer);
                    }
                }.setLabel(Model.of("delete")));

                item.visitChildren(FormComponent.class, (component, visit) -> {
                    component.add(new OnChangeAjaxBehavior() {
                        @Override
                        protected void onUpdate(AjaxRequestTarget target) {
                        }
                    });
                });
            }

            @Override
            protected IModel<Account> getListItemModel(IModel<? extends List<Account>> listViewModel, int index) {
                return new CompoundPropertyModel<>(super.getListItemModel(listViewModel, index));
            }
        });

        form.add(new AjaxLink("add") {
            @Override
            public void onClick(AjaxRequestTarget target) {
                client.getAccounts().add(new Account());

                target.add(accountsContainer);
            }
        });

    }
}
