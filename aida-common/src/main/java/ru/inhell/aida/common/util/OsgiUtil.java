package ru.inhell.aida.common.util;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleEvent;
import org.osgi.framework.FrameworkUtil;
import org.osgi.framework.wiring.BundleWiring;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.service.IProcedure;

import java.lang.annotation.Annotation;
import java.util.Collection;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.06.12 15:02
 */
public class OsgiUtil {
    private final static Logger log = LoggerFactory.getLogger(OsgiUtil.class);

    public static void scanAnnotation(BundleEvent event, Class<? extends Annotation> annotationClass, IProcedure<Class<?>> procedure){
        BundleWiring bundleWiring = (BundleWiring)event.getBundle().adapt(BundleWiring.class);

        if (bundleWiring != null) {
            Collection<String> res = bundleWiring.listResources("ru/inhell/aida/", "*", BundleWiring.LISTRESOURCES_RECURSE);

            if (res != null) {
                for (String r : res){
                    if (r.contains(".class")){
                        try{
                            Class<?> c = bundleWiring.getClassLoader().loadClass(r.replace("/", ".").replace(".class", ""));

                            Annotation annotation = c.getAnnotation(annotationClass);

                            if (annotation != null){
                                procedure.apply(c);
                            }
                        } catch (ClassNotFoundException e) {
                            log.error("Ошибка сканирования", e);
                        }
                    }
                }
            }
        }else {
            log.warn("BundleWiring for {} is null", annotationClass);
        }
    }

    public static String getTypeString(BundleEvent event){
        String type = "NULL";

        switch (event.getType()){
            case BundleEvent.INSTALLED:
                type = "INSTALLED";
                break;
            case BundleEvent.LAZY_ACTIVATION:
                type = "LAZY_ACTIVATION";
                break;
            case BundleEvent.RESOLVED:
                type = "RESOLVED";
                break;
            case BundleEvent.STARTED:
                type = "STARTED";
                break;
            case BundleEvent.STARTING:
                type = "STARTING";
                break;
            case BundleEvent.STOPPED:
                type = "STOPPED";
                break;
            case BundleEvent.STOPPING:
                type = "STOPPING";
                break;
            case BundleEvent.UNINSTALLED:
                type = "UNINSTALLED";
                break;
            case BundleEvent.UNRESOLVED:
                type = "UNRESOLVED";
                break;
            case BundleEvent.UPDATED:
                type = "UPDATED";
                break;
        }

        return type;
    }

    public static String getModuleName(Bundle bundle){
        return bundle.getSymbolicName() + "_" + bundle.getVersion();
    }

    public static String getModuleName(Class _class){
        return getModuleName(FrameworkUtil.getBundle(_class));
    }
}
