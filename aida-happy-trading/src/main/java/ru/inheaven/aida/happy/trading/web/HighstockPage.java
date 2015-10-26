package ru.inheaven.aida.happy.trading.web;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.JavaScriptHeaderItem;
import org.apache.wicket.resource.JQueryResourceReference;

/**
 * @author inheaven on 26.10.2015 21:48.
 */
public class HighstockPage extends BasePage {
    @Override
    public void renderHead(IHeaderResponse response) {
        response.render(JavaScriptHeaderItem.forReference(JQueryResourceReference.get()));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highstock.js"));
        response.render(JavaScriptHeaderItem.forUrl("./highstock/highcharts-3d.js"));
        response.render(JavaScriptHeaderItem.forUrl("./js/dark-unica-mod.js"));
    }
}
