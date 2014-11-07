package ru.inheaven.aida.coin.web;

import de.agilecoders.wicket.core.markup.html.bootstrap.html.HtmlTag;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.model.Model;

import java.util.Locale;

import static org.apache.commons.lang3.RandomStringUtils.randomAlphanumeric;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 07.01.14 21:15
 */
public abstract class AbstractPage extends WebPage {
    private String title = randomAlphanumeric(32);

    protected AbstractPage() {
        HtmlTag htmlTag = new HtmlTag("html", new Locale("ru"));
        add(htmlTag);

        htmlTag.add(new Label("title", Model.of(getTitle())));

//        Navbar navbar = new Navbar("navbar");
//        navbar.setBrandName(Model.of("AIDA-COINS"));
//        add(navbar);
//
//        navbar.addComponents(NavbarComponents.transform(Navbar.ComponentPosition.LEFT,
//                new NavbarButton(TraderList.class, Model.of("Торговцы"))));
    }

    protected String getTitle(){
        return title;
    }
}
