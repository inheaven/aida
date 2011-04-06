package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 15:46
 */
@Singleton
public class AlphaOracleService {
    private final static Logger log = LoggerFactory.getLogger(AlphaOracleService.class);

    private final static int CORE_POOL_SIZE = 2;
    private final static int PERIOD = 30;

    @Inject
    private QuotesBean quotesBean;

    @Inject
    private VectorForecastBean vectorForecastBean;

    @Inject
    private AlphaOracleBean alphaOracleBean;

    private Map<Long, Date> predictedTime = new ConcurrentHashMap<Long, Date>();

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    private Map<Long, ScheduledFuture> scheduledFutures = new LinkedHashMap<Long, ScheduledFuture>();

    private List<IAlphaOracleListener> listeners = new ArrayList<IAlphaOracleListener>();

    public ScheduledFuture process(Long alphaOracleId){
        AlphaOracle alphaOracle = alphaOracleBean.getAlphaOracle(alphaOracleId);

        ScheduledFuture f = scheduledFutures.get(alphaOracleId);

        if (f == null){
            f = executor.scheduleAtFixedRate(getCommand(alphaOracle), 0, PERIOD, TimeUnit.SECONDS);

            scheduledFutures.put(alphaOracleId, f);
        }

        return f;
    }

    public Collection<ScheduledFuture> getScheduledFutures(){
        return scheduledFutures.values();
    }

    public void addListener(IAlphaOracleListener listener){
        listeners.add(listener);
    }

    private void predicted(AlphaOracle alphaOracle, AlphaOracleData.PREDICTION prediction, List<Quote> quotes,
                           float[] forecast){
        String symbol = alphaOracle.getVectorForecast().getSymbol();
        int n = alphaOracle.getVectorForecast().getN();

        for (IAlphaOracleListener listener : listeners){
            if (prediction != null) {
                log.info("AlphaOracle" +alphaOracle.getId() + ". " + symbol + ", " + prediction.name() + ", " +
                        quotes.get(n-1).getDate() + ", " + forecast[n-1]);
            }

            try {
                listener.predicted(alphaOracle, prediction, quotes, forecast);
            } catch (Exception e) {
                log.error("ошибка слушателя", e);
            }
        }
    }

    private Runnable getCommand(final AlphaOracle alphaOracle){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Date last = vectorForecastBean.getLastVectorForecastDataDate(alphaOracle.getVectorForecast().getId());
                    Date lastQuote = quotesBean.getLastQuoteDate(alphaOracle.getVectorForecast().getSymbol());

                    int n = alphaOracle.getVectorForecast().getN();
                    int d = (int) DateUtil.getMinuteShift(lastQuote, last);

                    predict(alphaOracle, d > 0 && d < n ? d : 60);
//                    predict(alphaOracle, 1);
                } catch (Exception e) {
                    log.error("Ошибка предсказателя", e);
                }
            }
        };
    }

    public void predict(final AlphaOracle alphaOracle, int count){
        VectorForecast vf = alphaOracle.getVectorForecast();

        VectorForecastSSA vssa = vectorForecastBean.getVectorForecastSSA(vf);
        float[] forecast = new float[vssa.forecastSize()];

        List<Quote> allQuotes = quotesBean.getQuotes(vf.getSymbol(), vf.getN() + count);

        //skip execution
        Date date = predictedTime.get(alphaOracle.getId());
        Date quoteDate = allQuotes.get(allQuotes.size()-1).getDate();
        if (date != null && date.equals(quoteDate)){
            return;
        }else{
            predictedTime.put(alphaOracle.getId(), quoteDate);
        }

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

            if (VectorForecastUtil.isMin(forecast, vf.getN(), vf.getM())
                    || VectorForecastUtil.isMin(forecast, vf.getN()-1, vf.getM())
                    || VectorForecastUtil.isMin(forecast, vf.getN()+1, vf.getM())){ //LONG
                prediction = AlphaOracleData.PREDICTION.LONG;
            }else if (VectorForecastUtil.isMax(forecast, vf.getN(), vf.getM())
                    || VectorForecastUtil.isMax(forecast, vf.getN()-1, vf.getM())
                    || VectorForecastUtil.isMax(forecast, vf.getN()+1, vf.getM())){ //SHORT
                prediction = AlphaOracleData.PREDICTION.SHORT;
            }

            if (prediction != null) {
                //save prediction
                try {
                    alphaOracleBean.save(new AlphaOracleData(alphaOracle.getId(), quotes.get(vf.getN() - 1).getDate(),
                            forecast[vf.getN() - 1], prediction, DateUtil.now()));
                } catch (Exception e) {
                    //skip duplicates
                }
            }

            //fire listeners
            predicted(alphaOracle, prediction, quotes, forecast);
        }
    }
}
