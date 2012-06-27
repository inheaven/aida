package ru.inhell.aida.mybatis;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.06.12 16:46
 */
public class Listener implements BundleListener {
    private final static Logger log = LoggerFactory.getLogger(Listener.class);

    private final static String JNDI = "java:global/ru.inhell.aida.mybatis_1.0.0/SqlSessionFactoryService";

    @Override
    public void bundleChanged(BundleEvent event) {
        String name = event.getBundle().getSymbolicName();

        if (event.getType() == BundleEvent.STARTED && name.contains("aida") && !name.contains("mybatis")){
            try {
                SqlSessionFactoryService sqlSessionFactoryBean = (SqlSessionFactoryService) new InitialContext().lookup(JNDI);
                sqlSessionFactoryBean.addAnnotationMappers(event);
            } catch (Exception e) {
                log.error("Ошибка сканирования сервисов MyBatis", e);
            }
        }
    }
}
