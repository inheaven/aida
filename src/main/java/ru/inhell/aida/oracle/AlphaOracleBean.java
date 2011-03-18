package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.entity.Prediction;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.VectorForecastSSA;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

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
        executor.scheduleAtFixedRate(getCommand(new VectorForecast()), 0, 30, TimeUnit.SECONDS);
    }

    public void addListener(IAlphaOracleListener listener){
        listeners.add(listener);
    }

    private void predicted(String symbol, Prediction prediction, Date date, float price){
        for (IAlphaOracleListener listener : listeners){
            listener.predicted(symbol, prediction, date, price);
        }

        //todo store history
    }

    private VectorForecastSSA getVectorForecastSSA(VectorForecast vectorForecast){
        return vf1;
    }

    private Runnable getCommand(final VectorForecast vectorForecast){
        return new Runnable() {
            VectorForecastSSA vssa = getVectorForecastSSA(vectorForecast);
            float[] forecast = new float[vssa.forecastSize()];

            @Override
            public void run() {
                //load quotes
                List<Quote> quotes = quotesBean.getQuotes(vectorForecast.getSymbol(), vectorForecast.getN());
                float[] prices = QuoteUtil.getAveragePrices(quotes);

                //process vssa
                vssa.execute(prices, forecast);

                //save vector forecast history
                vectorForecastBean.save(vectorForecast, quotes, forecast);

                //predict
                Prediction prediction = null;

                if (VectorForecastUtil.isMin(forecast, vectorForecast.getN(), 5)){ //LONG
                    prediction = Prediction.LONG;
                }else if (VectorForecastUtil.isMax(forecast, vectorForecast.getN(), 5)){ //SHORT
                    prediction = Prediction.SHORT;
                }

                if (prediction != null) {
                    predicted(vectorForecast.getSymbol(), prediction,
                            DateUtil.nextMinute(quotes.get(vectorForecast.getN() - 1).getDate()),
                            forecast[vectorForecast.getN()]);
                }
            }
        };
    }
}
