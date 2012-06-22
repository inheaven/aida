package ru.inhell.aida.common.util;

import javax.naming.InitialContext;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.06.12 17:49
 */
public class EjbUtil {
    @SuppressWarnings("unchecked")
    public static <T> T getEjb(String module, Class<T> ejbClass){
        try {
            return (T) new InitialContext().lookup("java:global/" + module + "/" + ejbClass.getSimpleName());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
