package ru.inhell.aida.trader;

import org.apache.ibatis.logging.stdout.StdOutImpl;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.*;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.AlphaOracleSchoolBean;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.quik.TransactionBean;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.ssa.RemoteVSSAException;
import ru.inhell.aida.ssa.VectorForecastSSA;
import ru.inhell.aida.ssa.VectorForecastSSAService;
import ru.inhell.aida.util.DateUtil;
import ru.inhell.aida.util.QuoteUtil;
import ru.inhell.aida.util.VectorForecastUtil;

import javax.swing.*;
import java.awt.*;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 10.04.11 19:38
 */
public class AlphaTraderSchool {
    public static final DateFormat dateFormat = SimpleDateFormat.getDateTimeInstance(SimpleDateFormat.SHORT, SimpleDateFormat.SHORT);

    private final static int COUNT = 10000;
    static FileWriter fileWriter;

    public static void main(String... args) throws Exception{
        fileWriter = new FileWriter("c:\\aos.txt", true);

//                scoreAll();

        initGUI();

//        AidaInjector.getInstance(AlphaOracleService.class).predict(new AlphaOracle(38L), 720*12, false, false);

//        score(AidaInjector.getInstance(AlphaOracleBean.class).getAlphaOracle(38L));

//        study("", "GZM1", 1005, 60, 37, 10, true, true, 1.001f);


//        for (int i = 0; i < 10000; i+=20) {
//            study("", "GZM1", 100 + i, 10, 2, 5, true, true, 1.002f);
//        }

//        transaction("GZM1", "30.05.2011");
//        transaction("GZM1", "31.05.2011");
//        transaction("GZM1", "01.06.2011");

//        study2("", "GAZP", "GZM1", 720, 10, 2, 5, true, true, 1.002f);
//        study2("", "GZM1", 720, 10, 2, 5, true, true, 1.002f);



        randomStudyAll();

//        session();

//        studyAll();

//        AidaInjector.getInstance(AlphaOracleService.class).predict(new AlphaOracle(38L), 720, false, false);
    }

    private static void studyAll(){
        List<AlphaTrader> alphaTraders = AidaInjector.getInstance(AlphaTraderBean.class).getAlphaTraders();

        for (AlphaTrader alphaTrader : alphaTraders){
            VectorForecast vf = alphaTrader.getAlphaOracle().getVectorForecast();

            if (vf.getSymbol().equals("GAZP")) {
                study2(vf.getId() + "", "GZM1", "GZM1", vf.getN(), vf.getL(), vf.getP(), vf.getM(), true, true, alphaTrader.getAlphaOracle().getStopFactor());
            }
        }
    }

    private static void session(){
        AlphaOracleSchoolBean alphaOracleSchoolBean = AidaInjector.getInstance(AlphaOracleSchoolBean.class);

        List<AlphaOracleSchool> alphaOracleSchools = alphaOracleSchoolBean.getAlphaOracleSchools();

        for (AlphaOracleSchool aos : alphaOracleSchools){
            study2("", "GZM1", "GZM1", aos.getN(), aos.getL(), aos.getP(), aos.getM(), aos.isAverage(), true, aos.getStopFactor());
        }
    }


    static TimeSeriesCollection balanceDataSet = new TimeSeriesCollection();
    static TimeSeriesCollection forecastDataSet = new TimeSeriesCollection();
    static JLabel label,label1;

    private static void initGUI(){
        JFrame frame = new JFrame("Alpha Oracle School");

        JFreeChart chart = ChartFactory.createTimeSeriesChart("Alpha Oracle School", "time", "balance", balanceDataSet,
                true, true, false);

        chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);

        ((XYLineAndShapeRenderer)chart.getXYPlot().getRenderer()).setBaseShapesVisible(false);

//        SegmentedTimeline timeline = new SegmentedTimeline( SegmentedTimeline.MINUTE_SEGMENT_SIZE, 495, 945);
//        timeline.setStartTime(SegmentedTimeline.firstMondayAfter1900() + 630 * timeline.getSegmentSize());
//        timeline.setBaseTimeline(SegmentedTimeline.newMondayThroughFridayTimeline());
//
//        ((DateAxis)chart.getXYPlot().getDomainAxis()).setTimeline(SegmentedTimeline.newFifteenMinuteTimeline());

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(new ChartPanel(chart), BorderLayout.CENTER);

