package ru.inhell.aida.mybatis;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.12.10 17:05
 */
public class SqlSessionFactory {
    private static Logger log = LoggerFactory.getLogger(SqlSessionFactory.class);

    public static SqlSessionManager sessionManager;

    public static SqlSessionManager getSessionManager() {
        if (sessionManager == null){
            try {
                sessionManager = SqlSessionManager.newInstance(Resources.getResourceAsReader("mybatis-config.xml"));
            } catch (IOException e) {
               log.error("Файл конфигурации mybatis-config.xml не найден.", e);
            }
        }

        return sessionManager;
    }
}
