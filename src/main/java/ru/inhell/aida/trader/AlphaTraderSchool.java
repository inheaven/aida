package ru.inhell.aida.trader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
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
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import javax.swing.*;
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
        predict();
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
        frame.add(new ChartPanel(ChartFactory.createTimeSeriesChart("Alpha Oracle School", "time", "balance", balanceDataSet, true, true, false)));
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

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

        for (int i=0; i < 30; ++i){
            executor.execute(getRandomStudyCommand("GAZP"));
            executor.execute(getRandomStudyCommand("SBER"));
        }
    }

    private static Runnable getRandomStudyCommand(final String symbol){
        return new Runnable() {
            @Override
            public void run() {
                try {
                    randomStudy(symbol);
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                }
            }
        };
    }

    private static void randomStudy(String symbol) throws RemoteVSSAException {
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);
        VectorForecastSSAService vectorForecastSSAService = AidaInjector.getInstance(VectorForecastSSAService.class);
        Random random = new Random();

        boolean average = random.nextBoolean();
        int n = 500 + random.nextInt(2000);
        int l = 100 + random.nextInt(n/2-100);
        int p = 10 + random.nextInt(30);
        int m = 3 + random.nextInt(17);

        float balance = 0;
        float price = 0;
        int quantity = 0;

        List<Quote> allQuotes = quotesBean.getQuotes(symbol, COUNT + n);
        TimeSeries balanceTimeSeries = new TimeSeries(symbol +"-n" + n + "l" + l +"p" + p + "m" + m);
        balanceDataSet.addSeries(balanceTimeSeries);

        for (int i=0; i < COUNT; ++i) {
            List<Quote> quotes = allQuotes.subList(i, n + i);

            float[] prices = average ? QuoteUtil.getAveragePrices(quotes) : QuoteUtil.getClosePrices(quotes);

            Quote currentQuote = quotes.get(quotes.size() - 1);

            float[] forecast = vectorForecastSSAService.executeRemote(n, l, p, m, prices);

            if (VectorForecastUtil.isMin(forecast, n, m)
                    || VectorForecastUtil.isMin(forecast, n - 1, m)
                    || VectorForecastUtil.isMin(forecast, n + 1, m)){ //LONG
                if (quantity == 0){
                    price = currentQuote.getClose();
                }

                if (quantity == -1){
                    balance = balance + 2*(price - currentQuote.getClose());
                    price = currentQuote.getClose();
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
                }

                quantity = -1;
            }

            balanceTimeSeries.add(new Minute(currentQuote.getDate()), balance);
        }
    }
}
