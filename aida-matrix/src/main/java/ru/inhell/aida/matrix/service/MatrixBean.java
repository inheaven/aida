package ru.inhell.aida.matrix.service;

import ru.inhell.aida.common.entity.EntityWrapper;
import ru.inhell.aida.mybatis.AbstractMyBatisBean;
import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;
import ru.inhell.aida.mybatis.XmlMapper;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.05.12 17:27
 */
@XmlMapper
@Stateless
public class MatrixBean extends AbstractMyBatisBean {
    public List<Matrix> getMatrixListFromAllTrades(String symbol, Date start, Date end, MatrixPeriodType periodType){
        return sqlSession().selectList("selectMatrixListFromAllTrades",  new EntityWrapper()
                .add("symbol", symbol)
                .add("start", start)
                .add("end", end)
                .add("period_type", periodType.name()));
    }

    public Matrix getMatrix(Matrix matrix, MatrixPeriodType periodType){
        return sqlSession().selectOne("selectMatrix", EntityWrapper.of(matrix).add("period_type", periodType.name()));
    }

    public Long getMatrixId(Matrix matrix, MatrixPeriodType periodType){
        return sqlSession().selectOne("selectMatrixId", EntityWrapper.of(matrix).add("period_type", periodType.name()));
    }

    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void save(Matrix matrix, MatrixPeriodType periodType){
        sqlSession().insert("insertMatrix", EntityWrapper.of(matrix).add("period_type", periodType.name()));
    }

    public List<Matrix> getMatrixList(String symbol, Date start, Date end, MatrixPeriodType periodType){
        return sqlSession().selectList("selectMatrixList",  new EntityWrapper()
                .add("symbol", symbol)
                .add("start", start)
                .add("end", end)
                .add("period_type", periodType.name()));
    }
}
