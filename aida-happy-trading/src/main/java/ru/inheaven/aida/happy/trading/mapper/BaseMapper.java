package ru.inheaven.aida.happy.trading.mapper;

import org.apache.ibatis.session.SqlSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.entity.AbstractEntity;

import javax.inject.Inject;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * @author inheaven on 24.06.2015 22:30.
 */
public class BaseMapper<T extends AbstractEntity> {
    private Logger log = LoggerFactory.getLogger(getClass());

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

    private Executor executor = Executors.newCachedThreadPool();

    public void asyncSave(T entity){
        executor.execute(() -> {
            try {
                save(entity);
            } catch (Exception e) {
                log.error("error save -> ", e);
            }
        });
    }

    public void delete(T entity){
        sqlSession.delete("delete" + entity.getClass().getSimpleName(), entity);
    }
}
