package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.AlphaOracleFilter;
import ru.inhell.aida.util.DateUtil;

import javax.management.monitor.StringMonitor;
import java.util.Date;
import java.util.HashMap;
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

    @SuppressWarnings({"unchecked"})
    public List<AlphaOracleData> getAlphaOracleDatas(final Long alphaOracleId, final Date fromDate){
        return getAlphaOracleDatas(new AlphaOracleFilter(alphaOracleId, fromDate, DateUtil.nowMsk()));
    }

    @SuppressWarnings({"unchecked"})
    public List<AlphaOracleData> getAlphaOracleDatas(AlphaOracleFilter filter){
        return session.selectList(NS + ".selectAlphaOracleDatas", filter);
    }

    public void save(AlphaOracle alphaOracle){
        if (alphaOracle.getId() == null) {
            session.insert(NS + ".insertAlphaOracle", alphaOracle);
        } else {
            session.update(NS + ".updateAlphaOracle", alphaOracle);
        }
    }

    public void save(AlphaOracleData alphaOracleData){
        session.insert(NS + ".insertAlphaOracleData", alphaOracleData);
    }

    public boolean isAlphaOracleDataExists(final Long alphaOracleId, final Date date){
        return (Boolean)session.selectOne(NS + ".selectIsAlphaOracleDataExists", new HashMap<String, Object>(){{
            put("alphaOracleId", alphaOracleId);
            put("date", date);
        }});
    }
}
