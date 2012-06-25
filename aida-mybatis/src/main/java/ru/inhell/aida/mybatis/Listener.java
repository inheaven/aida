package ru.inhell.aida.mybatis;

import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.naming.InitialContext;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.06.12 16:46
 */
public class Listener implements BundleListener {
    private final static Logger log = LoggerFactory.getLogger(Listener.class);

    private final static String JNDI = "java:global/ru.inhell.aida.mybatis_1.0.0/SqlSessionFactoryBean";

    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (bundleEvent.getType() == BundleEvent.STARTED){
            try {
                SqlSessionFactoryBean sqlSessionFactoryBean = (SqlSessionFactoryBean) new InitialContext().lookup(JNDI);

                BundleWiring bundleWiring = (BundleWiring)bundleEvent.getBundle().adapt(BundleWiring.class);

                sqlSessionFactoryBean.addAnnotationMappers(bundleWiring.getClassLoader());
            } catch (Exception e) {
                log.error("Ошибка сканирования сервисов MyBatis", e);
            }
        }
    }
}
