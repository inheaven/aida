package ru.inhell.aida.matrix.web;

import com.google.common.collect.Lists;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.datetime.markup.html.form.DateTextField;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.IChoiceRenderer;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.odlabs.wiquery.ui.datepicker.DatePicker;
import ru.inhell.aida.common.web.IUpdateListener;
import ru.inhell.aida.matrix.entity.MatrixControl;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;

import java.util.Arrays;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 14.06.12 20:54
 */
public class MatrixControlPanel extends Panel{
    public MatrixControlPanel(String id, MatrixControl control, final IUpdateListener listener) {
        super(id);

        final FeedbackPanel feedbackPanel = new FeedbackPanel("messages");
        feedbackPanel.setOutputMarkupId(true);
        add(feedbackPanel);

        Form form = new Form<>("form", new CompoundPropertyModel<>(control));
        add(form);

        form.add(new TextField<>("symbol"));
        form.add(DateTextField.forDatePattern("start", "dd.MM.yy HH:mm"));
        form.add(new TextField<>("columnCount", Integer.class));
        form.add(new TextField<>("rowCount", Integer.class));
        form.add(new DropDownChoice<>("periodType", Arrays.asList(MatrixPeriodType.values()),
                new IChoiceRenderer<MatrixPeriodType>() {
                    @Override
                    public Object getDisplayValue(MatrixPeriodType object) {
                        return object.name();
                    }

                    @Override
                    public String getIdValue(MatrixPeriodType object, int index) {
                        return object.ordinal() + "";
                    }
                }));
        form.add(new TextField<>("timeStep", Long.class));
        form.add(new TextField<>("priceStep", Float.class));

        form.add(new AjaxButton("submit") {
            @Override
            protected void onSubmit(AjaxRequestTarget target, Form<?> form) {
                listener.onUpdate(target);
            }

            @Override
            protected void onError(AjaxRequestTarget target, Form<?> form) {
                target.add(feedbackPanel);
            }
        });
    }
}
