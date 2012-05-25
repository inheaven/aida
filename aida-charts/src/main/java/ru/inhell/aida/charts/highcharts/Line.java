package ru.inhell.aida.charts.highcharts;

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.odlabs.wiquery.core.IWiQueryPlugin;
import org.odlabs.wiquery.core.javascript.JsStatement;
import org.odlabs.wiquery.ui.commons.WiQueryUIPlugin;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 26.05.12 0:19
 */
@WiQueryUIPlugin
public class Line extends WebMarkupContainer implements IWiQueryPlugin{
    public Line(String id) {
        super(id);
    }

    @Override
    public JsStatement statement() {
        return new JsStatement().append("HelloWorld!");
    }
}
