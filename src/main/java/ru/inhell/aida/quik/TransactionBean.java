package ru.inhell.aida.quik;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.Transaction;
import ru.inhell.aida.entity.TransactionFilter;


import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 01.06.11 22:00
 */
public class TransactionBean {
    private final static String NS = TransactionBean.class.getName();

    @Inject
    private SqlSessionManager sm;

    @SuppressWarnings({"unchecked"})
    public List<Transaction> getTransactions(String symbol, String date){
        return sm.selectList(NS + ".selectTransactions", new TransactionFilter(symbol, date));
    }

}
