package ru.inhell.aida.template.web;

import org.apache.wicket.Page;
import org.reflections.Reflections;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.12 17:27
 */
public class MenuManager {
    private final static MenuManager instance = new MenuManager();

    private List<Menu> menuList;

    @SuppressWarnings("unchecked")
    public MenuManager() {
        menuList = new ArrayList<>();

        Reflections reflections = new Reflections("ru.inhell.aida");

        Set<Class<?>> classes = reflections.getTypesAnnotatedWith(TemplateMenu.class);

        for (Class<?> c : classes){
            TemplateMenu templateMenu = c.getAnnotation(TemplateMenu.class);

            menuList.add(new Menu(templateMenu.order(), templateMenu.groupKey(), (Class<? extends Page>) c));
        }

        menuList = Collections.unmodifiableList(menuList);
    }

    public static List<Menu> getMenuList(){
        return instance.menuList;
    }
}
