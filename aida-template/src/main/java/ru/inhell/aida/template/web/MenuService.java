package ru.inhell.aida.template.web;

import org.apache.wicket.Page;
import org.osgi.framework.BundleEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.service.IProcedure;
import ru.inhell.aida.common.util.OsgiUtil;

import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.12 17:27
 */
@Singleton
@TransactionManagement(value = TransactionManagementType.BEAN)
public class MenuService {
    private static final Logger log = LoggerFactory.getLogger(MenuService.class);

    private Map<Class, Menu> menuMap = new HashMap<>();

    @SuppressWarnings("unchecked")
    public void addMenu(BundleEvent event){
        OsgiUtil.scanAnnotation(event, TemplateMenu.class, new IProcedure<Class<?>>() {
            @Override
            public void apply(Class<?> c) {
                TemplateMenu tm = c.getAnnotation(TemplateMenu.class);
                menuMap.put(c, new Menu(tm.order(), tm.groupKey(), tm.titleKey(), (Class<? extends Page>) c));

                log.info("Menu {} loaded.", c.getName());
            }
        });
    }

    public void removeMenu(BundleEvent event){
        OsgiUtil.scanAnnotation(event, TemplateMenu.class, new IProcedure<Class<?>>() {
            @Override
            public void apply(Class<?> c) {
                menuMap.remove(c);

                log.info("Menu {} removed.", c.getName());
            }
        });
    }

    public Map<Class, Menu> getMenuMap(){
        return menuMap;
    }
}
