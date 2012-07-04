package ru.inhell.aida.common.service;

import org.wicketstuff.javaee.naming.IJndiNamingStrategy;
import ru.inhell.aida.common.util.OsgiUtil;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.07.12 18:29
 */
public class OsgiJndiNamingStrategy implements IJndiNamingStrategy{
    @Override
    public String calculateName(String ejbName, Class<?> ejbType) {
        return "java:global/" + OsgiUtil.getModuleName(ejbType) + "/" + (ejbName == null ? ejbType.getSimpleName() : ejbName);
    }
}
