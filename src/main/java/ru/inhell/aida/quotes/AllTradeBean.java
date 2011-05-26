package ru.inhell.aida.quotes;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.AllTrade;
import ru.inhell.aida.entity.AllTradeFilter;

import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 26.05.11 22:04
 */
public class AllTradeBean {
    private final static String NS = AllTradeBean.class.getName();

    @Inject
    private SqlSessionManager sm;

    @SuppressWarnings({"unchecked"})
    public List<AllTrade> getAllTrades(String symbol, int start, int count){
        return sm.selectList(NS + ".selectAllTrades", new AllTradeFilter(symbol, start, count));
    }

    public Long getAllTradesCount(String symbol){
        return (Long) sm.selectOne(NS + ".selectAllTradesCount", symbol);
    }
}
