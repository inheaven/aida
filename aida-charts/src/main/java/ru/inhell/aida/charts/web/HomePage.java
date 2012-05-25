package ru.inhell.aida.charts.web;

import org.apache.wicket.markup.html.WebPage;
import ru.inhell.aida.charts.highcharts.Line;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 26.05.12 0:16
 */
public class HomePage extends WebPage {
    public HomePage() {
        add(new Line("line"));
    }
}
