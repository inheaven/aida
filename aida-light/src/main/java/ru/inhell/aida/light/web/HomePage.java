package ru.inhell.aida.light.web;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import ru.inhell.aida.light.jna.Yocto;

/**
 * @author Anatoly Ivanov java@inheaven.ru
 *         Date: 02.05.12 23:57
 */
public class HomePage extends WebPage {
    public HomePage() {
        add(new Form("form"){
            @Override
            protected void onSubmit() {
                System.out.println(Yocto.INSTANCE.yRegisterHub("usb", ""));
            }
        });
    }
}
