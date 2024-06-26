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
import ru.inhell.aida.algo.func.StdDev;

import javax.swing.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import static java.math.BigDecimal.ZERO;
import static java.math.BigDecimal.valueOf;
import static java.math.RoundingMode.HALF_EVEN;
import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * inheaven on 04.05.2016.
 */
public class LevelBacktest<P> {
    static class Metric{
        private double price;
        private double total;
        private Date date;

        Metric(double price, double total, Date date) {
            this.price = price;
            this.total = total;
            this.date = date;
        }
    }

    protected void start(){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 11, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 12, 8, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);

        System.out.println("trade size: " + trades.size() + "\n");

        List<LevelBacktest> levelBacktestList = new ArrayList<>();

        System.out.println("LevelBacktest startDate: " + startDate + ", endDate: " + endDate + "\n");

        //base
//        levelBacktestList.add(new LevelBacktest(10100, 60000, 20, 50000, 0, 8.45, 0.022, 0, 59000, 1, 500, 300000, false, 3, 1, 2, 50000, 1, 2, null, 1, null));
//        levelBacktestList.add(new LevelBacktest(10100, 60000, 20, 50000, 0, 8.45, 0.022, 0, 59000, 1, 500, 300000, false, 4, 1, 1, 50000, 1, 2, null, 1, null));
//        levelBacktestList.add(new LevelBacktest(10100, 60000, 20, 50000, 0, 8.45, 0.011, 0, 59000, 1, 500, 300000, false, 8, 1, 8, 50000, 1, 2, null, 1, null));

        Random random = new SecureRandom();

        //optimize
//        for (int i3 = 1; i3 <= 1; ++i3){
//            for (int i2 = 1; i2 <= 1; ++i2) {
//                for (int i1 = 0; i1 <= 0; ++i1) {
//                    for (int i0 = 1; i0 <= 100; ++i0) {
//                        levelBacktestList.add(new LevelBacktest(10100, 60000, 20, 5000, 0, 2 + i0*0.1, 0.011, 0, 1000, 1, 150, 300000, false, 5, 1, 3, 10000, 1, 2, null, 1, BID));
//                        levelBacktestList.add(new LevelBacktest(10100, 60000, 20, 5000, 0, 2 + i0*0.1, 0.011, 0, 1000, 1, 150, 300000, false, 4, 1, 10, 10000, 1, 2, null, 1, ASK));
////                        levelBacktestList.add(new LevelBacktest(9200, 60000, 20, 10000, 0, 2.5, 0.011, 0, 1000, 1, 150, 300000, false, 4, 1, 1, 1000*i0, 1, 2, null, 1, BID));
////                        levelBacktestList.add(new LevelBacktest(9200, 60000, 20, 10000, 0, 2.5, 0.011, 0, 1000, 1, 150, 300000, false, 4, 1, 1, 1000*i0, 1, 2, null, 1, ASK));
//                    }
//                }
//            }
//        }


//        for (int i = 0; i <= 100; ++i) {
//            levelBacktestList.add(new LevelBacktest(13350, 60000, 10, 10000, 0, Math.sqrt(Math.PI * 2), 0.001*i + 0.01, 0, 30000, 2, 10, 300000, false, 0, 0, 0, 0, 0, 0, null, 0, null));
//        }

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        List<Future> futures = new ArrayList<>();

        AtomicDouble bestProfit = new AtomicDouble(0);

