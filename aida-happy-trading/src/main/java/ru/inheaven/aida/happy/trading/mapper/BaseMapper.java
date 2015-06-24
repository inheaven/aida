package ru.inheaven.aida.happy.trading.mapper;

import org.apache.ibatis.session.SqlSession;
import ru.inhell.aida.common.entity.AbstractEntity;

import javax.inject.Inject;

/**
 * @author inheaven on 24.06.2015 22:30.
 */
public class BaseMapper<T extends AbstractEntity> {
    @Inject
    private SqlSession sqlSession;

    protected SqlSession sqlSession(){
        return sqlSession;
    }

    public void save(T entity){
        if (entity.getId() == null){
            sqlSession.insert("insert" + entity.getClass().getSimpleName(), entity);
        }else{
            sqlSession.update("update" + entity.getClass().getSimpleName(), entity);
        }
    }

    public void delete(T entity){
        sqlSession.delete("delete" + entity.getClass().getSimpleName(), entity);
    }
}
