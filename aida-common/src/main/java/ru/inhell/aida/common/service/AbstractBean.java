package ru.inhell.aida.common.service;

import org.apache.ibatis.session.SqlSession;
import ru.inhell.aida.common.mybatis.SqlSessionFactoryBean;

import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.05.12 18:10
 */
public abstract class AbstractBean {
    @EJB
    private SqlSessionFactoryBean sqlSessionFactoryBean;

    public SqlSession sqlSession(){
        return sqlSessionFactoryBean.getSessionManager();
    }
}
