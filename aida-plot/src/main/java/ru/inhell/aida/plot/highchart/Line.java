package ru.inhell.aida.plot.highchart;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.request.resource.PackageResourceReference;
import org.odlabs.wiquery.core.IWiQueryPlugin;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.core.options.Options;
import org.odlabs.wiquery.ui.commons.WiQueryUIPlugin;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.05.12 17:59
 */
@WiQueryUIPlugin
public class Line extends WebMarkupContainer implements IWiQueryPlugin{
    private Options options;

    public Line(String id) {
        super(id);

        options = new Options(this);
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderJavaScriptReference("http://ajax.googleapis.com/ajax/libs/jquery/1.7.2/jquery.min.js");
        response.renderJavaScriptReference(new PackageResourceReference(Line.class, "highchart"));
    }


    @Override
    public JsStatement statement() {
        return new JsStatement().chain("new Highcharts.Chart", options.getJavaScriptOptions());
    }
}
