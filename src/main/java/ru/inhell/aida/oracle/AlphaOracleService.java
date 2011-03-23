package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.VectorForecastSSA;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 15:46
 */
@Singleton
public class AlphaOracleService {
    private final static int CORE_POOL_SIZE = 2;
    private final static int PERIOD = 30;

    @Inject
    private QuotesBean quotesBean;

    @Inject
    private VectorForecastBean vectorForecastBean;

    @Inject
    private AlphaOracleBean alphaOracleBean;

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    private Map<Long, ScheduledFuture> scheduledFutures = new LinkedHashMap<Long, ScheduledFuture>();

    private List<IAlphaOracleListener> listeners = new ArrayList<IAlphaOracleListener>();

    public ScheduledFuture process(Long alphaOracleId){
        AlphaOracle alphaOracle = alphaOracleBean.getAlphaOracle(alphaOracleId);

        ScheduledFuture f = executor.scheduleAtFixedRate(getCommand(alphaOracle), 0, PERIOD, TimeUnit.SECONDS);

        scheduledFutures.put(alphaOracleId, f);

        return f;
    }

    public Collection<ScheduledFuture> getScheduledFutures(){
        return scheduledFutures.values();
    }

    public void addListener(IAlphaOracleListener listener){
        listeners.add(listener);
    }

    private void predicted(String symbol, AlphaOracleData.PREDICTION prediction, Date date, float price){
        for (IAlphaOracleListener listener : listeners){
            listener.predicted(symbol, prediction, date, price);
        }
    }

    private Runnable getCommand(final AlphaOracle alphaOracle){
        return new Runnable() {
            VectorForecast vectorForecast = alphaOracle.getVectorForecast();

            VectorForecastSSA vssa = vectorForecastBean.getVectorForecastSSA(vectorForecast);
            float[] forecast = new float[vssa.forecastSize()];

            @Override
            public void run() {
                predict(alphaOracle, 1);
            }
        };
    }

    public void predict(final AlphaOracle alphaOracle, int count){
        VectorForecast vf = alphaOracle.getVectorForecast();

        VectorForecastSSA vssa = vectorForecastBean.getVectorForecastSSA(vf);
        float[] forecast = new float[vssa.forecastSize()];

        List<Quote> allQuotes = quotesBean.getQuotes(vf.getSymbol(), vf.getN() + count);

        for (int index = 0; index < count; ++index) {
            //load quotes
            List<Quote> quotes = allQuotes.subList(index, vf.getN() + index);

            float[] prices = QuoteUtil.getAveragePrices(quotes);

            //process vssa
            vssa.execute(prices, forecast);

            //save vector forecast history
            try {
                vectorForecastBean.save(vf, quotes, forecast);
            } catch (Exception e) {
                //skip duplicates
            }

            //predict
            AlphaOracleData.PREDICTION prediction = null;

            if (VectorForecastUtil.isMin(forecast, vf.getN(), vf.getM())){ //LONG
                prediction = AlphaOracleData.PREDICTION.LONG;
            }else if (VectorForecastUtil.isMax(forecast, vf.getN(), vf.getM())){ //SHORT
                prediction = AlphaOracleData.PREDICTION.SHORT;
            }

            if (prediction != null) {
                //save prediction
                try {
                    alphaOracleBean.save(new AlphaOracleData(alphaOracle.getId(), quotes.get(vf.getN()-1).getDate(),
                            forecast[vf.getN()-1], prediction, DateUtil.now()));
                } catch (Exception e) {
                    //skip duplicates
                }

                //fire listeners
                predicted(vf.getSymbol(), prediction, quotes.get(vf.getN() - 1).getDate(), forecast[vf.getN() - 1]);
            }
        }
    }
}
