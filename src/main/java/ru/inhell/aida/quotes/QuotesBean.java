package ru.inhell.aida.quotes;

import com.google.inject.Inject;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.QuoteFilter;

import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.03.11 13:28
 */
public class QuotesBean {
    private final static String NS = QuotesBean.class.getName();

    @Inject
    private SqlSessionManager sm;

    @SuppressWarnings({"unchecked"})
    public List<Quote> getQuotes(String symbol, int count){
        return sm.selectList(NS + ".selectQuotes", new QuoteFilter(symbol, count));
    }

    @SuppressWarnings({"unchecked"})
    public List<Quote> getQuotes(String symbol, Date startDate, Date endDate){
        return sm.selectList(NS + ".selectQuotesInterval", new QuoteFilter(symbol, startDate, endDate));
    }

    public Quote getQuote(String symbol, Date date){
        return (Quote) sm.selectOne(NS + ".selectQuote", new QuoteFilter(symbol, date));
    }

    public Date getLastQuoteDate(String symbol){
        return (Date) sm.selectOne(NS + ".selectLastQuoteDate", symbol);
    }

    public void save(Quote quote){
        sm.insert(NS + ".insertQuote", quote);
    }
}
