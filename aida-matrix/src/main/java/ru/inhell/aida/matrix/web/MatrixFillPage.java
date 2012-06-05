package ru.inhell.aida.matrix.web;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.odlabs.wiquery.ui.datepicker.DatePicker;
import ru.inhell.aida.common.service.IProcessCommand;
import ru.inhell.aida.common.service.IProcessListener;
import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.template.web.AbstractPage;
import ru.inhell.aida.template.web.TemplateMenu;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 05.06.12 14:52
 */
@TemplateMenu
public class MatrixFillPage extends AbstractPage{
    private AjaxSelfUpdatingTimerBehavior timerBehavior = null;

    public MatrixFillPage() {
        Form form = new Form<>("form");
        add(form);

        final TextField symbol = new TextField<>("symbol", Model.of());
        form.add(symbol);

        final DatePicker start = new DatePicker<>("start", Model.of(new Date()));
        form.add(start);

        final DatePicker end = new DatePicker<>("end", Model.of(new Date()));
        form.add(end);

        final WebMarkupContainer container = new WebMarkupContainer("container");
        container.setOutputMarkupId(true);
        add(container);

        final Label processed = new Label("processed", Model.of());
        container.add(processed);

        final Label skipped = new Label("skipped", Model.of());
        container.add(skipped);

        final Label error = new Label("error", Model.of());
        container.add(error);

        final IProcessCommand command = new IProcessCommand() {
            @Override
            public void cancel() {
                //todo cancel
            }
        };

        Button submit = new Button("submit"){
            @Override
            public void onSubmit() {
                IProcessListener listener = new IProcessListener<Matrix>() {
                    int processedCount = 0;
                    int skippedCount = 0;

                    @Override
                    public void processed(Matrix o) {
                        processed.setDefaultModelObject(++processedCount + ": " + o.getPrice() + " " + o.getDate());
                    }

                    @Override
                    public void skipped(Matrix o) {
                        skipped.setDefaultModelObject(++skippedCount + ": " + o.getPrice() + " " + o.getDate());
                    }

                    @Override
                    public void error(Matrix o, Exception e) {
                        error.setDefaultModelObject(e.getMessage());
                    }
                };

                //todo start (listener)
                timerBehavior = new AjaxSelfUpdatingTimerBehavior(Duration.ONE_SECOND);
                container.add(timerBehavior);
            }
        };
        form.add(submit);

        AjaxButton cancel = new AjaxButton("cancel"){
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                if (timerBehavior != null){
                    timerBehavior.stop(target);
                    command.cancel();
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
