package ru.inhell.aida.trader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.RemoteVSSAException;
import ru.inhell.aida.ssa.VectorForecastSSAService;
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 10.04.11 19:38
 */
public class AlphaTraderSchool {
    private final static int COUNT = 10000;

    public static void main(String... args) throws RemoteVSSAException {
//        studyAll();

//        scoreAll();
//
        randomStudyAll();

//        initGUI();
//        study("SBER03", 1027, 285, 16, 11, true, true, 1.002f);
//        study("SBER03", 596, 64, 37, 8, true, true, 1.0002f);
//        study("20-","GAZP", 202, 75, 71, 7, true, true, 1.008f);
//        study("GAZP", 922, 80, 29, 6, true, true, 1.002f);
//        study("SBER03", 1244, 327, 37, 7, true, true, 1.01f);
    }


    static TimeSeriesCollection balanceDataSet = new TimeSeriesCollection();
    static TimeSeriesCollection forecastDataSet = new TimeSeriesCollection();

    private static void initGUI(){
        JFrame frame = new JFrame("Alpha Oracle School");

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Alpha Oracle School - Balance", "time", "balance", balanceDataSet,
                true, true, false);

        chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        ((XYLineAndShapeRenderer)chart.getXYPlot().getRenderer()).setBaseShapesVisible(false);

//        SegmentedTimeline timeline = new SegmentedTimeline( SegmentedTimeline.MINUTE_SEGMENT_SIZE, 495, 945);
//        timeline.setStartTime(SegmentedTimeline.firstMondayAfter1900() + 630 * timeline.getSegmentSize());
//        timeline.setBaseTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
//
//        ((DateAxis)chart.getXYPlot().getDomainAxis()).setTimeline(SegmentedTimeline.newFifteenMinuteTimeline());

        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }


    private static void studyAll() throws RemoteVSSAException {
        initGUI();

        AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);

        List<AlphaOracle> alphaOracles =  alphaOracleBean.getAlphaOracles();
        for (AlphaOracle ao : alphaOracles){
            VectorForecast vf = ao.getVectorForecast();

            if (vf.getSymbol().equals("GAZP")){
                executor.execute(getStudyCommand(vf.getId()+"-", vf.getSymbol(), vf.getN(), vf.getL(), vf.getP(), vf.getM(), ao.getPriceType().equals(AlphaOracle.PRICE_TYPE.AVERAGE), true, 1.002f));
            }
        }

    }

    private static void scoreAll() throws RemoteVSSAException {
        initGUI();

        AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);
        AlphaOracleService alphaOracleService = AidaInjector.getInstance(AlphaOracleService.class);

        List<AlphaOracle> alphaOracles =  alphaOracleBean.getAlphaOracles();
        for (AlphaOracle alphaOracle : alphaOracles){
            alphaOracleService.predict(alphaOracle, COUNT, true, false);

            score(alphaOracle);
        }
    }

    private static void score(AlphaOracle alphaOracle){
        Long alphaOracleId = alphaOracle.getId();

        AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

        List<Quote> quotes = quotesBean.getQuotes(alphaOracle.getVectorForecast().getSymbol(), COUNT);

        List<AlphaOracleData> alphaOracleDataList = alphaOracleBean.getAlphaOracleDatas(alphaOracleId, quotes.get(0).getDate());

        VectorForecast vf = alphaOracle.getVectorForecast();

        String name = vf.getId() + vf.getSymbol() +"-n" + vf.getN() + "l" + vf.getL() +"p" + vf.getP() + "m" + vf.getM() +
                (alphaOracle.getPriceType().equals(AlphaOracle.PRICE_TYPE.AVERAGE)?"a":"c");

        TimeSeries balanceTimeSeries = new TimeSeries(name);
        balanceDataSet.addSeries(balanceTimeSeries);

        float balance = 0;
        float price = 0;
        int quantity = 0;

        for (AlphaOracleData alphaOracleData : alphaOracleDataList){
            if (quantity == 0){
                price = alphaOracleData.getPrice();
            }

            switch (alphaOracleData.getPrediction()){
                case LONG:
                    if (quantity == -1){
                        balance = balance + 2*(price - alphaOracleData.getPrice());
                        price = alphaOracleData.getPrice();

                        balanceTimeSeries.add(new Minute(alphaOracleData.getDate()), balance);
                    }

                    quantity = 1;
                    break;
                case SHORT:
                    if (quantity == 1){
                        balance = balance + 2*(alphaOracleData.getPrice() - price);
                        price = alphaOracleData.getPrice();

                        balanceTimeSeries.add(new Minute(alphaOracleData.getDate()), balance);
                    }

                    quantity = -1;
                    break;
            }
        }
    }

    private static void randomStudyAll() throws RemoteVSSAException{
        initGUI();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);

        for (int i=0; i < 100; ++i){
            executor.execute(getRandomStudyCommand());
        }
    }

    private static Runnable getRandomStudyCommand(){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Random random = new Random();

                    int n = 1000 + random.nextInt(1000);
                    int l = 495;
                    int p = 20 + random.nextInt(100);
                    int m = 10;
//                    boolean useStop = random.nextBoolean();
//                    boolean average = random.nextBoolean();


                    study("", "GAZP", n, l, p, m, true, true, 1.004f);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
    }

    private static Runnable getStudyCommand(final String prefix, final String symbol, final int n, final int l, final int p, final int m,
                                            final boolean average, final boolean useStop, final float stopFactor){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    study(prefix, symbol, n, l, p, m, average, useStop,  stopFactor);
                } catch (RemoteVSSAException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    static float maxBalance = 0;

    private static void study(String prefix, String symbol, int n, int l, int p, int m, boolean average, boolean useStop, float stopFactor)
            throws RemoteVSSAException {
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);
        VectorForecastSSAService vectorForecastSSAService = AidaInjector.getInstance(VectorForecastSSAService.class);

        float balance = 0;
        float price = 0;
        float stopPrice = 0;
        int quantity = 0;

        int orderCount = 0;
        int stopCount = 0;

        boolean closeDay = false;

        Date start = null, end = null;

        List<Quote> allQuotes = quotesBean.getQuotes(symbol, COUNT + n);

        String name = symbol +"-n" + n + "l" + l +"p" + p + "m" + m + (average?"a":"c") + (useStop?"s":"") + stopFactor;

        TimeSeries balanceTimeSeries = new TimeSeries(name);
        balanceDataSet.addSeries(balanceTimeSeries);

//        TimeSeries stopTimeSeries = new TimeSeries("STOP_" + name);
//        balanceDataSet.addSeries(stopTimeSeries);

        Calendar current = Calendar.getInstance();

        for (int i=0; i < COUNT; ++i) {
            List<Quote> quotes = allQuotes.subList(i, n + i);

            float[] prices = average ? QuoteUtil.getAveragePrices(quotes) : QuoteUtil.getClosePrices(quotes);

            Quote currentQuote = quotes.get(quotes.size() - 1);

            if (i == 0){
                start = currentQuote.getDate();
            }else if (i == COUNT - 1){
                end = currentQuote.getDate();
            }

            //close day
            current.setTime(currentQuote.getDate());
            if (current.get(Calendar.HOUR_OF_DAY) == 18 && current.get(Calendar.MINUTE) > 40){
                if (orderCount < 3 || balance < -5){
                    System.out.println(prefix+balanceTimeSeries.getKey() + "," + balance + "," + orderCount + "," + stopCount + "," + start + "," + end);
                    balanceDataSet.removeSeries(balanceTimeSeries);
                    return;
                }


                closeDay = true;
            }

            //stop
            if ((useStop && stopPrice != 0) || closeDay) {
                if (quantity > 0 && (prices[prices.length-1] < stopPrice || closeDay)){ //stop sell
                    balance = balance + (currentQuote.getClose() - price);
                    price = currentQuote.getClose();

                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);

                    stopPrice = 0;
                    quantity = 0;
                    closeDay = false;
                    stopCount++;
                    continue;
                }else if (quantity < 0 && (prices[prices.length-1] > stopPrice || closeDay)){ //stop buy
                    balance = balance + (price - currentQuote.getClose());
                    price = currentQuote.getClose();

                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);

                    stopPrice = 0;
                    quantity = 0;
                    closeDay = false;
                    stopCount++;
                    continue;
                }
            }

            float[] forecast = vectorForecastSSAService.execute(n, l, p, m, prices);

            if (VectorForecastUtil.isMin(forecast, n, m)
                    || VectorForecastUtil.isMin(forecast, n - 1, m)
                    || VectorForecastUtil.isMin(forecast, n + 1, m)){ //LONG
                if (quantity == 0){
                    price = currentQuote.getClose();
                    stopPrice = price/stopFactor;

                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);

                    orderCount++;
                    quantity = 1;
                }

                if (quantity == -1){
                    balance = balance + 2*(price - currentQuote.getClose());
                    price = currentQuote.getClose();
                    stopPrice = price/stopFactor;

                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);

                    orderCount++;
                    quantity = 1;
                }
            }else if (VectorForecastUtil.isMax(forecast, n, m)
                    || VectorForecastUtil.isMax(forecast, n-1, m)
                    || VectorForecastUtil.isMax(forecast, n+1, m)){ //SHORT
                if (quantity == 0){
                    price = currentQuote.getClose();
                    stopPrice = price/stopFactor;

                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);

                    orderCount++;
                    quantity = -1;
                }

                if (quantity == 1){
                    balance = balance + 2*(currentQuote.getClose() - price);
                    price = currentQuote.getClose();
                    stopPrice = price*stopFactor;

                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);

                    orderCount++;
                    quantity = -1;
                }
            }
        }

        if (balance < maxBalance){
            balanceDataSet.removeSeries(balanceTimeSeries);
        }else{
            maxBalance = balance;
        }

        System.out.println(prefix+balanceTimeSeries.getKey() + "," + balance + "," + orderCount + "," + stopCount + "," + start + "," + end);
    }
}
