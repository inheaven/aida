package ru.inhell.aida.plot.web;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;
import org.odlabs.wiquery.ui.datepicker.DatePicker;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.05.12 16:04
 */
public class HomePage extends WebPage{
    public HomePage() {
        DatePicker datePicker = new DatePicker<Date>("date", Model.of(new Date()));
        add(datePicker);
    }
}
