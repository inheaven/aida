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
        calendar.add(Calendar.MINUTE, -150);
        calendar.set(2012, Calendar.FEBRUARY, 6, 12, 0, 0);

        control = new MatrixControl("RIU2", calendar.getTime(), 20, 20, MatrixPeriodType.ONE_MINUTE, 1000*60*10, 1f);

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
