package ru.inhell.aida.mybatis;

import org.apache.ibatis.session.SqlSession;

import javax.ejb.EJB;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.05.12 18:10
 */
public abstract class AbstractMyBatisBean {
    @EJB
    private SqlSessionFactoryBean sqlSessionFactoryBean;

    public SqlSession sqlSession(){
        return sqlSessionFactoryBean.getSessionManager();
    }
}
