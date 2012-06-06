package ru.inhell.aida.matrix.test;

import ru.inhell.aida.matrix.entity.MatrixControl;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;
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
    public MatrixTestPage() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(2012, Calendar.FEBRUARY, 6, 12, 0, 0);

        add(new MatrixPanel("matrix", new MatrixControl("GAZP", calendar.getTime(), 20, 20, MatrixPeriodType.ONE_MINUTE, 1000*60, 0.25f)));
    }
}
