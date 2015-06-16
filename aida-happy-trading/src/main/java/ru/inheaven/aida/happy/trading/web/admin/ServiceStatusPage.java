package ru.inheaven.aida.happy.trading.web.admin;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.AbstractReadOnlyModel;
import ru.inheaven.aida.happy.trading.service.TestService;

import javax.inject.Inject;

/**
 * @author inheaven on 16.06.2015 19:45.
 */
public class ServiceStatusPage extends WebPage{
    @Inject
    private TestService testService;

    public ServiceStatusPage() {
        add(new Label("testLabel", new AbstractReadOnlyModel<String>() {
            @Override
            public String getObject() {
                return testService.getCurrentHelloWorld();
            }
        }));
    }
}
