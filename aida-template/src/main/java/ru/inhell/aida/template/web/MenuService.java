package ru.inhell.aida.template.web;

import org.apache.wicket.Page;
import org.osgi.framework.BundleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.service.IProcedure;
import ru.inhell.aida.common.util.OsgiUtil;

import javax.ejb.Singleton;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.12 17:27
 */
@Singleton
public class MenuService {
    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    private List<Menu> menuList = new ArrayList<>();

    @SuppressWarnings("unchecked")
    public void addMenu(BundleEvent event){
        OsgiUtil.scanAnnotation(event, TemplateMenu.class, new IProcedure<Class<?>>() {
            @Override
            public void apply(Class<?> c) {
                TemplateMenu tm = c.getAnnotation(TemplateMenu.class);
                menuList.add(new Menu(tm.order(), tm.groupKey(), tm.titleKey(), (Class<? extends Page>) c));

                log.info("Menu {} loaded.", c.getName());
            }
        });
    }

    public void removeMenu(BundleEvent event){
        //todo bundle wiring is null
    }

    public List<Menu> getMenuList(){
        return menuList;
    }
}
