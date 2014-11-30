package ru.inheaven.aida.coin.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapAjaxButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.dialog.Modal;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.BootstrapForm;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.FormGroup;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.FormType;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.form.*;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.PropertyModel;
import ru.inheaven.aida.coin.entity.ExchangeType;
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.entity.TraderType;
import ru.inheaven.aida.coin.service.TraderBean;

import javax.ejb.EJB;
import java.util.Arrays;

import static org.apache.wicket.model.Model.of;

/**
 * inheaven on 30.11.2014 4:45.
 */
public class TraderEditModal extends Modal {
    @EJB
    private TraderBean traderBean;

    private IModel<Trader> traderModel = new CompoundPropertyModel<>(new Trader());

    private BootstrapForm<Trader> form;

    public TraderEditModal(String markupId) {
        super(markupId);

        setHeaderVisible(false);
        setFadeIn(false);

        form  = new BootstrapForm<>("form",  traderModel);
        form.setOutputMarkupId(true);
        form.type(FormType.Horizontal);
        add(form);

        form.add(new FormGroup("exchange", of("Exchange")).add(new DropDownChoice<>("exchange",
                new PropertyModel<>(traderModel, "exchange"),
                Arrays.asList(ExchangeType.values())).setRequired(true)));

        form.add(new FormGroup("pair", of("Pair")).add(new RequiredTextField<>("pair", new PropertyModel<>(traderModel, "pair"))));
        form.add(new FormGroup("high", of("High")).add(new RequiredTextField<>("high",  new PropertyModel<>(traderModel, "high"))));
        form.add(new FormGroup("low", of("Low")).add(new RequiredTextField<>("low",  new PropertyModel<>(traderModel, "low"))));
        form.add(new FormGroup("lot", of("Lot")).add(new TextField<>("lot",  new PropertyModel<>(traderModel, "lot"))
                .setConvertEmptyInputStringToNull(true)));

        form.add(new FormGroup("type", of("Type")).add(new DropDownChoice<>("type",
                new PropertyModel<>(traderModel, "type"),
                Arrays.asList(TraderType.values()), new IChoiceRenderer<TraderType>(){
            @Override
            public Object getDisplayValue(TraderType object) {
                return object.name();
            }

            @Override
            public String getIdValue(TraderType object, int index) {
                return object.name();
            }
        }).setRequired(true)));

        form.add(new FormGroup("running", of("Active")).add(new DropDownChoice<>("running",
                new PropertyModel<>(traderModel, "running"),
                Arrays.asList(new Boolean[]{true, false})).setRequired(true)));

        form.add(new BootstrapAjaxButton("save", of("Save"), Buttons.Type.Primary){
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                traderBean.save(traderModel.getObject());

                close(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(form);
            }
        });

        form.add(new BootstrapAjaxButton("cancel", of("Cancel"), Buttons.Type.Default){
            @Override
            public void onSubmit(AjaxRequestTarget target, Form<?> form) {
                close(target);
            }
        }.setDefaultFormProcessing(false));
    }

    public void show(AjaxRequestTarget target, Trader trader){
        traderModel.setObject(trader);
        form.clearInput();

        target.add(form);

        show(target);
    }
}
