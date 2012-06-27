package ru.inhell.aida.template;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.BundleListener;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.06.12 14:48
 */
public class Activator implements BundleActivator {
    private BundleListener listener;

    @Override
    public void start(BundleContext context) throws Exception {
        listener = new Listener();

        context.addBundleListener(listener);
    }

    @Override
    public void stop(BundleContext context) throws Exception {
        context.removeBundleListener(listener);
    }
}
