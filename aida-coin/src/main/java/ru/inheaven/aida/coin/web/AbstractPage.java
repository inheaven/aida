package ru.inheaven.aida.coin.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.html.HtmlTag;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.Navbar;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarButton;
import de.agilecoders.wicket.core.markup.html.bootstrap.navbar.NavbarComponents;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.model.Model;

import java.util.Locale;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 07.01.14 21:15
 */
public abstract class AbstractPage extends WebPage {
    protected AbstractPage() {
        add(new HtmlTag("html", new Locale("ru")));

        Navbar navbar = new Navbar("navbar");
        navbar.brandName(Model.of("AIDA-CEX·IO"));
        add(navbar);

        navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.LEFT,
                new NavbarButton(TraderList.class, Model.of("Торговцы"))));
    }
}
