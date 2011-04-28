package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.Aida;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.quotes.CurrentBean;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.RemoteVSSAException;
import ru.inhell.aida.ssa.VectorForecastSSAService;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
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

    private final static int CORE_POOL_SIZE = 3;
    private final static int PERIOD = 30;
    private final static int UPDATE_COUNT = 3;

    private final static boolean USE_REMOTE = Aida.getProperty("use_remote_vssa").equals("true");

    public static final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

    @Inject
    private QuotesBean quotesBean;

    @Inject
    private VectorForecastBean vectorForecastBean;

    @Inject
    private AlphaOracleBean alphaOracleBean;

    @Inject
    private VectorForecastSSAService vectorForecastSSAService;

    @Inject
    private CurrentBean currentBean;

    private Map<Long, Date> predictedTime = new ConcurrentHashMap<Long, Date>();
    private Map<Long, Integer> predictedTimeCount = new ConcurrentHashMap<Long, Integer>();

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    private Map<Long, ScheduledFuture> scheduledFutures = new LinkedHashMap<Long, ScheduledFuture>();

    private List<IAlphaOracleListener> listeners = new ArrayList<IAlphaOracleListener>();

    public ScheduledFuture process(AlphaOracle alphaOracle){
        ScheduledFuture f = scheduledFutures.get(alphaOracle.getId());

        if (f == null){
            f = executor.scheduleAtFixedRate(getCommand(alphaOracle), 0, PERIOD, TimeUnit.SECONDS);

            scheduledFutures.put(alphaOracle.getId(), f);
        }

        return f;
    }

    public Collection<ScheduledFuture> getScheduledFutures(){
        return scheduledFutures.values();
    }

    public void addListener(IAlphaOracleListener listener){
        listeners.add(listener);
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
                    Integer updateCount = predictedTimeCount.get(alphaOracle.getId());

                    if (date != null && date.equals(lastQuote)){
                        if (updateCount > UPDATE_COUNT){
                            return;
                        }else{
                            predictedTimeCount.put(alphaOracle.getId(), updateCount + 1);
                        }
                    }else{
                        predictedTime.put(alphaOracle.getId(), lastQuote);
                        predictedTimeCount.put(alphaOracle.getId(), 0);
                    }

                    int count = DateUtil.isSameDay(last, lastQuote) ? DateUtil.getMinuteShift(lastQuote, last) : 1;

                    predict(alphaOracle, count, USE_REMOTE, true);
                } catch (Throwable e) {
                    log.error("Ошибка предсказателя", e);
                }
            }
        };
    }

    public void predict(final AlphaOracle alphaOracle, int count, boolean skipIfOracleExists, boolean useRemote)
            throws  RemoteVSSAException {
        VectorForecast vf = alphaOracle.getVectorForecast();

        //загружаем все котировки
        List<Quote> allQuotes = quotesBean.getQuotes(vf.getSymbol(), vf.getN() + count);

        float[] forecast;

        for (int index = 0; index < count; ++index) {
            //текущий список котировок
            List<Quote> quotes = allQuotes.subList(index, vf.getN() + index);

            //текущая дата
            Date date = quotes.get(quotes.size()-1).getDate();

            //текущая цена
            float currentPrice = DateUtil.getAbsMinuteShiftMsk(date) < 2
                        ? currentBean.getCurrent(vf.getSymbol()).getPrice()
                        : quotes.get(quotes.size()-1).getClose();

            //пропускаем если уже есть запись предсказания в базе данных
            if (skipIfOracleExists && alphaOracleBean.isAlphaOracleDataExists(alphaOracle.getId(), date)){
                continue;
            }

            float[] prices;

            //тип цены
            switch (alphaOracle.getPriceType()){
                case AVERAGE:
                    prices = QuoteUtil.getAveragePrices(quotes);
                    break;
                case CLOSE:
                    prices = QuoteUtil.getClosePrices(quotes);
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            //алгоритм векторного прогнозирования
            if (useRemote) {
                forecast = vectorForecastSSAService.executeRemote(vf.getN(), vf.getL(), vf.getP(), vf.getM(), prices);
            }else{
                forecast = vectorForecastSSAService.execute(vf.getN(), vf.getL(), vf.getP(), vf.getM(), prices);
            }

            Prediction prediction = null;

            //предсказание на текущую дату
            if (alphaOracle.getStopCount() < alphaOracle.getMaxStopCount()) {
                if (StopType.F_STOP.equals(alphaOracle.getStopType()) && alphaOracle.isInMarket()){
                    float stopPrice = alphaOracle.getStopPrice();

                    if (Prediction.LONG.equals(alphaOracle.getPrediction()) && currentPrice < stopPrice){
                        //защитная приостановка - продажа
                        prediction = Prediction.STOP_SELL;
                    }else if (Prediction.SHORT.equals(alphaOracle.getPrediction()) &&  currentPrice > stopPrice){
                        //защитная приостановка - покупка
                        prediction = Prediction.STOP_BUY;
                    }
                }

                if (prediction == null) {
                    if (VectorForecastUtil.isMin(forecast, vf.getN(), vf.getM())
                            || VectorForecastUtil.isMin(forecast, vf.getN()-1, vf.getM()-1)
                            || VectorForecastUtil.isMin(forecast, vf.getN()+1, vf.getM()-1)){
                        //уже в позиции
                        if (Prediction.LONG.equals(alphaOracle.getPrediction())){
                            return;
                        }

                        //длинная покупка
                        prediction = Prediction.LONG;

                        //установка цены защитной приостановки
                        alphaOracle.setStopPrice(currentPrice / alphaOracle.getStopFactor());
                    }else if (VectorForecastUtil.isMax(forecast, vf.getN(), vf.getM())
                            || VectorForecastUtil.isMax(forecast, vf.getN()-1, vf.getM()-1)
                            || VectorForecastUtil.isMax(forecast, vf.getN()+1, vf.getM()-1)){
                        //уже в позиции
                        if (Prediction.SHORT.equals(alphaOracle.getPrediction())){
                            return;
                        }

                        //короткая продажа
                        prediction = Prediction.SHORT;

                        //установка цены защитной приостановки
                        alphaOracle.setStopPrice(currentPrice * alphaOracle.getStopFactor());
                    }
                }

                //обновление текущих параметров предсказателя
                if (prediction != null) {
                    alphaOracle.update(prediction, currentPrice);
                    alphaOracleBean.save(alphaOracle);
                }
            }

            //уведомление подписчиков о предсказании
            predicted(alphaOracle, prediction, quotes, forecast);

            //сохранение результата векторного прогнозирования
            vectorForecastBean.save(vf, quotes, forecast);

            //сохранение предсказания
            if (prediction != null && !alphaOracleBean.isAlphaOracleDataExists(alphaOracle.getId(), date)) {
                alphaOracleBean.save(new AlphaOracleData(alphaOracle.getId(), date, currentPrice, prediction));
            }
        }
    }

    private void predicted(AlphaOracle alphaOracle, Prediction prediction, List<Quote> quotes, float[] forecast){
        if (prediction != null) {
            int n = alphaOracle.getVectorForecast().getN();
            log.info(alphaOracle.getId() + "-" + alphaOracle.getVectorForecast().getSymbol() + ", " +
                    prediction.name() + ", " + dateFormat.format(quotes.get(n-1).getDate()) + ", " +
                    quotes.get(n-1).getClose());
        }

        for (IAlphaOracleListener listener : listeners){
            if (listener.getFilteredId() == null || listener.getFilteredId().equals(alphaOracle.getId())) {
                try {
                    listener.predicted(alphaOracle, prediction, quotes, forecast);
                } catch (Throwable e) {
                    log.error("Ошибка слушателя", e);
                }
            }
        }
    }
}
