package ru.inhell.aida.matrix.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import ru.inhell.aida.common.web.IUpdateListener;
import ru.inhell.aida.matrix.entity.MatrixControl;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;
import ru.inhell.aida.matrix.web.MatrixControlPanel;
import ru.inhell.aida.matrix.web.MatrixPanel;
import ru.inhell.aida.template.web.AbstractPage;
import ru.inhell.aida.template.web.TemplateMenu;

import java.util.Calendar;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 16:43
 */
@TemplateMenu()
public class MatrixTestPage extends AbstractPage{
    private MatrixControl control;
    private MatrixPanel matrixPanel;

    public MatrixTestPage() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_MONTH, 3);
        calendar.set(Calendar.HOUR_OF_DAY, 12);

        control = new MatrixControl("RIU2", calendar.getTime(), 30, 30, MatrixPeriodType.ONE_MINUTE, 1000*60, 50f);

        matrixPanel = new MatrixPanel("matrix", control, true);
        matrixPanel.setOutputMarkupId(true);
        add(matrixPanel);

        add(new MatrixControlPanel("control", control, new IUpdateListener() {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                target.add(matrixPanel);
            }
        }));
    }

}