        JPanel panelLabel = new JPanel(new GridLayout(1,2));
        panel.add(panelLabel, BorderLayout.SOUTH);

        label = new JLabel();
        panelLabel.add(label);

        label1 = new JLabel();
        panelLabel.add(label1);

        frame.add(panel);

        frame.pack();
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private static void scoreAll() throws RemoteVSSAException {
        AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);
        final AlphaOracleService alphaOracleService = AidaInjector.getInstance(AlphaOracleService.class);

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(3);

        List<AlphaOracle> alphaOracles =  alphaOracleBean.getAlphaOracles();
        for (final AlphaOracle alphaOracle : alphaOracles){
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    if (alphaOracle.getVectorForecast().getSymbol().equals("GZM1") && alphaOracle.getId() >= 20
                            && (alphaOracle.getId() == 37 || alphaOracle.getId() == 38)) {
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
                balanceTimeSeries.add(new Minute(date), balance);
//                balanceTimeSeries.add(new FixedMillisecond(i), balance);
            }

            switch (alphaOracleData.getPrediction()){
                case LONG:
                    if (quantity == -1){
                        balance += 2*(price - alphaOracleData.getPrice());
                        price = alphaOracleData.getPrice();

                        balanceTimeSeries.add(new Minute(date), balance);
//                        balanceTimeSeries.add(new FixedMillisecond(i), balance);`
                    }

                    quantity = 1;
                    break;
                case SHORT:
                    if (quantity == 1){
                        balance += 2*(alphaOracleData.getPrice() - price);
                        price = alphaOracleData.getPrice();

                        balanceTimeSeries.add(new Minute(date), balance);
//                        balanceTimeSeries.add(new FixedMillisecond(i), balance);
                    }

                    quantity = -1;
                    break;
                case STOP_BUY:
                    if (quantity == -1){
                        balance += (price - alphaOracleData.getPrice());
                        price = alphaOracleData.getPrice();

                        balanceTimeSeries.add(new Minute(date), balance);
//                        balanceTimeSeries.add(new FixedMillisecond(i), balance);

                        quantity = 0;
                    }
                    break;
                case STOP_SELL:
                    if (quantity == 1) {
                        balance += (alphaOracleData.getPrice() - price);
                        price = alphaOracleData.getPrice();

                        balanceTimeSeries.add(new Minute(date), balance);
//                        balanceTimeSeries.add(new FixedMillisecond(i), balance);

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
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        for (int i=0; i < 1000; ++i){

            executor.execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        Random random = new Random();

//                        int[] ll = {10, 20, 30, 60};
//                        int nn[] = {720, 720*2, 720*3};
//                        int[] mm = {5, 10, 15};
                        float[] ss = {1.002f, 1.004f, 1.008f, 1.01f};

                        int n = 720 + random.nextInt(720*2);
                        int l = 10  + random.nextInt(20);
                        int p = 1 + random.nextInt(10);
                        int m = 5;
                        float s = ss[random.nextInt(4)];

                        study2("", "GZM1", "GZM1", n, l, p, m, true, true, s);
                        study2("", "GAZP", "GZM1", n, l, p, m, true, true, s);
                    } catch (Throwable e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    static AtomicInteger currentLabel = new AtomicInteger(2);
    static float maxBalance = 0;
    static float minBalance = 0;

    private final static boolean timing = false;


    @Deprecated
    private static void study(String prefix, String symbol, int n, int l, int p, int m, boolean average, boolean useStop, float stopFactor)
            throws RemoteVSSAException {
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

        float balance = 0;
        float price = 0;
        float stopPrice = 0;
        int quantity = 0;
        int orderQuantity = 1;

        int orderCount = 0;
        int stopCount = 0;

        boolean closeDay = false;

        Date start = null, end = null;

        List<Quote> allQuotes = quotesBean.getQuotes(symbol, COUNT + n);
        List<Quote> allFutureQuotes = quotesBean.getQuotes(symbol, COUNT + n);

        String name = symbol +"-n" + n + "l" + l +"p" + p + "m" + m + (average?"a":"c") + (useStop?"s":"") + stopFactor;

        TimeSeries balanceTimeSeries = new TimeSeries(name);
        balanceDataSet.addSeries(balanceTimeSeries);

        Calendar current = Calendar.getInstance();

        VectorForecastSSA vectorForecastSSA = new VectorForecastSSA(n, l, p, m);

        long time;

        for (int i=0; i < COUNT-1; ++i) {
            if ((i > 10000 && balance < 0) || (i > 500 && orderCount < 2)){
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
//            List<Quote> quotesFuture = allFutureQuotes.subList(i, n + i);

            if (timing){
                System.out.println("AlphaTraderSchool.1 " + (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
            }

            float[] prices = average ? QuoteUtil.getAveragePrices(quotes) : QuoteUtil.getClosePrices(quotes);

            if (timing){
                System.out.println("AlphaTraderSchool.2 " + (System.currentTimeMillis() - time));
                time = System.currentTimeMillis();
            }


            Quote currentQuote = allQuotes.get(n + i);
            float currentPrice = currentQuote.getOpen();

            Quote currentFutureQuote = allQuotes.get(n + i);
            float currentFuturePrice = (currentFutureQuote.getOpen() + currentFutureQuote.getHigh()
                    + currentFutureQuote.getLow() + currentFutureQuote.getClose())/4;
            Date currentDate = currentFutureQuote.getDate();

            if (i == 0){
                start = currentDate;
            }else if (i == COUNT - 2){
                end = currentDate;
            }

            //close day
            current.setTime(currentDate);
            if (current.get(Calendar.HOUR_OF_DAY) == 23 && current.get(Calendar.MINUTE) > 40){
//                if (orderCount < 2){
//                    balanceDataSet.removeSeries(balanceTimeSeries);
//                    return;
//                }

                closeDay = true;
            }

            //stop
            if ((useStop && stopPrice != 0) || closeDay) {
                if (quantity > 0 && (currentPrice < stopPrice || closeDay)){ //stop sell
                    balance = balance + (currentFuturePrice - price);
                    price = currentFuturePrice;

                    balanceTimeSeries.add(new Minute(currentDate), balance);

                    stopPrice = 0;
                    quantity = 0;
                    closeDay = false;
                    stopCount++;
                    orderQuantity = 1;

                    System.out.println("STOP_SELL, " + price + ", " + dateFormat.format(currentDate) + ", " + balance);

                    continue;
                }else if (quantity < 0 && (currentPrice > stopPrice || closeDay)){ //stop buy
                    balance = balance + (price - currentFuturePrice);
                    price = currentFuturePrice;

                    balanceTimeSeries.add(new Minute(currentDate), balance);

                    stopPrice = 0;
                    quantity = 0;
                    closeDay = false;
                    stopCount++;
                    orderQuantity = 1;

                    System.out.println("STOP_BUY, " + price + ", " + dateFormat.format(currentDate) + ", " + balance);

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
                    price = currentFuturePrice;
                    stopPrice = currentPrice/stopFactor;

                    balanceTimeSeries.add(new Minute(currentDate), balance);

                    orderCount++;
                    quantity = 1;
                    orderQuantity = 1;

                    System.out.println("LONG, " + price + ", " + dateFormat.format(currentDate) + ", " + balance);
                }

                if (quantity == -1){
                    balance = balance + orderQuantity*(price - currentFuturePrice);
                    price = currentFuturePrice;
                    stopPrice = currentPrice/stopFactor;

                    balanceTimeSeries.add(new Minute(currentDate), balance);

                    orderCount++;
                    quantity = 1;
                    orderQuantity = 2;

                    System.out.println("LONG, " + price + ", " + dateFormat.format(currentDate) + ", " + balance);
                }
            }else if (VectorForecastUtil.isMax(forecast, n, m)
                    || VectorForecastUtil.isMax(forecast, n-1, m)
                    || VectorForecastUtil.isMax(forecast, n+1, m)){ //SHORT
                if (quantity == 0){
                    price = currentFuturePrice;
                    stopPrice = currentPrice/stopFactor;

                    balanceTimeSeries.add(new Minute(currentDate), balance);

                    orderCount++;
                    quantity = -1;
                    orderQuantity = 1;

                    System.out.println("SHORT, " + price + ", " + dateFormat.format(currentDate) + ", " + balance);
                }

                if (quantity == 1){
                    balance = balance + orderQuantity*(currentFuturePrice - price);
                    price = currentFuturePrice;
                    stopPrice = currentPrice*stopFactor;

                    balanceTimeSeries.add(new Minute(currentDate), balance);

                    orderCount++;
                    quantity = -1;
                    orderQuantity = 2;

                    System.out.println("SHORT, " + price + ", " + dateFormat.format(currentDate) + ", " + balance);
                }


            }

            if (timing){
                System.out.println("AlphaTraderSchool.5 " + (System.currentTimeMillis() - time));
            }

            label.setText(i + ": " + prefix+balanceTimeSeries.getKey() + ", " + balance + ", " + orderCount + ", " + stopCount);
        }

        if (balance >= minBalance && balance < maxBalance){
//            balanceDataSet.removeSeries(balanceTimeSeries);
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

    private static final SimpleDateFormat TDF = new SimpleDateFormat("dd.MM.yyyy HH:mm:ss");

    static int balance = 0;
    static int quantity = 0;

    public static void transaction(String symbol, String date) throws ParseException {
        TimeSeries balanceTimeSeries = new TimeSeries(symbol + "-" + date);
        balanceDataSet.addSeries(balanceTimeSeries);

        List<Transaction> transactions = AidaInjector.getInstance(TransactionBean.class).getTransactions(symbol, date);

        for (Transaction transaction : transactions){
            if ("Купля".equals(transaction.getType())){
                balance -= transaction.getQuantity()*transaction.getPrice();

                quantity += transaction.getQuantity();

                if (quantity == 0) {
                    balanceTimeSeries.addOrUpdate(new Minute(TDF.parse(transaction.getDate() + " " + transaction.getTime())), balance);
                }
            }else if ("Продажа".equals(transaction.getType())){
                balance += transaction.getQuantity()*transaction.getPrice();

                quantity -= transaction.getQuantity();

                if (quantity == 0) {
                    balanceTimeSeries.addOrUpdate(new Minute(TDF.parse(transaction.getDate() + " " + transaction.getTime())), balance);
                }
            }
        }
    }

    static float maxMoney = 0;
    static float minMoney = 0;

     private static void study2(String prefix, String symbol, String future, int n, int l, int p, int m, boolean average, boolean useStop, float stopFactor)
            throws RemoteVSSAException {
        currentLabel.incrementAndGet();

        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

        float money = 0;
        float balance = 0;
        float stopPrice = 0;
        int quantity = 0;

        int orderCount = 0;
        int stopCount = 0;

        boolean closeDay = false;

        Date start = null, end = null;

        List<Quote> allQuotes = quotesBean.getQuotes(symbol, COUNT + n);
        List<Quote> allFutureQuotes = quotesBean.getQuotes(future, COUNT + n);

        String name = symbol + "-" + future +"-n" + n + "l" + l +"p" + p + "m" + m + (average?"a":"c") + (useStop?"s":"") + stopFactor;

        TimeSeries balanceTimeSeries = new TimeSeries(name);
        balanceDataSet.addSeries(balanceTimeSeries);

        Calendar current = Calendar.getInstance();

        VectorForecastSSA vectorForecastSSA = new VectorForecastSSA(n, l, p, m);

        long time;

        for (int i=0; i < COUNT-1; ++i) {
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


            Quote currentQuote = allQuotes.get(n + i);
            float currentPrice = currentQuote.getOpen();
            Date currentDate = currentQuote.getDate();

            float currentFuturePrice = allFutureQuotes.get(n+i).getOpen();

            if (i == 0){
                start = currentDate;
            }else if (i == COUNT - 2){
                end = currentDate;
            }

            if ((i > 2000 && money < 0) || (i > 500 && orderCount < 2)){
                try {
                    fileWriter.append(name).append(",,,,,").append("\n");
                    balanceDataSet.removeSeries(balanceTimeSeries);
                } catch (IOException e) {
                    e.printStackTrace();
                }

                return;
            }

            //close day
            current.setTime(currentDate);
            if ((current.get(Calendar.HOUR_OF_DAY) == (symbol.equals(future) ? 23 : 18)
                    && current.get(Calendar.MINUTE) > 40) || i == COUNT - 2){

                closeDay = true;
            }

            //stop
            if ((useStop && stopPrice != 0) || closeDay) {
                if (quantity > 0 && (currentPrice < stopPrice || closeDay)){ //stop sell
                    balance +=  currentFuturePrice;

                    stopPrice = 0;
                    quantity = 0;
                    closeDay = false;
                    stopCount++;

                    money = balance;
                    balanceTimeSeries.add(new Minute(currentDate), money);
                    System.out.println("STOP_SELL, " + currentFuturePrice + ", " + dateFormat.format(currentDate) + ", " + money);

                    String t = i + ": " + prefix+balanceTimeSeries.getKey() + ", " + balance + ", " + orderCount + ", " + stopCount;

                    if (currentLabel.get()%2 == 0){
                        label.setText(t);
                    }else {
                        label1.setText(t);
                    }

                    continue;
                }else if (quantity < 0 && (currentPrice > stopPrice || closeDay)){ //stop buy
                    balance -= currentFuturePrice;

                    stopPrice = 0;
                    quantity = 0;
                    closeDay = false;
                    stopCount++;

                    money = balance;
                    balanceTimeSeries.add(new Minute(currentDate), money);
                    System.out.println("STOP_BUY, " + currentFuturePrice + ", " + dateFormat.format(currentDate) + ", " + money);

                    String t = i + ": " + prefix+balanceTimeSeries.getKey() + ", " + balance + ", " + orderCount + ", " + stopCount;

                    if (currentLabel.get()%2 == 0){
                        label.setText(t);
                    }else {
                        label1.setText(t);
                    }

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
                    System.out.println("LONG, " + currentFuturePrice + ", " + dateFormat.format(currentDate) + ", " + money);
                    balanceTimeSeries.add(new Minute(currentDate), money);

                    stopPrice = currentPrice/stopFactor;

                    balance -= currentFuturePrice;

                    orderCount++;
                    quantity = 1;
                }

                if (quantity == -1){
                    balance -= 2*currentFuturePrice;
                    stopPrice = currentPrice/stopFactor;

                    orderCount++;
                    quantity = 1;

                    money = balance + currentFuturePrice;
                    balanceTimeSeries.add(new Minute(currentDate), money);
                    System.out.println("LONG, " + currentFuturePrice + ", " + dateFormat.format(currentDate) + ", " + money);
                }
            }else if (VectorForecastUtil.isMax(forecast, n, m)
                    || VectorForecastUtil.isMax(forecast, n-1, m)
                    || VectorForecastUtil.isMax(forecast, n+1, m)){ //SHORT
                if (quantity == 0){
                    balanceTimeSeries.add(new Minute(currentDate), money);
                    System.out.println("SHORT, " + currentFuturePrice + ", " + dateFormat.format(currentDate) + ", " + money);

                    stopPrice = currentPrice/stopFactor;

                    balance += currentFuturePrice;

                    orderCount++;
                    quantity = -1;
                }

                if (quantity == 1){
                    balance += 2*currentFuturePrice;
                    stopPrice = currentPrice*stopFactor;

                    orderCount++;
                    quantity = -1;

                    money = balance - currentFuturePrice;
                    balanceTimeSeries.add(new Minute(currentDate), money);
                    System.out.println("SHORT, " + currentFuturePrice + ", " + dateFormat.format(currentDate) + ", " + money);
                }
            }

            if (timing){
                System.out.println("AlphaTraderSchool.5 " + (System.currentTimeMillis() - time));
            }
        }

        if (money >= minMoney && money < maxMoney){
            balanceDataSet.removeSeries(balanceTimeSeries);
        }else{
            if (money > maxMoney){
                maxMoney = money;
            } else if (money < minMoney){
                minMoney = money;
            }
        }

        String s = prefix+balanceTimeSeries.getKey() + "," + money + "," + orderCount + "," + stopCount + "," + dateFormat.format(start) + "," + dateFormat.format(end);

        try {
            fileWriter.append(s).append("\n");
            fileWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(s);
    }
}

