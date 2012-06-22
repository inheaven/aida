package ru.inhell.aida.mybatis;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.06.12 16:37
 */
public class Activator implements BundleActivator{
    private BundleListener listener;

    @Override
    public void start(BundleContext bundleContext) throws Exception {
        listener = new Listener();

        bundleContext.addBundleListener(listener);
    }

    @Override
    public void stop(BundleContext bundleContext) throws Exception {
        bundleContext.removeBundleListener(listener);
    }
}
