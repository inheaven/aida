package ru.inhell.aida.matrix.service;

import ru.inhell.aida.common.entity.AllTrades;
import ru.inhell.aida.common.entity.EntityWrapper;
import ru.inhell.aida.common.mybatis.XmlMapper;
import ru.inhell.aida.common.service.AbstractBean;

import javax.ejb.Stateless;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 26.05.11 22:04
 */
@XmlMapper
@Stateless
public class AllTradesBean extends AbstractBean {

    public List<AllTrades> getAllTrades(String symbol, Date start, Date end){
        return sqlSession().selectList(".selectAllTrades", new EntityWrapper()
                .add("symbol", symbol)
                .add("start", start)
                .add("end", end));
    }
}
