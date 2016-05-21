package ru.inheaven.aida.backtest;

import com.google.common.util.concurrent.AtomicDouble;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.util.OrderDoubleMap;
import ru.inhell.aida.algo.arima.ArimaFitter;
import ru.inhell.aida.algo.arima.ArimaProcess;
import ru.inhell.aida.algo.arima.DefaultArimaForecaster;
import ru.inhell.aida.algo.func.StdDev;

import javax.swing.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_EVEN;
import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;
import static ru.inhell.aida.algo.arima.ArimaFitter.fit;

/**
 * inheaven on 04.05.2016.
 */
public class LevelBacktest {
    private class Metric{
        private double price;
        private double total;
        private Date date;

        Metric(double price, double total, Date date) {
            this.price = price;
            this.total = total;
            this.date = date;
        }
    }

    public static void main(String[] args){
        Date startDate = Date.from(LocalDateTime.of(2016, 5, 20, 9, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 5, 21, 9, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 2000000);

        System.out.println("trade size: " + trades.size() + "\n");

        List<LevelBacktest> levelBacktestList = new ArrayList<>();

        System.out.println("LevelBacktest startDate: " + startDate + ", endDate: " + endDate + "\n");

        //base
//        levelBacktestList.add(new LevelBacktest(17650, 60000, 20, 50000, 0, 8.45, 0.022, 0, 59000, 1, 500, 300000, false, 3, 1, 2, 50000, 1, 2, null, null));
//        levelBacktestList.add(new LevelBacktest(17650, 60000, 20, 50000, 0, 8.45, 0.022, 0, 59000, 1, 500, 300000, false, 4, 1, 1, 50000, 1, 2, null, null));
//        levelBacktestList.add(new LevelBacktest(17650, 60000, 20, 50000, 0, 8.45, 0.022, 0, 59000, 1, 500, 300000, false, 8, 1, 1, 50000, 1, 2, null, null));

        //optimize
        for (int i3 = 1; i3 <= 1; ++i3){
            for (int i2 = 1; i2 <= 10; ++i2) {
                for (int i1 = 1; i1 <= 10; ++i1) {
                    for (int i0 = 1; i0 <= 10; ++i0) {
                        levelBacktestList.add(new LevelBacktest(17680, 60000, 20, 50000, 0.10, 0, 1, 0, 1000, 1, 50000, 300000, false, i0, i1, i1, 50000, 1, 2, null, 1, BID));
                        levelBacktestList.add(new LevelBacktest(17680, 60000, 20, 50000, 0.10, 0, 1, 0, 1000, 1, 50000, 300000, false, i0, i1, i1, 50000, 1, 2, null, 1, ASK));
                    }
                }
            }
        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        List<Future> futures = new ArrayList<>();

        AtomicDouble bestTotal = new AtomicDouble(0);

        levelBacktestList.forEach(l -> {
            Future future = executorService.submit(() -> {
                try {
                    long time = System.currentTimeMillis();

                    //action
                    trades.forEach(l::action);

                    //clear
                    l.arimaPriceQueue.clear();
                    l.metrics.clear();
                    if (l.standardDeviation != null) {
                        l.standardDeviation.clear();
                        l.standardDeviation = null;
                    }

                    System.out.println(new Date() + " " + (System.currentTimeMillis() - time)/1000 + "s " + l.getStringRow() + " " +  (l.total - bestTotal.get()));

                    if (bestTotal.get() == 0 || l.total > bestTotal.get()){
                        bestTotal.set(l.total);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });

            futures.add(future);
        });

        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        levelBacktestList.sort((l1, l2) -> Double.compare(l2.total, l1.total));

        System.out.println("\n Rank");

        levelBacktestList.forEach(l -> {
            System.out.println(l.getStringRow());
        });

        //chart
        XYSeries priceSeries = new XYSeries("price");
        XYSeries totalSeries = new XYSeries("total");

        levelBacktestList.get(0).metrics.forEach(m -> {
            priceSeries.add(m.date.getTime(), m.price);
            totalSeries.add(m.date.getTime(), m.total);
        });

        XYPlot plot = new XYPlot();
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer(true, false);
        XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer(true, false);

        //y1
        plot.setDataset(0, new XYSeriesCollection(priceSeries));
        plot.setRenderer(0, renderer1);
        DateAxis domainAxis = new DateAxis("Date");
        plot.setDomainAxis(domainAxis);

        NumberAxis y1 = new NumberAxis("price");
        y1.setAutoRange(true);
        y1.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(y1);

        //y2
        plot.setDataset(1, new XYSeriesCollection(totalSeries));
        plot.setRenderer(1, renderer2);

        NumberAxis y2 = new NumberAxis("total");
        y2.setAutoRange(true);
        y2.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(1, y2);

        plot.mapDatasetToRangeAxis(0, 0);
        plot.mapDatasetToRangeAxis(1, 1);

        JFreeChart chart = new JFreeChart("LevelBacktest", plot);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
    }

    private double initialTotal;
    private double subtotalCny;
    private double subtotalBtc = -1.0;
    private long cancelDelay;
    private double cancelRange;
    private int spreadSize;
    private double spreadFixed;
    private double spreadDiv;
    private double amountLot;
    private int amountRange;
    private int balanceDelay;
    private double balanceValue;
    private long tradeDelay;
    private long metricDelay;
    private boolean slip;

    private int p, d, q, arimaSize, arimaNext, arimaFilter;
    private double arimaCoef;
    private ArimaProcess arimaProcess;

    private OrderDoubleMap orderMap = new OrderDoubleMap();

    private StdDev standardDeviation;

    private Deque<Metric> metrics = new LinkedList<>();
    private double total = 0d;

    private double buyPrice = 0d;
    private double buyVolume = 0d;

    private double sellPrice = 0d;
    private double sellVolume = 0d;

    private int arimaError = 0;

    private OrderType orderType;

    private LevelBacktest(double initialTotal, long cancelDelay, double cancelRange, int spreadSize, double spreadFixed,
                          double spreadDiv, double amountLot, int amountRange, int balanceDelay, double balanceValue,
                          long tradeDelay, long metricDelay, boolean slip, int p, int d, int q, int arimaSize, int arimaNext,
                          int arimaFilter, ArimaProcess arimaProcess, double arimaCoef, OrderType orderType) {
        this.initialTotal = initialTotal;

        this.cancelDelay = cancelDelay;
        this.cancelRange = cancelRange;
        this.spreadSize = spreadSize;
        this.spreadFixed = spreadFixed;
        this.spreadDiv = spreadDiv;
        this.amountLot = amountLot;
        this.amountRange = amountRange;
        this.balanceDelay = balanceDelay;
        this.balanceValue = balanceValue;
        this.tradeDelay = tradeDelay;
        this.metricDelay = metricDelay;
        this.slip = slip;

        this.p = p;
        this.d = d;
        this.q = q;
        this.arimaSize = arimaSize;
        this.arimaCoef = arimaCoef;
        this.arimaNext = arimaNext;
        this.arimaFilter = arimaFilter;
        this.arimaProcess = arimaProcess;
        this.orderType = orderType;
    }

    private Double getFreeBtc(){
        return subtotalBtc - (orderMap.getAskMap().values().stream()
                .flatMap(m -> m.values().stream())
                .mapToDouble(o -> o.getAmount().doubleValue())
                .sum());
    }

    private Double getFreeCny(){
        return subtotalCny - (orderMap.getBidMap().values().stream()
                .flatMap(m -> m.values().stream())
                .mapToDouble(o -> o.getAmount().multiply(o.getPrice()).doubleValue())
                .sum());
    }

    private double stdDev;
    private long lastStdDevTime = 0;

    private double getStdDev(Trade trade){
        if (lastStdDevTime == 0 || trade.getCreated().getTime() - lastStdDevTime > 60000){
            stdDev = getStandardDeviation().stdDev();

            lastStdDevTime = trade.getCreated().getTime();
        }

        return !Double.isNaN(stdDev) && stdDev > 0 ? stdDev : 0.1;
    }

    private double getSpread(Trade trade){
        if (spreadFixed > 0){
            return spreadFixed;
        }else{
            double stdDev = getStdDev(trade);

            double spread = stdDev / spreadDiv;

            return  spread > 0.1 ? spread : 0.1;
        }
    }

    private long lastBalanceTime = 0;
    private boolean balance;

    Deque<ArimaTest.Train> trains = new ConcurrentLinkedDeque<>();

    private boolean isBalance(Trade trade){
        if (subtotalBtc == 0){
            return true;
        }

        double forecast = 0;

        if (lastBalanceTime == 0 || trade.getCreated().getTime() - lastBalanceTime > balanceDelay){
            double price = trade.getPrice().doubleValue();

            if (arimaSize > 0 && arimaPriceQueue.size() >= arimaSize) {
                double[] prices;

                if (arimaFilter == 3){
                    prices = arimaPriceQueue.stream().mapToDouble(d -> d).toArray();

                    int trainSize = (int) (prices.length*2/Math.PI);
                    int trainStart = prices.length - trainSize - 3;

                    double[] train = new double[trainSize];

                    for (int j = 0; j < trainSize; ++j){
                        train[j] = prices[trainStart + j + 1]/prices[trainStart + j] - 1;
                    }

                    ArimaTest.Train t = new ArimaTest.Train();
                    t.process = ArimaFitter.fit(train, p, d, q);

                    double check = prices[prices.length - 1]/prices[prices.length - 2] - 1;
                    forecast =  new DefaultArimaForecaster(t.process, train).next();
                    t.check = check*forecast > 0 && Math.abs(forecast) > Math.abs(check);

                    if (t.check) {
                        trains.add(t);

                        if (trains.size() > 11){
                            trains.removeFirst();
                        }
                    }

                    double[] predictsDelta = new double[trainSize];
                    trainStart = prices.length - trainSize - 2;
                    for (int i = 0; i < trainSize; ++i){
                        predictsDelta[i] = prices[trainStart + i + 1]/prices[trainStart + i] - 1;
                    }

                    trains.forEach(t1 -> t1.forecast = new DefaultArimaForecaster(t1.process, predictsDelta).next());

                    double f = trains.parallelStream().mapToDouble(t1 -> t1.forecast).average().orElse(0);

                    price = prices[prices.length -1] * (f + 1);
                }else {
                    if (arimaFilter == 1) {
                        prices = arimaPriceQueue.stream().mapToDouble(Math::log).toArray();
                    } else if (arimaFilter == 2) {
                        prices = arimaPriceQueue.stream().mapToDouble(d -> d).toArray();

                        double[] pricesDelta = new double[prices.length - 1];

                        for (int i = 0; i < prices.length - 1; ++i) {
                            pricesDelta[i] = prices[i+1]/prices[i] - 1;
                        }

                        prices = pricesDelta;
                    } else {
                        prices = arimaPriceQueue.stream().mapToDouble(d -> d).toArray();
                    }

                    forecast = new DefaultArimaForecaster(arimaProcess != null ? arimaProcess : fit(prices, p, d, q), prices).next(arimaNext)[arimaNext - 1];
                }

                //error
                double tradePrice = trade.getPrice().doubleValue();
                if (Double.isNaN(price) || Math.abs(price - tradePrice) > tradePrice){
                    price = tradePrice;

                    ++arimaError;
                }
            }

            balance = subtotalCny / (subtotalBtc * price) > balanceValue*(1 - forecast);

            lastBalanceTime = trade.getCreated().getTime();
        }

        return balance;
    }

    private void order(Order order){
        if (order.getType().equals(BID) && getFreeCny() > order.getAmount().multiply(order.getPrice()).doubleValue()){
            orderMap.put(order);
        }

        if (order.getType().equals(ASK) && getFreeBtc() > order.getAmount().doubleValue()){
            orderMap.put(order);
        }
    }

    private Deque<Double> arimaPriceQueue = new LinkedList<>();

    private long lastTradeTime = 0;
    private BigDecimal lastTradePrice = ZERO;

    private List<String> clearOrderIds = new ArrayList<>();

    private StdDev getStandardDeviation(){
        if (standardDeviation == null){
            standardDeviation = new StdDev(spreadSize);
        }

        return standardDeviation;
    }

    private void trade(Trade trade){
        double price = trade.getPrice().doubleValue();

        //init
        if (subtotalBtc == -1.0){
            subtotalCny = initialTotal*balanceValue/(1+balanceValue);
            subtotalBtc = initialTotal/(trade.getPrice().doubleValue()*(1+balanceValue));
        }

        //stdDev
        if (spreadFixed <= 0){
            getStandardDeviation().add(price);
        }

        //arima
        arimaPriceQueue.add(price);
        if (arimaPriceQueue.size() > arimaSize){
            arimaPriceQueue.removeFirst();
        }

        //trade
        if (lastTradeTime > 0 && (trade.getCreated().getTime() - lastTradeTime < tradeDelay)){
            return;
        }

        orderMap.getBidMap().tailMap(price).forEach((k, v) -> v.values().forEach(o -> {
            subtotalBtc += o.getAmount().doubleValue();
            subtotalCny -= o.getAmount().multiply(o.getPrice()).doubleValue();

            double bv = buyVolume;
            buyVolume = o.getAmount().doubleValue() + bv;
            buyPrice = (buyPrice * bv  + o.getPrice().multiply(o.getAmount()).doubleValue()) / (o.getAmount().doubleValue() + bv);

            clearOrderIds.add(o.getOrderId());
        }));

        orderMap.getAskMap().headMap(price).forEach((k, v) -> v.values().forEach(o -> {
            subtotalBtc -= o.getAmount().doubleValue();
            subtotalCny += o.getAmount().multiply(o.getPrice()).doubleValue();

            double sv = sellVolume;
            sellVolume= o.getAmount().doubleValue() + sv;
            sellPrice = (sellPrice * sv  + o.getPrice().multiply(o.getAmount()).doubleValue()) / (o.getAmount().doubleValue() + sv);

            clearOrderIds.add(o.getOrderId());
        }));

        clearOrderIds.forEach(orderMap::remove);
        clearOrderIds.clear();

        lastTradeTime = trade.getCreated().getTime();
        lastTradePrice = trade.getPrice();
    }

    private long lastCancelTime = 0;

    private void cancel(Trade trade){
        if (lastCancelTime > 0 && (trade.getCreated().getTime() - lastCancelTime < cancelDelay)){
            return;
        }

        double range = getSpread(trade) * cancelRange;

        orderMap.getBidMap().headMap(trade.getPrice().doubleValue() - range)
                .forEach((k, v) -> v.values().forEach(o -> clearOrderIds.add(o.getOrderId())));

        orderMap.getAskMap().tailMap(trade.getPrice().doubleValue() + range)
                .forEach((k, v) -> v.values().forEach(o -> clearOrderIds.add(o.getOrderId())));

        clearOrderIds.forEach(orderMap::remove);
        clearOrderIds.clear();

        lastCancelTime = trade.getCreated().getTime();
    }

    private long lastMetricTime = 0;

    private void metric(Trade trade){
        if (lastMetricTime > 0 && trade.getCreated().getTime() - lastMetricTime < metricDelay){
            return;
        }

        total = (subtotalBtc * trade.getPrice().doubleValue()) + subtotalCny;
        metrics.add(new Metric(trade.getPrice().doubleValue(), total, trade.getCreated()));

        lastMetricTime = trade.getCreated().getTime();
    }

    private SecureRandom random = new SecureRandom();
    private long idGen = 0;

    private double lastBuyPrice = 0;
    private double lastSellPrice = 0;

    private double lastActionPrice = 0;

    private int count = 0;

    private void action(Trade trade){
        //order type
        if (orderType != null && !orderType.equals(trade.getOrderType())){
            return;
        }

        trade(trade);

        if (++count < spreadSize){
            return;
        }

        cancel(trade);

        double p = trade.getPrice().doubleValue();
        double spread = getSpread(trade);

        if (Math.abs(lastActionPrice - p) < spread/2){
            return;
        }

        boolean up = isBalance(trade);

        double buyPrice = up ? p : p - spread;
        double sellPrice = up ? p + spread : p;

        if (!orderMap.contains(buyPrice, spread, BID) && !orderMap.contains(sellPrice, spread, ASK)){
            double buyAmount;
            double sellAmount;

//            double q1 = QuranRandom.nextDouble();
//            double q2 = QuranRandom.nextDouble();
//
//            double max = Math.max(q1, q2);
//            double min = Math.min(q1, q2);

            double max = 0.02;
            double min = 0.01;

            if (amountLot == 0 ){
                //amount range
                double amount = (subtotalCny / p  + subtotalBtc) / (spreadDiv * amountRange);

                buyAmount = (up ? max : min) * amount;
                sellAmount = (up ? min : max) * amount;
            }else{
                //amount
                buyAmount = (up ? max : min) * amountLot;
                sellAmount = (up ? min : max) * amountLot;
            }

            //slip
            if (lastBuyPrice > 0 && (slip || buyPrice < lastBuyPrice)){
                buyAmount = buyAmount * Math.abs(lastBuyPrice - buyPrice) / spread;
            }
            if (lastSellPrice > 0 && (slip || sellPrice > lastSellPrice)){
                sellAmount = sellAmount * Math.abs(sellPrice - lastSellPrice) / spread;
            }

            lastBuyPrice = buyPrice;
            lastSellPrice = sellPrice;

            //less
            if (buyAmount < 0.01){
                buyAmount = 0.01;
            }
            if (sellAmount < 0.01){
                sellAmount = 0.01;
            }

            //order
            order(new Order(String.valueOf(++idGen), BID, valueOf(buyPrice), valueOf(buyAmount).setScale(3, HALF_EVEN)));
            order(new Order(String.valueOf(++idGen), ASK, valueOf(sellPrice), valueOf(sellAmount).setScale(3, HALF_EVEN)));

            metric(trade);

            lastActionPrice = p;
        }
    }

    private Double getProfit(double price) {
        return (Math.min(sellVolume, buyVolume) * (sellPrice - buyPrice)) +
               (Math.abs(buyVolume - sellVolume) * (buyVolume > sellVolume ? price - buyPrice : sellPrice - price));
    }

    @Override
    public String toString() {
        return "LevelBacktest{" +
                "subtotalCny=" + subtotalCny +
                ", subtotalBtc=" + subtotalBtc +
                ", cancelDelay=" + cancelDelay +
                ", cancelRange=" + cancelRange +
                ", tradeSize=" + spreadSize +
                ", spreadDiv=" + spreadDiv +
                ", amountLot=" + amountLot +
                ", amountRange=" + amountRange +
                ", balanceDelay=" + balanceDelay +
                ", balanceValue=" + balanceValue +
                ", tradeDelay=" + tradeDelay +
                ", metricDelay=" + metricDelay +
                ", buyPrice=" + buyPrice +
                ", buyVolume=" + buyVolume +
                ", sellPrice=" + sellPrice +
                ", sellVolume=" + sellVolume +
                ", stdDev=" + stdDev +
                ", lastStdDevTime=" + lastStdDevTime +
                ", lastBalanceTime=" + lastBalanceTime +
                ", balance=" + balance +
                ", lastTradeTime=" + lastTradeTime +
                ", lastCancelTime=" + lastCancelTime +
                ", lastMetricTime=" + lastMetricTime +
                ", lastBuyPrice=" + lastBuyPrice +
                ", lastSellPrice=" + lastSellPrice +
                ", lastActionPrice=" + lastActionPrice +
                '}';
    }
    
    private String getStringRow(){
        DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        
        return  df.format(subtotalBtc * lastTradePrice.doubleValue() + subtotalCny - initialTotal) + " " +
                df.format(getProfit(lastTradePrice.doubleValue())) + " " +
                df.format(100*getProfit(lastTradePrice.doubleValue())/ initialTotal) + "% " +
                df.format(subtotalCny) + " " +
                df.format(subtotalBtc) + " " +
                cancelDelay + " " +
                cancelRange + " " +
                spreadSize + " " +
                spreadFixed + " " +
                spreadDiv + " " +
                amountLot + " " +
                amountRange + " " +
                balanceDelay + " " +
                balanceValue + " " +
                tradeDelay + " " +
                metricDelay + " " +
                df.format(buyVolume) + " " +
                df.format(sellVolume) + " " +
                slip + " " +
                p + " " +
                d + " " +
                q + " " +
                arimaSize + " " +
                arimaNext + " " +
                arimaFilter + " " +
                (arimaProcess != null) + " " +
                arimaError + " " +
                arimaCoef + " " +
                orderType;
    }
}


