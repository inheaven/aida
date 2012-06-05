package ru.inhell.aida.common.service;

import org.wicketstuff.javaee.naming.IJndiNamingStrategy;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.06.12 21:56
 */
public class GlassfishJndiNamingStrategy implements IJndiNamingStrategy {
    @Override
    public String calculateName(String ejbName, Class<?> ejbType) {
        return "java:module/" + (ejbName == null ? ejbType.getSimpleName() : ejbName);
    }
}
