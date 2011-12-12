package ru.inhell.aida.trader;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.AlphaTrader;
import ru.inhell.aida.entity.AlphaTraderData;
import ru.inhell.aida.entity.AlphaTraderFilter;
import ru.inhell.aida.util.DateUtil;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 13:47
 */
public class AlphaTraderBean {
    private final static String NS = AlphaTraderBean.class.getName();

    @Inject
    private SqlSessionManager sm;

    @SuppressWarnings({"unchecked"})
    public List<AlphaTrader> getAlphaTraders(){
        return sm.selectList(NS + ".selectAlphaTraders");
    }

    public AlphaTrader getAlphaTrader(Long id){
        return (AlphaTrader) sm.selectOne(NS + ".selectAlphaTrader", id);
    }

    @SuppressWarnings({"unchecked"})
    public List<AlphaTraderData> getAlphaTraderDatas(AlphaTraderFilter filter){
        return sm.selectList(NS + ".selectAlphaTraderDatas", filter);
    }

    public Long getAlphaTraderDatasCount(AlphaTraderFilter filter){
        return (Long) sm.selectOne(NS + ".selectAlphaTraderDatasCount", filter);
    }

    public void save(AlphaTraderData alphaTraderData){
        if (alphaTraderData.getId() == null){
            sm.insert(NS + ".insertAlphaTraderData", alphaTraderData);
        }else{
            sm.update(NS + ".updateAlphaTraderData", alphaTraderData);
        }
    }

    public void save(AlphaTrader alphaTrader){
        sm.update(NS + ".updateAlphaTrader", alphaTrader);
    }

    public AlphaTraderData getCurrentAlphaTraderData(Long alphaTraderId){
        return (AlphaTraderData) sm.selectOne(NS + ".selectCurrentAlphaTraderData", alphaTraderId);
    }

    public AlphaTraderData getAlphaTraderData(Long id){
        return (AlphaTraderData) sm.selectOne(NS + ".selectAlphaTraderData", id);
    }
}
