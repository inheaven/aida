package ru.inhell.aida.trader;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.AlphaTrader;
import ru.inhell.aida.entity.AlphaTraderData;

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
    public List<AlphaTraderData> getAlphaTraderDatas(Long alphaTraderId){
        return sm.selectList(NS + ".selectAlphaTraderDatas", alphaTraderId);
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
}
