package ru.inheaven.aida.backtest;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.util.OrderDoubleMap;
import ru.inheaven.aida.happy.trading.util.QuranRandom;

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
        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        Date startDate = Date.from(LocalDateTime.of(2016, 5, 7, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 5, 8, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        List<LevelBacktest> levelBacktestList = new ArrayList<>();

        for (int i = 1; i < 2; ++i){
            for (int j = 1; j < 2; ++j) {
                for (int k = 1; k < 20; ++k) {
                    for (int l = 1; l < 20; ++l) {
                        levelBacktestList.add(new LevelBacktest(34242, 60000, 20, 5000*k, 0.5*l, 0, 10, 60000, 1, 500, 60000));
                    }
                }
            }
        }

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);

        System.out.println("trade size: " + trades.size());

        ExecutorService executorService = Executors.newFixedThreadPool(2);

        List<Future> futures = new ArrayList<>();

        DecimalFormat df = new DecimalFormat("#.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));

        levelBacktestList.forEach(l -> {
            Future future = executorService.submit(() -> {
                long time = System.currentTimeMillis();

                System.out.println(new Date() + " start " + l);

                trades.forEach(l::action);

                System.out.println(new Date() + " finish " +
                        (System.currentTimeMillis() - time)/1000 + "s " +
                        df.format(l.metrics.getLast().total) + " " +
                        df.format(l.getProfit(l.metrics.getLast().price)) + " " +
                        l + "\n");
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

        levelBacktestList.sort((l1, l2) -> Double.compare(l2.metrics.getLast().total, l1.metrics.getLast().total));

        levelBacktestList.forEach(l -> {
            System.out.println("" +
                    df.format(l.subtotalCny) + " " +
                    df.format(l.subtotalBtc) + " " +
                    l.cancelDelay + " " +
                    l.cancelRange + " " +
                    l.tradeSize + " " +
                    l.spreadDiv + " " +
                    l.amountLevel + " " +
                    l.amountRange + " " +
                    l.balanceDelay + " " +
                    l.balanceValue + " " +
                    l.tradeDelay + " " +
                    l.metricDelay + " " +
                    df.format(l.metrics.getLast().total) + " " +
                    df.format(l.getProfit(l.metrics.getLast().price))
            );
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

    private double subtotalCny;
    private double subtotalBtc = 0d;
    private long cancelDelay;
    private double cancelRange;
    private int tradeSize;
    private double spreadDiv;
    private double amountLevel;
    private int amountRange;
    private int balanceDelay;
    private double balanceValue;

    private long tradeDelay;

    private OrderDoubleMap orderMap = new OrderDoubleMap();

    private long metricDelay;
    private Deque<Metric> metrics = new LinkedList<>();

    private double buyPrice = 0d;
    private double buyVolume = 0d;

    private double sellPrice = 0d;
    private double sellVolume = 0d;

    private LevelBacktest(double subtotalCny, long cancelDelay, double cancelRange, int tradeSize,
                          double spreadDiv, double amountLevel, int amountRange, int balanceDelay, double balanceValue,
                          long tradeDelay, long metricDelay) {
        this.subtotalCny = subtotalCny;
        this.cancelDelay = cancelDelay;
        this.cancelRange = cancelRange;
        this.tradeSize = tradeSize;
        this.spreadDiv = spreadDiv;
        this.amountLevel = amountLevel;
        this.amountRange = amountRange;
        this.balanceDelay = balanceDelay;
        this.balanceValue = balanceValue;
        this.tradeDelay = tradeDelay;
        this.metricDelay = metricDelay;
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

    private StandardDeviation standardDeviation = new StandardDeviation(true);

    private double stdDev;
    private long lastStdDevTime = 0;

    private double getStdDev(Trade trade){
        if (lastStdDevTime == 0 || trade.getCreated().getTime() - lastStdDevTime > 60000){
            stdDev = standardDeviation.evaluate(tradePriceQueue.stream().mapToDouble(d -> d).toArray());

            lastStdDevTime = trade.getCreated().getTime();
        }

        return !Double.isNaN(stdDev) ? stdDev : 0.1;
    }

    private double getSpread(Trade trade){
        double stdDev = getStdDev(trade);

        return  stdDev / spreadDiv;
    }

    private long lastBalanceTime = 0;
    private boolean balance;

    private boolean isBalance(Trade trade){
        if (subtotalBtc == 0){
            return true;
        }

        if (lastBalanceTime == 0 || trade.getCreated().getTime() - lastBalanceTime > balanceDelay){
            balance = subtotalCny / (subtotalBtc * trade.getPrice().doubleValue()) > balanceValue;

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

    private Deque<Double> tradePriceQueue = new LinkedList<>();
    private long lastTradeTime = 0;

    private void trade(Trade trade){
        //queue
        if (tradePriceQueue.size() > tradeSize){
            tradePriceQueue.removeFirst();
        }

        tradePriceQueue.add(trade.getPrice().doubleValue());

        //trade
        if (lastTradeTime > 0 && (trade.getCreated().getTime() - lastTradeTime < tradeDelay)){
            return;
        }

        orderMap.getBidMap().tailMap(trade.getPrice().doubleValue()).forEach((k, v) -> v.values().forEach(o -> {
            subtotalBtc += o.getAmount().doubleValue();
            subtotalCny -= o.getAmount().multiply(o.getPrice()).doubleValue();

            orderMap.remove(o.getOrderId());

            double bv = buyVolume;
            buyVolume = o.getAmount().doubleValue() + bv;
            buyPrice = (buyPrice * bv  + o.getPrice().multiply(o.getAmount()).doubleValue()) / (o.getAmount().doubleValue() + bv);
        }));

        orderMap.getAskMap().headMap(trade.getPrice().doubleValue()).forEach((k, v) -> v.values().forEach(o -> {
            subtotalBtc -= o.getAmount().doubleValue();
            subtotalCny += o.getAmount().multiply(o.getPrice()).doubleValue();

            orderMap.remove(o.getOrderId());

            double sv = sellVolume;
            sellVolume= o.getAmount().doubleValue() + sv;
            sellPrice = (sellPrice * sv  + o.getPrice().multiply(o.getAmount()).doubleValue()) / (o.getAmount().doubleValue() + sv);
        }));

        lastTradeTime = trade.getCreated().getTime();
    }

    private long lastCancelTime = 0;

    private void cancel(Trade trade){
        if (lastCancelTime > 0 && (trade.getCreated().getTime() - lastCancelTime < cancelDelay)){
            return;
        }

        double range = getSpread(trade) * cancelRange;

        orderMap.getBidMap().headMap(trade.getPrice().doubleValue() - range)
                .forEach((k, v) -> v.values().forEach(o -> orderMap.remove(o.getOrderId())));

        orderMap.getAskMap().tailMap(trade.getPrice().doubleValue() + range)
                .forEach((k, v) -> v.values().forEach(o -> orderMap.remove(o.getOrderId())));

        lastCancelTime = trade.getCreated().getTime();
    }

    private long lastMetricTime = 0;

    private void metric(Trade trade){
        if (lastMetricTime > 0 && trade.getCreated().getTime() - lastMetricTime < metricDelay){
            return;
        }

        metrics.add(new Metric(trade.getPrice().doubleValue(), (subtotalBtc * trade.getPrice().doubleValue()) + subtotalCny, trade.getCreated()));

        lastMetricTime = trade.getCreated().getTime();
    }

    private SecureRandom random = new SecureRandom();
    private long idGen = 0;

    private double lastBuyPrice = 0;
    private double lastSellPrice = 0;

    private BigDecimal lastActionPrice = ZERO;

    private void action(Trade trade){
        if (lastActionPrice.equals(trade.getPrice())){
            return;
        }

        boolean up = isBalance(trade);
        double spread = getSpread(trade);

        double p = trade.getPrice().doubleValue();
        double buyPrice = up ? p : p - spread;
        double sellPrice = up ? p + spread : p;

        if (!orderMap.contains(buyPrice, spread, BID) && !orderMap.contains(sellPrice, spread, ASK)){
            double amount = (subtotalCny / p  + subtotalBtc) / (getStdDev(trade) * amountRange / getSpread(trade));

            //amount
            double buyAmount = (up ? QuranRandom.nextDouble() : 0) * amount;
            double sellAmount = (up ? 0 : QuranRandom.nextDouble()) * amount;

            //slip
            if (lastBuyPrice > 0 && buyPrice < lastBuyPrice){
                buyAmount = buyAmount * Math.abs(lastBuyPrice - buyPrice) / spread;
            }
            if (lastSellPrice > 0 && sellPrice > lastSellPrice){
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
        }

        trade(trade);
        cancel(trade);

        lastActionPrice = trade.getPrice();
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
                ", tradeSize=" + tradeSize +
                ", spreadDiv=" + spreadDiv +
                ", amountLevel=" + amountLevel +
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
}


