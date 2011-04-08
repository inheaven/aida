package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.Aida;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.VectorForecastSSA;
import ru.inhell.aida.ssa.VectorForecastSSAService;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
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
    private final static int PERIOD = 20;
    private final static int UPDATE_COUNT = 4;

    @Inject
    private QuotesBean quotesBean;

    @Inject
    private VectorForecastBean vectorForecastBean;

    @Inject
    private AlphaOracleBean alphaOracleBean;

    @Inject
    private VectorForecastSSAService vectorForecastSSAService;

    private Map<Long, Date> predictedTime = new ConcurrentHashMap<Long, Date>();
    private Map<Long, Integer> predictedTimeCount = new ConcurrentHashMap<Long, Integer>();

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
            } catch (Throwable e) {
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

                    //skip execution
                    Date date = predictedTime.get(alphaOracle.getId());
                    if (date != null && date.equals(lastQuote)){
                        Integer updateCount = predictedTimeCount.get(alphaOracle.getId());

                        if (updateCount > UPDATE_COUNT){
                            return;
                        }else{
                            predictedTimeCount.put(alphaOracle.getId(), updateCount + 1);
                        }
                    }else{
                        predictedTime.put(alphaOracle.getId(), lastQuote);
                        predictedTimeCount.put(alphaOracle.getId(), 0);
                    }

                    predict(alphaOracle, DateUtil.isSameDay(last, lastQuote)
                            ? (int) DateUtil.getMinuteShift(lastQuote, last) : 1);
                } catch (Throwable e) {
                    log.error("Ошибка предсказателя", e);
                }
            }
        };
    }

    public void predict(final AlphaOracle alphaOracle, int count) throws NotBoundException, RemoteException {
        VectorForecast vf = alphaOracle.getVectorForecast();

        List<Quote> allQuotes = quotesBean.getQuotes(vf.getSymbol(), vf.getN() + count);

        float[] forecast;

        for (int index = 0; index < count; ++index) {
            //load quotes
            List<Quote> quotes = allQuotes.subList(index, vf.getN() + index);

            float[] prices = QuoteUtil.getAveragePrices(quotes);

            //process vssa
            if (Aida.getProperty("use_remote_vssa").equals("true")) {
                forecast = vectorForecastSSAService.executeRemote(vf.getN(), vf.getL(), vf.getP(), vf.getM(), prices);
            }else{
                forecast = vectorForecastSSAService.execute(vf.getN(), vf.getL(), vf.getP(), vf.getM(), prices);
            }

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
