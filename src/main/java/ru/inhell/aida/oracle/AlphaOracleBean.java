package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.03.11 16:50
 */
public class AlphaOracleBean {
    private final static String NS = AlphaOracleBean.class.getName();

    @Inject
    private SqlSessionManager session;

    @Inject
    private VectorForecastBean vectorForecastBean;

    public AlphaOracle getAlphaOracle(Long id){
        return (AlphaOracle) session.selectOne(NS + ".selectAlphaOracle", id);
    }

    @SuppressWarnings({"unchecked"})
    public List<AlphaOracle> getAlphaOracles(){
        return session.selectList(NS + ".selectAlphaOracles");
    }

    public void save(AlphaOracle alphaOracle){
        session.insert(NS + ".insertAlphaOracle", alphaOracle);
    }

    public void save(AlphaOracleData alphaOracleData){
        session.insert(NS + ".insertAlphaOracleData", alphaOracleData);
    }
}
