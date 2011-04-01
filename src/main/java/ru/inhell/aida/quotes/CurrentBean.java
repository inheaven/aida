package ru.inhell.aida.quotes;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.Current;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.04.11 17:14
 */
public class CurrentBean {
    private final static String NS = CurrentBean.class.getName();

    @Inject
    private SqlSessionManager sm;

    public Current getCurrent(String symbol){
        return (Current) sm.selectOne(NS + ".selectCurrent", symbol);
    }
}
