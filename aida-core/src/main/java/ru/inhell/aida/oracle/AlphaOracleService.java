package ru.inhell.aida.oracle;

import com.google.common.primitives.Floats;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.TimeSeries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.Aida;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.inject.AidaInjector;
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

    private final static int CORE_POOL_SIZE = 1;

    private final static boolean USE_REMOTE = Aida.getProperty("use_remote_vssa").equals("true");

    public static final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.FULL);

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

    private ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(CORE_POOL_SIZE);
    private Map<Long, ScheduledFuture> scheduledFutures = new LinkedHashMap<Long, ScheduledFuture>();

    private List<IAlphaOracleListener> listeners = new ArrayList<IAlphaOracleListener>();

    public ScheduledFuture process(AlphaOracle alphaOracle){
        ScheduledFuture f = scheduledFutures.get(alphaOracle.getId());

        if (f == null){
            f = executor.scheduleAtFixedRate(getCommand(alphaOracle), 0, 500, TimeUnit.MILLISECONDS);

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
            private int orderSecond = 0;
            private Date last =  DateUtil.now();

            @Override
            public void run() {
                try {
                    if (Calendar.getInstance().get(Calendar.SECOND) != orderSecond){
                        return;
                    }

                    if (DateUtil.now().getTime() - last.getTime() < 500){
                        return;
                    }

//                    if (DateUtil.nowMsk().getTime() - quotesBean.getLastQuoteDate(alphaOracle.getSymbol()).getTime() > 100*1000){
//                        return;
//                    }

                    orderSecond = (orderSecond + 3) % 60;

                    predict(alphaOracle, 1, false, USE_REMOTE);

//                    log.info(new Date().toString());

                } catch (Throwable e) {
                    log.error("Ошибка предсказателя", e);
                }
            }
        };
    }

    private Map<Long, Date> lastPredict = new HashMap<Long, Date>();

    public void predict(AlphaOracle alphaOracle, int count, boolean skipIfOracleExists, boolean useRemote)
            throws  RemoteVSSAException {
        alphaOracle = alphaOracleBean.getAlphaOracle(alphaOracle.getId());

        Prediction prediction = null;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(DateUtil.nowMsk());
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute  = calendar.get(Calendar.MINUTE);

        if (hour == 2 && minute >= 49){
            if (!alphaOracle.isInMarket()){
                return;
            }

            switch (alphaOracle.getPrediction()){
                case LONG:
                    prediction = Prediction.STOP_SELL;
                    break;
                case SHORT:
                    prediction = Prediction.STOP_BUY;
                    break;
            }
        } else if (hour > 3 && hour < 10){
            return;
        }

        VectorForecast vf = alphaOracle.getVectorForecast();

        //загружаем все котировки
        List<Quote> allQuotes;

        if (alphaOracle.getInterval().equals(Interval.FIVE_SECOND)){
            allQuotes = quotesBean.getQuotes5Sec(vf.getSymbol(), vf.getN() + count);
        }else{
            allQuotes = quotesBean.getQuotes(vf.getSymbol(), vf.getN() + count);
        }

//        long now = DateUtil.nowMsk().getTime();
//        long last = allQuotes.get(allQuotes.size() - 1).getDate().getTime();

        //проверяем время последней котировки
//        if ((now - last < 50*1000 && alphaOracle.getInterval().equals(Interval.ONE_MINUTE))
//                || (now - last < 3*1000 && alphaOracle.getInterval().equals(Interval.FIVE_SECOND))){
//            allQuotes.remove(allQuotes.size() - 1);
//        } else {
//            allQuotes.remove(0);
//        }

        float[] forecast = null;

        for (int index = 1; index <= count; ++index) {
            //текущий список котировок
            List<Quote> quotes = allQuotes.subList(index, vf.getN() + index);

            float[] low = QuoteUtil.getLowPrices(quotes);
            float[] high = QuoteUtil.getHighPrices(quotes);

            //текущая дата
//            Date date = quotes.get(quotes.size()-1).getDate();
            Date date = DateUtil.nowMsk();

//            log.info(alphaOracle.getName() + " - predict " + dateFormat.format(date));

            //текущая цена
            float currentPrice = quotes.get(quotes.size()-1).getClose();

            //пропускаем если уже есть запись предсказания в базе данных
//            if (skipIfOracleExists && vectorForecastBean.isVectorForecastDataExists(vf.getId(), date)){
//                if (!vectorForecastBean.hasVectorForecastDataExtremum(vf.getId(), date)){
//                    continue;
//                }else if (alphaOracleBean.isAlphaOracleDataExists(alphaOracle.getId(), date)){
//                    continue;
//                }
//            }

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

            //предсказание на текущую дату
            if (prediction == null && alphaOracle.getStopCount() < alphaOracle.getMaxStopCount()) {
                if (alphaOracle.isInMarket()) {
                    float stopFactor = alphaOracle.getStopFactor();
                    float stopPrice = alphaOracle.getStopPrice();

                    if (Prediction.LONG.equals(alphaOracle.getPrediction())
                            &&(currentPrice < stopPrice || currentPrice > stopPrice*Math.pow(stopFactor, 5))){
                        prediction = Prediction.STOP_SELL;
                    }else if (Prediction.SHORT.equals(alphaOracle.getPrediction())
                            &&  (currentPrice > stopPrice || currentPrice < stopPrice/Math.pow(stopFactor, 5))){
                        prediction = Prediction.STOP_BUY;
                    }else if (StopType.T_STOP.equals(alphaOracle.getStopType())){ //stop trailing
                        if (Prediction.LONG.equals(alphaOracle.getPrediction())){
                            float min = Floats.min(sub(low, alphaOracle.getTs())) - 0.0002f;

                            if (stopPrice < min){
                                alphaOracle.setStopPrice(min);
                                alphaOracleBean.save(alphaOracle);
                            }
                        }else if (Prediction.SHORT.equals(alphaOracle.getPrediction())){
                            float max = Floats.max(sub(high, alphaOracle.getTs())) + 0.0002f;

                            if (stopPrice > max){
                                alphaOracle.setStopPrice(max);
                                alphaOracleBean.save(alphaOracle);
                            }
                        }
                    }
                }

                Date last = lastPredict.get(alphaOracle.getId());

                boolean skip = last != null && DateUtil.nowMsk().getTime() - last.getTime() < 30*1000;

                if (!skip && prediction == null && !alphaOracle.isInMarket()) {
                    //алгоритм векторного прогнозирования
                    if (useRemote) {
                        forecast = vectorForecastSSAService.executeRemote(vf.getN(), vf.getL(), vf.getP(), vf.getM(), prices);
                    }else{
                        forecast = vectorForecastSSAService.execute(vf.getN(), vf.getL(), vf.getP(), vf.getM(), prices);
                    }

                    if (VectorForecastUtil.isMin(forecast, vf.getN(), alphaOracle.getMd())
                            || VectorForecastUtil.isMin(forecast, vf.getN()-1, alphaOracle.getMd())
                            || VectorForecastUtil.isMin(forecast, vf.getN()+1, alphaOracle.getMd())){
                        //уже в позиции
                        if (!Prediction.LONG.equals(alphaOracle.getPrediction())){
                            //длинная покупка
                            prediction = Prediction.LONG;

                            //установка цены защитной приостановки
                            alphaOracle.setStopPrice(currentPrice / alphaOracle.getStopFactor());

                            lastPredict.put(alphaOracle.getId(), DateUtil.nowMsk());
                        }
                    }else if (VectorForecastUtil.isMax(forecast, vf.getN(), alphaOracle.getMd())
                            || VectorForecastUtil.isMax(forecast, vf.getN()-1, alphaOracle.getMd())
                            || VectorForecastUtil.isMax(forecast, vf.getN()+1, alphaOracle.getMd())){
                        //уже в позиции
                        if (!Prediction.SHORT.equals(alphaOracle.getPrediction())){
                            //короткая продажа
                            prediction = Prediction.SHORT;

                            //установка цены защитной приостановки
                            alphaOracle.setStopPrice(currentPrice * alphaOracle.getStopFactor());

                            lastPredict.put(alphaOracle.getId(), DateUtil.nowMsk());
                        }
                    }
                }

                //обновление текущих параметров предсказателя
                if (prediction != null) {
                    alphaOracle.update(prediction, currentPrice);
                    alphaOracleBean.save(alphaOracle);
                }
            }

            //уведомление подписчиков о предсказании
            if (index == count) {
                predicted(alphaOracle, prediction, quotes, forecast);
            }

            //сохранение результата векторного прогнозирования
            if (forecast != null) {
                vectorForecastBean.save(vf, quotes, forecast);
            }

            //сохранение предсказания
            if (prediction != null) {
                try {
                    alphaOracleBean.save(new AlphaOracleData(alphaOracle.getId(), date, currentPrice, prediction));
                } catch (Exception e) {
                    log.error(e.getMessage());
                }
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

    public void score(AlphaOracle alphaOracle, Date startDate, Date endDate){
        Long alphaOracleId = alphaOracle.getId();

        List<Quote> quotes = quotesBean.getQuotes(alphaOracle.getVectorForecast().getSymbol(), startDate, endDate);

        List<AlphaOracleData> alphaOracleDataList = alphaOracleBean.getAlphaOracleDatas(alphaOracleId, quotes.get(0).getDate());

        float score = 0;
        float price = 0;
        int quantity = 0;
        float max = 0;
        float min = 0;

        int size = alphaOracleDataList.size();

        for (int i=0; i < size; ++i){
            AlphaOracleData alphaOracleData = alphaOracleDataList.get(i);

            Date date = alphaOracleData.getDate();

            if (quantity == 0){
                price = alphaOracleData.getPrice();
            }

            //Результат сделки
            switch (alphaOracleData.getPrediction()){
                case LONG:
                    if (quantity == -1){
                        score += 2*(price - alphaOracleData.getPrice());
                        price = alphaOracleData.getPrice();
                    }

                    quantity = 1;
                    break;
                case SHORT:
                    if (quantity == 1){
                        score += 2*(alphaOracleData.getPrice() - price);
                        price = alphaOracleData.getPrice();
                    }

                    quantity = -1;
                    break;
                case STOP_BUY:
                    if (quantity == -1){
                        score += (price - alphaOracleData.getPrice());
                        price = alphaOracleData.getPrice();

                        quantity = 0;
                    }
                    break;
                case STOP_SELL:
                    if (quantity == 1) {
                        score += (alphaOracleData.getPrice() - price);
                        price = alphaOracleData.getPrice();

                        quantity = 0;
                    }
                    break;
            }

            //Максимум и минимум
            if (score > max){
                max = score;
            } else if (score < min){
                min = score;
            }

            //Новый торговый день, сохранение результатов
            if (i == size - 1 || !DateUtil.isSameDay(date, alphaOracleDataList.get(i+1).getDate())){
                float quotePrice = quotesBean.getQuote(alphaOracle.getVectorForecast().getSymbol(), date).getClose();

                if (quantity == -1){
                    score += (price - quotePrice);
                } else if (quantity == 1){
                    score += (quotePrice - price);
                }

                try {
                    AlphaOracleScore alphaOracleScore = new AlphaOracleScore(alphaOracleId, date, score, min, max);
                    alphaOracleBean.save(alphaOracleScore);

                    log.info(alphaOracleScore.toString());
                } catch (Exception e) {
                    log.error("", e);
                }

                quantity = 0;
                price = 0;
                score = 0;
                max = 0;
                min = 0;
            }
        }
    }

    private static float[] sub(float[] prices, int count){
        float[] p = new float[count];

        System.arraycopy(prices, prices.length - count - 1, p, 0, count);

        return p;
    }
}
