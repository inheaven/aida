package ru.inheaven.aida.happy.trading.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.html.HtmlTag;
import org.apache.wicket.markup.html.WebPage;

import java.util.Locale;

/**
 * @author inheaven on 18.06.2015 20:28.
 */
public class BasePage extends WebPage{
    public BasePage() {
        HtmlTag htmlTag = new HtmlTag("html", new Locale("ru"));
        add(htmlTag);
    }
}
