package ru.inhell.aida.matrix.service;

import ru.inhell.aida.common.entity.FilterWrapper;
import ru.inhell.aida.common.mybatis.XmlMapper;
import ru.inhell.aida.common.service.AbstractBean;
import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.05.12 17:27
 */
@XmlMapper
@Stateless
public class MatrixBean extends AbstractBean{
    public List<Matrix> getMatrixList(String symbol, Date start, Date end, MatrixPeriodType type){
        return sqlSession().selectList("selectMatrixList",  new FilterWrapper()
                .add("symbol", symbol)
                .add("start", start)
                .add("end", end)
                .add("type", type.name()));
    }
}
