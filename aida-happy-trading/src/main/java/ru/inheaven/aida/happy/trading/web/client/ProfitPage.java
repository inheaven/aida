package ru.inheaven.aida.happy.trading.web.client;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.resource.JQueryResourceReference;
import ru.inheaven.aida.happy.trading.web.BasePage;

/**
 * @author inheaven on 29.09.2015 2:34.
 */
public class ProfitPage extends BasePage {
    public ProfitPage() {
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highstock.js"));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highcharts-3d.js"));

        response.render(JavaScriptHeaderItem.forUrl("./js/dark-unica-mod.js"));
        response.render(JavaScriptHeaderItem.forUrl("./js/profit.js"));
    }
}
