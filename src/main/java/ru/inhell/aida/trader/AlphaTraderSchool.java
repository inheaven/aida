package ru.inhell.aida.trader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.PeriodAxis;
import org.jfree.chart.axis.SegmentedTimeline;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.RemoteVSSAException;
import ru.inhell.aida.ssa.VectorForecastSSAService;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import javax.swing.*;
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
    private final static int COUNT = 6000;

    public static void main(String... args) throws RemoteVSSAException {
        randomStudyAll();
        //predict();
    }

    private static void predict() throws RemoteVSSAException {
        AlphaOracleService alphaOracleService = AidaInjector.getInstance(AlphaOracleService.class);

//        alphaOracleService.predict(1L, COUNT, true);
//        score();
        randomStudyAll();
    }

    static TimeSeriesCollection balanceDataSet = new TimeSeriesCollection();

    private static void initGUI(){
        JFrame frame = new JFrame("Alpha Oracle School");
        JFreeChart chart = ChartFactory.createTimeSeriesChart("Alpha Oracle School", "time", "balance", balanceDataSet,
                true, true, false);

//        ((XYLineAndShapeRenderer)chart.getXYPlot().getRenderer()).setBaseShapesVisible(true);

        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void score(){
        initGUI();

        score(1L);
        score(2L);
        score(3L);
        score(4L);
        score(5L);
        score(6L);
        score(7L);
        score(8L);
    }

    private static void score(Long alphaOracleId){
        AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

        AlphaOracle alphaOracle = alphaOracleBean.getAlphaOracle(alphaOracleId);

        List<Quote> quotes = quotesBean.getQuotes(alphaOracle.getVectorForecast().getSymbol(), COUNT);

        List<AlphaOracleData> alphaOracleDataList = alphaOracleBean.getAlphaOracleDatas(alphaOracleId, quotes.get(0).getDate());

        TimeSeries balanceTimeSeries = new TimeSeries("ao"+alphaOracleId);
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
                    }

                    quantity = 1;
                    break;
                case SHORT:
                    if (quantity == 1){
                        balance = balance + 2*(alphaOracleData.getPrice() - price);
                        price = alphaOracleData.getPrice();
                    }

                    quantity = -1;
                    break;
            }

            balanceTimeSeries.add(new Minute(alphaOracleData.getDate()), balance);
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

                    study("GAZP", n, l, p, m, true);
                    study("GAZP", n, l, p, m, false);

                    study("SBER03", n, l, p, m, true);
                    study("SBER03", n, l, p, m, false);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
    }

    private static void study(String symbol, int n, int l, int p, int m, boolean average)
            throws RemoteVSSAException {
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);
        VectorForecastSSAService vectorForecastSSAService = AidaInjector.getInstance(VectorForecastSSAService.class);

        float balance = 0;
        float price = 0;
        int quantity = 0;

        List<Quote> allQuotes = quotesBean.getQuotes(symbol, COUNT + n);
        TimeSeries balanceTimeSeries = new TimeSeries(symbol +"-n" + n + "l" + l +"p" + p + "m" + m + (average?"a":"c"));
        balanceDataSet.addSeries(balanceTimeSeries);

        for (int i=0; i < COUNT; ++i) {
            List<Quote> quotes = allQuotes.subList(i, n + i);

            float[] prices = average ? QuoteUtil.getAveragePrices(quotes) : QuoteUtil.getClosePrices(quotes);

            Quote currentQuote = quotes.get(quotes.size() - 1);

            float[] forecast = vectorForecastSSAService.execute(n, l, p, m, prices);

            if (VectorForecastUtil.isMin(forecast, n, m)
                    || VectorForecastUtil.isMin(forecast, n - 1, m)
                    || VectorForecastUtil.isMin(forecast, n + 1, m)){ //LONG
                if (quantity == 0){
                    price = currentQuote.getClose();
                }

                if (quantity == -1){
                    balance = balance + 2*(price - currentQuote.getClose());
                    price = currentQuote.getClose();
                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);
                }

                quantity = 1;
            }else if (VectorForecastUtil.isMax(forecast, n, m)
                    || VectorForecastUtil.isMax(forecast, n-1, m)
                    || VectorForecastUtil.isMax(forecast, n+1, m)){ //SHORT
                if (quantity == 0){
                    price = currentQuote.getClose();
                }

                if (quantity == 1){
                    balance = balance + 2*(currentQuote.getClose() - price);
                    price = currentQuote.getClose();
                    balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);
                }

                quantity = -1;
            }
        }

        if (balance < 0){
            balanceDataSet.removeSeries(balanceTimeSeries);
            System.out.println("series " + balanceTimeSeries.getKey() + " by negative balance: " + balance);
        }
    }
}
