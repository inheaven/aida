package ru.inhell.aida.trader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.SegmentedTimeline;
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
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 10.04.11 19:38
 */
public class AlphaTraderSchool {
    private final static int COUNT = 3000;

    public static void main(String... args) throws RemoteVSSAException {
//        randomStudyAll();
        initGUI();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);


        study("GAZP", 922, 80, 29, 6, true, false, 0);
        study("GAZP", 922, 80, 29, 6, true, true, 1.002f);
        study("GAZP", 922, 80, 29, 6, true, true, 1.004f);
        study("GAZP", 922, 80, 29, 6, true, true, 1.010f);

//        study("SBER03", 1244, 327, 37, 7, true, false, 0);

//        study("SBER03", 1244, 327, 37, 7, true, true, 1.002f);
//        study("SBER03", 1244, 327, 37, 7, true, true, 1.004f);
//        study("SBER03", 1244, 327, 37, 7, true, true, 1.01f);
    }


    static TimeSeriesCollection balanceDataSet = new TimeSeriesCollection();

    private static void initGUI(){
        JFrame frame = new JFrame("Alpha Oracle School");
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Alpha Oracle School", "time", "balance", balanceDataSet,
                true, true, false);

        ((XYLineAndShapeRenderer)chart.getXYPlot().getRenderer()).setBaseShapesVisible(true);
        SegmentedTimeline timeline = new SegmentedTimeline( SegmentedTimeline.MINUTE_SEGMENT_SIZE, 495, 945);
        timeline.setStartTime(SegmentedTimeline.firstMondayAfter1900() + 630 * timeline.getSegmentSize());
        timeline.setBaseTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());

        ((DateAxis)chart.getXYPlot().getDomainAxis()).setTimeline(SegmentedTimeline.newFifteenMinuteTimeline());

        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        for (int i=0; i < 20; ++i){
            executor.execute(getRandomStudyCommand());
        }
    }

    private static Runnable getRandomStudyCommand(){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    Random random = new Random();

                    int n = 300 + random.nextInt(1200);
                    int l = 30 + random.nextInt(70);
                    int p = 10 + random.nextInt(40);
                    int m = 5 + random.nextInt(5);
                    boolean useStop = random.nextBoolean();
                    boolean average = random.nextBoolean();

                    study("GAZP", n, l, p, m, average, useStop, 1.002f);
                    study("SBER03", n, l, p, m, average, useStop, 1.002f);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
    }

    private static Runnable getStudyCommand(final String symbol, final int n, final int l, final int p, final int m,
                                            final boolean average, final boolean useStop, final float stopFactor){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    study(symbol, n, l, p, m, average, useStop,  stopFactor);
                } catch (RemoteVSSAException e) {
                    e.printStackTrace();
                }
            }
        };
    }

    private static void study(String symbol, int n, int l, int p, int m, boolean average, boolean useStop, float stopFactor)
            throws RemoteVSSAException {
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);
        VectorForecastSSAService vectorForecastSSAService = AidaInjector.getInstance(VectorForecastSSAService.class);

        float balance = 0;
        float price = 0;
        float stopPrice = 0;
        int quantity = 0;

        boolean closeDay = false;

        List<Quote> allQuotes = quotesBean.getQuotes(symbol, COUNT + n);
        TimeSeries balanceTimeSeries = new TimeSeries(symbol +"-n" + n + "l" + l +"p" + p + "m" + m
                + (average?"a":"c") + (useStop?"s":"") + stopFactor);
        balanceDataSet.addSeries(balanceTimeSeries);

        Calendar current = Calendar.getInstance();

        for (int i=0; i < COUNT; ++i) {
            List<Quote> quotes = allQuotes.subList(i, n + i);

            float[] prices = average ? QuoteUtil.getAveragePrices(quotes) : QuoteUtil.getClosePrices(quotes);

            Quote currentQuote = quotes.get(quotes.size() - 1);

            //close day
            current.setTime(currentQuote.getDate());
            if (current.get(Calendar.HOUR_OF_DAY) == 18 && current.get(Calendar.MINUTE) > 40){
                closeDay = true;
            }

            float[] forecast = vectorForecastSSAService.execute(n, l, p, m, prices);

            if (VectorForecastUtil.isMin(forecast, n, m)
                    || VectorForecastUtil.isMin(forecast, n - 1, m)
                    || VectorForecastUtil.isMin(forecast, n + 1, m)){ //LONG
                if (quantity == 0){
                    price = currentQuote.getClose();
                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);
                }

                if (quantity == -1){
                    balance = balance + 2*(price - currentQuote.getClose());
                    price = currentQuote.getClose();
                    stopPrice = price/stopFactor;
                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);
                }

                quantity = 1;
                continue;
            }else if (VectorForecastUtil.isMax(forecast, n, m)
                    || VectorForecastUtil.isMax(forecast, n-1, m)
                    || VectorForecastUtil.isMax(forecast, n+1, m)){ //SHORT
                if (quantity == 0){
                    price = currentQuote.getClose();
                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);
                }

                if (quantity == 1){
                    balance = balance + 2*(currentQuote.getClose() - price);
                    price = currentQuote.getClose();
                    stopPrice = price*stopFactor;
                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);
                }

                quantity = -1;
                continue;
            }

            //stop
            if ((useStop && stopPrice != 0) || closeDay) {
                if (quantity > 0 && (currentQuote.getClose() < stopPrice || closeDay)){ //stop sell
                    balance = balance + (currentQuote.getClose() - price);
                    price = currentQuote.getClose();

                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);

                    stopPrice = 0;
                    quantity = 0;
                    closeDay = false;
                }else if (quantity < 0 && (currentQuote.getClose() > stopPrice || closeDay)){ //stop bay
                    balance = balance + (price - currentQuote.getClose());
                    price = currentQuote.getClose();

                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);

                    stopPrice = 0;
                    quantity = 0;
                    closeDay = false;
                }
            }
        }

        if (balance < 0){
            balanceDataSet.removeSeries(balanceTimeSeries);
            System.out.println("series " + balanceTimeSeries.getKey() + " by negative balance: " + balance);
        }
    }
}
