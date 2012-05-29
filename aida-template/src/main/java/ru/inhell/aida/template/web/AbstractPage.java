package ru.inhell.aida.template.web;

import org.apache.wicket.markup.html.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.resource.PackageResourceReference;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.12 16:44
 */
public abstract class AbstractPage extends WebPage{
    protected AbstractPage() {
        populateMenu();
    }

    @Override
    public void renderHead(IHeaderResponse response) {
        response.renderCSSReference(new PackageResourceReference(AbstractPage.class, "style.css"));
    }

    protected void populateMenu(){
        List<Menu> menuList = MenuManager.getMenuList();

//        getApplication().getResourceReferenceRegistry().getResourceReference()
        //Iterator<IStringResourceLoader> iter = getStringResourceLoaders().iterator();



        for (Menu menu : menuList){


        }
    }
}
