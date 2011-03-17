package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.VectorForecastSSA;
import ru.inhell.aida.util.QuoteUtil;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 15:46
 */
@Singleton
public class AlphaOracleBean {
    @Inject
    private SqlSessionManager session;

    @Inject
    private QuotesBean quotesBean;

    @Inject
    private VectorForecastBean vectorForecastBean;

    private ScheduledThreadPoolExecutor executor;
    private Map<String, ScheduledFuture> scheduledFutures;

    private List<IAlphaOracleListener> listeners = new ArrayList<IAlphaOracleListener>();

    private VectorForecastSSA vf1 = new VectorForecastSSA(1000, 200, 14, 5);

    public void process(String symbol){
        executor.scheduleAtFixedRate(getCommand(symbol), 0, 30, TimeUnit.SECONDS);
    }

    public void addListener(IAlphaOracleListener listener){
        listeners.add(listener);
    }

    private void predicted(String symbol, AlphaOracleType type, Date date, float price){
        for (IAlphaOracleListener listener : listeners){
            listener.predicted(symbol, type, date, price);
        }
    }

    private VectorForecastSSA getVectorForecast(){
        return vf1;
    }

    private Runnable getCommand(final String symbol){
        return new Runnable() {
            VectorForecastSSA vectorForecast = getVectorForecast();
            float[] forecast = new float[vectorForecast.forecastSize()];

            @Override
            public void run() {
                //load quotes
                List<Quote> quotes = quotesBean.getQuotes(symbol, vectorForecast.getN());
                float[] prices = QuoteUtil.getAveragePrices(quotes);

                //process vssa
                vectorForecast.execute(prices, forecast);

                //find max & min

                //store result
            }
        };
    }




}
