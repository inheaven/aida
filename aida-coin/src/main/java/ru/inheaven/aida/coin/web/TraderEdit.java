package ru.inheaven.aida.coin.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.button.BootstrapButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.button.Buttons;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.BootstrapForm;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.FormGroup;
import de.agilecoders.wicket.core.markup.html.bootstrap.form.FormType;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import ru.inheaven.aida.coin.entity.ExchangeType;
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.entity.TraderType;
import ru.inheaven.aida.coin.service.TraderBean;

import javax.ejb.EJB;
import java.util.Arrays;

import static org.apache.wicket.model.Model.of;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 14:56
 */
public class TraderEdit extends AbstractPage{
    @EJB
    private TraderBean traderBean;

    public TraderEdit(PageParameters pageParameters) {
        Long id = pageParameters.get("id").toOptionalLong();

        final IModel<Trader> traderModel = new CompoundPropertyModel<>(id != null ? traderBean.getTrader(id) : new Trader());

        BootstrapForm form  = new BootstrapForm<>("form",  traderModel);
        form.type(FormType.Horizontal);
        add(form);

        form.add(new FormGroup("exchange", of("Exchange")).add(new DropDownChoice<>("exchange",
                Arrays.asList(ExchangeType.values())).setRequired(true)));
        form.add(new FormGroup("pair", of("Pair")).add(new RequiredTextField("pair")));
        form.add(new FormGroup("high", of("High")).add(new RequiredTextField<>("high")));
        form.add(new FormGroup("low", of("Low")).add(new RequiredTextField<>("low")));
//        form.add(new FormGroup("volume", of("Volume")).add(new RequiredTextField<>("volume")));
//        form.add(new FormGroup("spread", of("Spread")).add(new RequiredTextField<>("spread")));
        form.add(new FormGroup("lot", of("Lot")).add(new TextField<>("lot").setConvertEmptyInputStringToNull(true)));

        form.add(new FormGroup("type", of("Type")).add(new DropDownChoice<>("type",
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
                Arrays.asList(new Boolean[]{true, false})).setRequired(true)));

        form.add(new BootstrapButton("save", of("Save"), Buttons.Type.Primary){
            @Override
            public void onSubmit() {
                traderBean.save(traderModel.getObject());

                setResponsePage(TraderList.class);
            }
        });

        form.add(new BootstrapButton("cancel", of("Cancel"), Buttons.Type.Default){
            @Override
            public void onSubmit() {
                setResponsePage(TraderList.class);
            }
        }.setDefaultFormProcessing(false));
    }
}