        levelBacktestList.forEach(l -> {
            Future future = executorService.submit(() -> {
                try {
                    long time = System.currentTimeMillis();

                    //action
                    trades.forEach(l::action);

                    //clear
                    l.forecastPriceQueue.clear();
                    l.metrics.clear();
                    if (l.standardDeviation != null) {
                        l.standardDeviation.clear();
                        l.standardDeviation = null;
                    }

                    System.out.println(new Date() + " " + (System.currentTimeMillis() - time)/1000 + "s " + l.getStringRow());

                    if (bestProfit.get() == 0 || l.getProfit(l.lastActionPrice) > bestProfit.get()){
                        bestProfit.set(l.getProfit(l.lastActionPrice));

//                        System.out.println(new Date() + " " + (System.currentTimeMillis() - time)/1000 + "s " + l.getStringRow());
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

        levelBacktestList.sort((l1, l2) -> Double.compare(l2.getProfit(l2.lastActionPrice), l1.getProfit(l1.lastActionPrice)));

        System.out.println("\n Rank");

        levelBacktestList.forEach(l -> {
            System.out.println(l.getStringRow());
        });

        //chart
        XYSeries priceSeries = new XYSeries("price");
        XYSeries totalSeries = new XYSeries("total");

        //noinspection unchecked
//        levelBacktestList.get(0).metrics.forEach(m -> {
//            priceSeries.add(m.date.getTime(), m.price);
//            totalSeries.add(m.date.getTime(), m.total);
//        });

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

    private int forecastSize;
    protected P parameters;
    private int forecastError;

    private OrderDoubleMap orderMap = new OrderDoubleMap();

    private StdDev standardDeviation;

    private Deque<Metric> metrics = new LinkedList<Metric>();
    protected double total = 0d;

    private double buyPrice = 0d;
    private double buyVolume = 0d;

    private double sellPrice = 0d;
    private double sellVolume = 0d;

    private OrderType orderType;

    protected LevelBacktest(double initialTotal, long cancelDelay, double cancelRange, int spreadSize, double spreadFixed,
                            double spreadDiv, double amountLot, int amountRange, int balanceDelay, double balanceValue,
                            long tradeDelay, long metricDelay, boolean slip, int forecastSize, P parameters, OrderType orderType) {
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

        this.forecastSize = forecastSize;
        this.parameters = parameters;

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

    private boolean isBalance(Trade trade){
        if (subtotalBtc == 0){
            return true;
        }

        if (lastBalanceTime == 0 || trade.getCreated().getTime() - lastBalanceTime > balanceDelay){
            double price = trade.getPrice().doubleValue();

            double forecast = getForecast(forecastPriceQueue.stream().mapToDouble(d -> d).toArray(), parameters);

            if (Double.isNaN(forecast) || forecast == 0){
                forecast = trade.getPrice().doubleValue();

                ++forecastError;
            }else if (Math.abs(price - forecast)/price > 0.5) {
                forecast = price*(0.5*Math.signum(price - forecast) + 1);
            }

//            balance = subtotalCny > subtotalBtc*forecast;

//            balance = subtotalCny > subtotalBtc*(2*forecast - price);
            balance = forecast > price;

            lastBalanceTime = trade.getCreated().getTime();
        }

        return balance;
    }


    protected double getForecast(double[] prices, P parameters){
        return 0;
    }

    private void order(Order order){
        if (order.getType().equals(BID) && getFreeCny() > order.getAmount().multiply(order.getPrice()).doubleValue()){
            orderMap.put(order);
        }

        if (order.getType().equals(ASK) && getFreeBtc() > order.getAmount().doubleValue()){
            orderMap.put(order);
        }
    }

    private Deque<Double> forecastPriceQueue = new LinkedList<>();

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

//            if (orderType == null || orderType.equals(ASK)) {
//                //arima
//                forecastPriceQueue.add(o.getPrice().doubleValue());
//                if (forecastPriceQueue.size() > arimaSize){
//                    forecastPriceQueue.removeFirst();
//                }
//            }
        }));

        orderMap.getAskMap().headMap(price).forEach((k, v) -> v.values().forEach(o -> {
            subtotalBtc -= o.getAmount().doubleValue();
            subtotalCny += o.getAmount().multiply(o.getPrice()).doubleValue();

            double sv = sellVolume;
            sellVolume= o.getAmount().doubleValue() + sv;
            sellPrice = (sellPrice * sv  + o.getPrice().multiply(o.getAmount()).doubleValue()) / (o.getAmount().doubleValue() + sv);

            clearOrderIds.add(o.getOrderId());

//            if (orderType == null || orderType.equals(BID)) {
//                //arima
//                if (forecastPriceQueue.size() > arimaSize){
//                    forecastPriceQueue.removeFirst();
//                }
//            }
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

    public void action(Trade trade){
        //order type
        if (orderType != null && !orderType.equals(trade.getOrderType())){
            return;
        }

        double price = trade.getPrice().doubleValue();

        //stdDev
        if (spreadFixed <= 0){
            getStandardDeviation().add(price);
        }

        //forecast
        if (forecastPriceQueue.size() < forecastSize ||  Math.abs(forecastPriceQueue.peekLast() - price) > getSpread(trade)) {
            forecastPriceQueue.add(price);
            if (forecastPriceQueue.size() > forecastSize){
                forecastPriceQueue.removeFirst();
            }
        }

        trade(trade);

        if (++count < spreadSize){
            return;
        }

        cancel(trade);

        double spread = getSpread(trade);

        boolean up = isBalance(trade);

        double p = trade.getPrice().doubleValue() + (up ? spread/10 : -spread/10);
        double buyPrice = up ? p : p - spread;
        double sellPrice = up ? p + spread : p;

        if (!orderMap.contains(buyPrice, spread, BID) && !orderMap.contains(sellPrice, spread, ASK)){
            double buyAmount;
            double sellAmount;

//            double q1 = QuranRandom.nextDouble()*2;
//            double q2 = QuranRandom.nextDouble()*2;
//
//            double max = Math.max(q1, q2);
//            double min = Math.min(q1, q2);

            double max = random.nextGaussian()/2 + 2;
            double min = random.nextGaussian()/2 + 1;

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
            if (slip) {
                if (lastBuyPrice > 0 && buyPrice < lastBuyPrice){
                    buyAmount = buyAmount * Math.abs(lastBuyPrice - buyPrice) / spread;
                }
                if (lastSellPrice > 0 && sellPrice > lastSellPrice){
                    sellAmount = sellAmount * Math.abs(sellPrice - lastSellPrice) / spread;
                }
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
    
    public String getStringRow(){
        DecimalFormat df = new DecimalFormat("0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        DecimalFormat df3 = new DecimalFormat("0.000", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
        
        return  df.format(subtotalBtc * lastTradePrice.doubleValue() + subtotalCny - initialTotal) + " " +
                df.format(getProfit(lastTradePrice.doubleValue())) + " " +
                df.format(100*getProfit(lastTradePrice.doubleValue())/ initialTotal) + "% " +
                df.format(initialTotal) + " " +
                df.format(subtotalCny) + " " +
                df.format(subtotalBtc) + " " +
                cancelDelay + " " +
                cancelRange + " " +
                spreadSize + " " +
                df.format(spreadFixed) + " " +
                df.format(spreadDiv) + " " +
                df3.format(amountLot) + " " +
                amountRange + " " +
                balanceDelay + " " +
                balanceValue + " " +
                tradeDelay + " " +
                metricDelay + " " +
                df.format(buyVolume) + " " +
                df.format(sellVolume) + " " +
                slip + " " +
                forecastError + " " +
                orderType +
                parameters.toString();
    }
}


