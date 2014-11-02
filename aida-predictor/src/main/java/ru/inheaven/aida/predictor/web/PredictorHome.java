package ru.inheaven.aida.predictor.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import ru.inheaven.aida.predictor.service.PredictorService;

import javax.ejb.EJB;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:01
 */
public class PredictorHome extends WebPage {
    @EJB
    private PredictorService predictorService;

    public PredictorHome() {
        add(new Link("test") {
            @Override
            public void onClick() {
                predictorService.test();
            }
        });
    }
}
