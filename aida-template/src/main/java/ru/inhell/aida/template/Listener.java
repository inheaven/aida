package ru.inhell.aida.template;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.util.OsgiUtil;
import ru.inhell.aida.template.web.MenuService;

import javax.naming.InitialContext;
import javax.naming.NamingException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.06.12 14:49
 */
public class Listener implements BundleListener {
    private final static Logger log = LoggerFactory.getLogger(Listener.class);

    private final static String JNDI = "java:global/ru.inhell.aida.template_1.0.0/MenuService";

    @Override
    public void bundleChanged(BundleEvent event) {
        try {
            if (event.getType() == BundleEvent.INSTALLED || event.getType() == BundleEvent.UPDATED){
                getMenuService().addMenu(event);
            }else if (event.getType() == BundleEvent.UNINSTALLED){
                getMenuService().removeMenu(event);
            }

           log.info("Bundle {}: {}",event.getBundle().getSymbolicName(), OsgiUtil.getTypeString(event));

        } catch (NamingException e) {
            log.error("Ошибка добавления меню", e);
        }
    }

    private MenuService getMenuService() throws NamingException {
        return (MenuService) new InitialContext().lookup(JNDI);
    }
}
