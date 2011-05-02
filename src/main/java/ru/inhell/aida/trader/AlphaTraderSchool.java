package ru.inhell.aida.trader;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.FixedMillisecond;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.RemoteVSSAException;
import ru.inhell.aida.ssa.VectorForecastSSA;
import ru.inhell.aida.ssa.VectorForecastSSAService;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import javax.swing.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
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
    public static final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

    private final static int COUNT = 495*20;
    static FileWriter fileWriter;

    public static void main(String... args) throws RemoteVSSAException, IOException {
        fileWriter = new FileWriter("c:\\aos.txt", true);

        //        scoreAll();

        randomStudyAll();
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

    private static void scoreAll() throws RemoteVSSAException {
        initGUI();

        AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);
        final AlphaOracleService alphaOracleService = AidaInjector.getInstance(AlphaOracleService.class);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

        List<AlphaOracle> alphaOracles =  alphaOracleBean.getAlphaOracles();
        for (final AlphaOracle alphaOracle : alphaOracles){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (alphaOracle.getVectorForecast().getSymbol().equals("GAZP") && alphaOracle.getId() >= 20) {
//                        alphaOracleService.predict(alphaOracle, COUNT, true, false);
                        score(alphaOracle);
                    }
                }
            });
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
                (alphaOracle.getPriceType().equals(PriceType.AVERAGE)?"a":"c");

        TimeSeries balanceTimeSeries = new TimeSeries(name);
        balanceDataSet.addSeries(balanceTimeSeries);

        float balance = 0;
        float price = 0;
        int quantity = 0;

        for (int i=0; i < alphaOracleDataList.size()-1; ++i){
            AlphaOracleData alphaOracleData = alphaOracleDataList.get(i);
            AlphaOracleData alphaOracleDataNext = alphaOracleDataList.get(i+1);

             Date date = alphaOracleData.getDate();

            if (quantity == 0){
                price = alphaOracleData.getPrice();
//                balanceTimeSeries.add(new Minute(date), balance);
                balanceTimeSeries.add(new FixedMillisecond(i), balance);
            }

            switch (alphaOracleData.getPrediction()){
                case LONG:
                    if (quantity == -1){
                        balance += 2*(price - alphaOracleData.getPrice());
                        price = alphaOracleData.getPrice();

//                        balanceTimeSeries.add(new Minute(date), balance);
                        balanceTimeSeries.add(new FixedMillisecond(i), balance);
                    }

                    quantity = 1;
                    break;
                case SHORT:
                    if (quantity == 1){
                        balance += 2*(alphaOracleData.getPrice() - price);
                        price = alphaOracleData.getPrice();

//                        balanceTimeSeries.add(new Minute(date), balance);
                        balanceTimeSeries.add(new FixedMillisecond(i), balance);
                    }

                    quantity = -1;
                    break;
                case STOP_BUY:
                    if (quantity == -1){
                        balance += (price - alphaOracleData.getPrice());
                        price = alphaOracleData.getPrice();

//                        balanceTimeSeries.add(new Minute(date), balance);
                        balanceTimeSeries.add(new FixedMillisecond(i), balance);

                        quantity = 0;
                    }
                    break;
                case STOP_SELL:
                    if (quantity == 1) {
                        balance += (alphaOracleData.getPrice() - price);
                        price = alphaOracleData.getPrice();

//                        balanceTimeSeries.add(new Minute(date), balance);
                        balanceTimeSeries.add(new FixedMillisecond(i), balance);

                        quantity = 0;
                    }
                    break;
            }


            if (!DateUtil.isSameDay(date, alphaOracleDataNext.getDate())){
                quantity = 0;
            }
        }
    }

    private static void randomStudyAll() throws RemoteVSSAException{
        initGUI();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        for (int i=0; i < 1000; ++i){

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Random random = new Random();

                        int[] ll = {30, 60, 80, 90, 120};
                        int nn[] = {495*3, 495*4, 495*5, 495*6, 495*7, 495*8, 495*9, 495*10, 495*11, 495*12, 495*13, 495*14};
                        int[] mm = {5, 10, 15, 30};
                        float[] ss = {1.002f, 1.004f, 1.008f, 1.01f};

                        int n = nn[random.nextInt(12)];
                        int l = ll[random.nextInt(5)];
                        int p = 10 + random.nextInt(90);
                        int m = mm[random.nextInt(4)];
                        float s = ss[random.nextInt(4)];

                        study("", "GAZP", n, l, p, m, true, true, s);
                    } catch (Exception e) {
                        System.out.println(e.getMessage());
                    }
                }
            });
        }
    }

    static float maxBalance = 0;
    static float minBalance = 0;

    private final static boolean timing = false;


    private static void study(String prefix, String symbol, int n, int l, int p, int m, boolean average, boolean useStop, float stopFactor)
            throws RemoteVSSAException {
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

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

        Calendar current = Calendar.getInstance();

        VectorForecastSSA vectorForecastSSA = new VectorForecastSSA(n, l, p, m);

        long time;

        for (int i=0; i < COUNT; ++i) {
            if (i > 1000 && balance < 0){
                try {
                    fileWriter.append(name).append(",,,,,").append("\n");
                    balanceDataSet.removeSeries(balanceTimeSeries);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;
            }

            if (timing) time = System.currentTimeMillis();

            List<Quote> quotes = allQuotes.subList(i, n + i);

            if (timing){
                System.out.println("AlphaTraderSchool.1 " + (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
            }

            float[] prices = average ? QuoteUtil.getAveragePrices(quotes) : QuoteUtil.getClosePrices(quotes);

            if (timing){
                System.out.println("AlphaTraderSchool.2 " + (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
            }

            Quote currentQuote = quotes.get(quotes.size() - 1);

            if (i == 0){
                start = currentQuote.getDate();
            }else if (i == COUNT - 1){
                end = currentQuote.getDate();
            }

            //close day
            current.setTime(currentQuote.getDate());
            if (current.get(Calendar.HOUR_OF_DAY) == 18 && current.get(Calendar.MINUTE) > 40){
                if (orderCount < 2){
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

            if (timing){
                System.out.println("AlphaTraderSchool.3 " + (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
            }

            float[] forecast = new float[0];
            try {
                forecast = vectorForecastSSA.execute(prices);
            } catch (Exception e) {
                balanceDataSet.removeSeries(balanceTimeSeries);
            }

            if (timing){
                System.out.println("AlphaTraderSchool.4 " + (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
            }

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

            if (timing){
                System.out.println("AlphaTraderSchool.5 " + (System.currentTimeMillis() - time));
            }
        }

        if (balance >= minBalance && balance < maxBalance){
            balanceDataSet.removeSeries(balanceTimeSeries);
        }else{
            if (balance > maxBalance){
                maxBalance = balance;
            } else if (balance < minBalance){
                minBalance = balance;
            }
        }

        String s = prefix+balanceTimeSeries.getKey() + "," + balance + "," + orderCount + "," + stopCount + "," + dateFormat.format(start) + "," + dateFormat.format(end);

        try {
            fileWriter.append(s).append("\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(s);
    }
}
