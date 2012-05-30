package ru.inhell.aida.charts.highcharts;

import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.odlabs.wiquery.core.IWiQueryPlugin;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.core.options.Options;
import org.odlabs.wiquery.ui.commons.WiQueryUIPlugin;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 26.05.12 0:19
 */
@WiQueryUIPlugin
public class Line extends WebMarkupContainer implements IWiQueryPlugin{
    private Options options;

    public Line(String id) {
        super(id);

        options = new Options(this);

        fillOptions();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
//        response.render(new PackageResourceReference(Line.class, "highcharts.js"));
    }

    @Override
    public JsStatement statement() {
        return new JsStatement().append("new Highcharts").chain("Chart", options.getJavaScriptOptions());
    }

    private void fillOptions(){
        Options chart = new Options();
        chart.putLiteral("renderTo", getMarkupId());
        chart.putLiteral("type", "line");

        Options series = new Options();
        series.putLiteral("name", "Hello");
        series.put("data", "[3.9, 4.2, 5.7, 8.5, 11.9, 15.2, 17.0, 16.6, 14.2, 10.3, 6.6, 4.8]");

        options.put("chart", chart.getJavaScriptOptions() + "");
        options.put("series", "[" + series.getJavaScriptOptions() + "]");

    }
}
