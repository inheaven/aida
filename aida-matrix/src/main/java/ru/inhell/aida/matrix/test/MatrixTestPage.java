package ru.inhell.aida.matrix.test;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.atmosphere.EventBus;
import org.apache.wicket.atmosphere.Subscribe;
import ru.inhell.aida.common.web.IUpdateListener;
import ru.inhell.aida.matrix.entity.MatrixControl;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;
import ru.inhell.aida.matrix.web.MatrixControlPanel;
import ru.inhell.aida.matrix.web.MatrixPanel;
import ru.inhell.aida.template.web.AbstractPage;
import ru.inhell.aida.template.web.TemplateMenu;

import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

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
        calendar.set(2012, Calendar.FEBRUARY, 6, 12, 0, 0);

        control = new MatrixControl("GAZP", calendar.getTime(), 10, 20, MatrixPeriodType.ONE_MINUTE, 1000*60*10, 0.25f);

        matrixPanel = new MatrixPanel("matrix", control);
        matrixPanel.setOutputMarkupId(true);
        add(matrixPanel);

        add(new MatrixControlPanel("control", control, new IUpdateListener() {
            @Override
            public void onUpdate(AjaxRequestTarget target) {
                target.add(matrixPanel);
            }
        }));

        final long time = control.getStart().getTime();

        final EventBus eventBus = EventBus.get();

        Executors.newScheduledThreadPool(1).scheduleWithFixedDelay(new Runnable() {
            long t = time;

            @Override
            public void run() {
                eventBus.post(t += 1000 * 60);
            }
        }, 2, 2, TimeUnit.SECONDS);

        add(new AjaxLink("start") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {

            }
        });

        add(new AjaxLink("stop") {
            @Override
            public void onClick(AjaxRequestTarget ajaxRequestTarget) {

            }
        });
    }

    @Subscribe
    public void receiveMessage(AjaxRequestTarget target, long time){
        control.setStart(new Date(time));

        target.add(matrixPanel);
    }
}
