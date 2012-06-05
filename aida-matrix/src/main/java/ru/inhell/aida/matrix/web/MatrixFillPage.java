package ru.inhell.aida.matrix.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.LoadableDetachableModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.odlabs.wiquery.ui.datepicker.DatePicker;
import ru.inhell.aida.common.service.IProcessListener;
import ru.inhell.aida.common.service.ProcessCommand;
import ru.inhell.aida.common.util.DateUtil;
import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;
import ru.inhell.aida.matrix.service.MatrixService;
import ru.inhell.aida.template.web.AbstractPage;
import ru.inhell.aida.template.web.TemplateMenu;

import javax.ejb.EJB;
import java.util.Arrays;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 14:52
 */
@TemplateMenu
public class MatrixFillPage extends AbstractPage{
    @EJB
    private MatrixService matrixService;

    private AjaxSelfUpdatingTimerBehavior timerBehavior = null;

    public MatrixFillPage() {
        Form form = new Form<>("form");
        add(form);

        final TextField<String> symbol = new TextField<>("symbol", Model.of(""));
        symbol.setRequired(true);
        form.add(symbol);

        final DatePicker<Date> start = new DatePicker<>("start", Model.of(new Date()), Date.class);
        form.add(start);

        final DatePicker<Date>  end = new DatePicker<>("end", Model.of(new Date()), Date.class);
        form.add(end);

        final DropDownChoice<MatrixPeriodType>  type = new DropDownChoice<>("type", Model.of(MatrixPeriodType.ONE_MINUTE),
                Arrays.asList(MatrixPeriodType.values()));
        form.add(type);

        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        final IModel<String> pModel = Model.of("");
        Label processed = new Label("processed", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return pModel.getObject();
            }
        });
        container.add(processed);

        final IModel<String> sModel =  Model.of("");
        Label skipped = new Label("skipped", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return sModel.getObject();
            }
        });
        container.add(skipped);

        final IModel<String> eModel =  Model.of("");
        Label error = new Label("error", new LoadableDetachableModel<String>() {
            @Override
            protected String load() {
                return eModel.getObject();
            }
        });
        container.add(error);

        final ProcessCommand command = new ProcessCommand();

        Button submit = new Button("submit"){
            @Override
            public void onSubmit() {
                IProcessListener<Matrix> listener = new IProcessListener<Matrix>() {
                    int processedCount = 0;
                    int skippedCount = 0;

                    @Override
                    public void processed(Matrix o) {
                        pModel.setObject(++processedCount + ": " + DateUtil.getString(o.getDate())  + " " + o.getPrice());
                    }

                    @Override
                    public void skipped(Matrix o) {
                        sModel.setObject(++skippedCount + ": "  + DateUtil.getString(o.getDate())  + " " + o.getPrice());
                    }

                    @Override
                    public void error(Matrix o, Exception e) {
                        eModel.setObject(e.getMessage());
                    }
                };

                command.setCancel(false);

                matrixService.populateMatrixTable(symbol.getModelObject(), start.getModelObject(), end.getModelObject(),
                        type.getModelObject(), listener, command);

                timerBehavior = new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND);
                container.add(timerBehavior);
            }
        };
        form.add(submit);

        AjaxButton cancel = new AjaxButton("cancel"){
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (timerBehavior != null){
                    command.setCancel(true);
                    timerBehavior.stop(target);
                }
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                //wtf
            }
        };
        form.add(cancel);
    }
}
