package ru.inhell.aida.mybatis;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.BundleListener;
import ru.inhell.aida.common.util.EjbUtil;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.06.12 16:46
 */
public class Listener implements BundleListener {
    @Override
    public void bundleChanged(BundleEvent bundleEvent) {
        if (bundleEvent.getType() == BundleEvent.STARTED){
            Bundle bundle = bundleEvent.getBundle();

            SqlSessionFactoryBean sqlSessionFactoryBean = EjbUtil.getEjb("ru.inhell.aida.mybatis_1.0.0",
                    SqlSessionFactoryBean.class);

            sqlSessionFactoryBean.addAnnotationMappers(bundle.getClass());

            System.out.println(bundleEvent);
        }
    }
}
