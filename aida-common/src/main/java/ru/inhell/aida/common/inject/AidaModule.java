package ru.inhell.aida.common.inject;

import com.google.inject.AbstractModule;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.common.mybatis.SqlSessionFactory;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 15:47
 */
public class AidaModule extends AbstractModule{
    @Override
    protected void configure() {
        bind(SqlSessionManager.class).toInstance(SqlSessionFactory.getSessionManager());
    }
}
