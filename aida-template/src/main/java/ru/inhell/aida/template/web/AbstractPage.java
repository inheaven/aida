package ru.inhell.aida.template.web;

import org.apache.wicket.Page;
import org.apache.wicket.markup.head.CssHeaderItem;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.request.resource.CssResourceReference;
import org.apache.wicket.resource.loader.IStringResourceLoader;

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
        response.render(CssHeaderItem.forReference(new CssResourceReference(AbstractPage.class, "style.css")));
    }

    protected void populateMenu(){
        add( new ListView<Menu>("menu_list", MenuManager.getMenuList()) {
            @Override
            protected void populateItem(ListItem<Menu> item) {
                Menu menu = item.getModelObject();

                BookmarkablePageLink link = new BookmarkablePageLink<Void>("link", menu.getPage());
                link.add(new Label("label", AbstractPage.this.getString(menu.getPage(), menu.getTitleKey())));

                item.add(link);
            }
        });
    }

    protected String getString(Class<? extends Page> page, String key){
        List<IStringResourceLoader> loaders = getApplication().getResourceSettings().getStringResourceLoaders();

        for (IStringResourceLoader loader : loaders){
            String s = loader.loadStringResource(page, key, getLocale(), null, null);

//            getString() todo cache?

            if (s != null){
                return s;
            }

        }

        return "[" + key + "]";
    }

}
