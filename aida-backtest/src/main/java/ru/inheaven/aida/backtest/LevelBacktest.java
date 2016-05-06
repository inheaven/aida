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
import ru.inheaven.aida.happy.trading.util.OrderMap;

import javax.swing.*;
import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static ru.inheaven.aida.happy.trading.entity.OrderType.ASK;
import static ru.inheaven.aida.happy.trading.entity.OrderType.BID;

/**
 * inheaven on 04.05.2016.
 */
public class LevelBacktest {
    private class Metric{
        private BigDecimal price;
        private BigDecimal total;
        private Date date;

        public Metric(BigDecimal price, BigDecimal total, Date date) {
            this.price = price;
            this.total = total;
            this.date = date;
        }
    }

    public static void main(String[] args){
        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        Date startDate = Date.from(LocalDateTime.of(2016, 5, 6, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 5, 7, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        LevelBacktest levelBacktest = new LevelBacktest(BigDecimal.valueOf(8000), 60000, 5, 10000, 2.5, 0.1, 60000, 2);

        tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 10000).forEach(levelBacktest::action);


        //chart
        XYSeries priceSeries = new XYSeries("price");
        XYSeries totalSeries = new XYSeries("total");

        levelBacktest.metrics.forEach(m -> {
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

        NumberAxis y1 = new NumberAxis("y2");
        y1.setAutoRange(true);
        y1.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(y1);

        //y2
        plot.setDataset(1, new XYSeriesCollection(totalSeries));
        plot.setRenderer(1, renderer2);

        NumberAxis y2 = new NumberAxis("y2");
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

    private BigDecimal subtotalCny;
    private BigDecimal subtotalBtc = ZERO;
    private long cancelDelay;
    private double cancelRange;
    private int tradeSize;
    private double spreadDiv;
    private double levelAmount;
    private int balanceDelay;
    private double balanceValue;

    private OrderMap orderMap = new OrderMap(2);

    private List<Metric> metrics = new ArrayList<>();

    private BigDecimal buyPrice = ZERO;
    private BigDecimal buyVolume = ZERO;

    private BigDecimal sellPrice = ZERO;
    private BigDecimal sellVolume = ZERO;

    private LevelBacktest(BigDecimal subtotalCny, long cancelDelay, double cancelRange, int tradeSize,
                         double spreadDiv, double levelAmount, int balanceDelay, double balanceValue) {
        this.subtotalCny = subtotalCny;
        this.cancelDelay = cancelDelay;
        this.cancelRange = cancelRange;
        this.tradeSize = tradeSize;
        this.spreadDiv = spreadDiv;
        this.levelAmount = levelAmount;
        this.balanceDelay = balanceDelay;
        this.balanceValue = balanceValue;
    }

    private BigDecimal getFreeBtc(){
        return subtotalBtc.subtract(orderMap.getAskMap().values().stream()
                .flatMap(Collection::stream)
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal getFreeCny(){
        return subtotalCny.subtract(orderMap.getBidMap().values().stream()
                .flatMap(Collection::stream)
                .map(o -> o.getAmount().multiply(o.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private StandardDeviation standardDeviation = new StandardDeviation(true);

    private double getStdDev(){
        return standardDeviation.evaluate(tradeQueue.stream().mapToDouble(t -> t.getPrice().doubleValue()).toArray());
    }

    private BigDecimal getSpread(){
        double stdDev = getStdDev();

        return !Double.isNaN(stdDev) ? BigDecimal.valueOf( stdDev / spreadDiv).setScale(2, HALF_EVEN) : BigDecimal.valueOf(0.1);
    }

    private long lastBalanceTime = 0;
    private boolean balance;

    private boolean isBalance(Trade trade){
        if (subtotalBtc.equals(ZERO)){
            return true;
        }

        if (lastBalanceTime == 0 || trade.getCreated().getTime() - lastBalanceTime > balanceDelay){
            balance = subtotalCny.divide(subtotalBtc.multiply(trade.getPrice()), 8, HALF_EVEN).compareTo(BigDecimal.valueOf(balanceValue)) > 0;

            lastBalanceTime = trade.getCreated().getTime();
        }

        return balance;
    }

    private void order(Order order){
        if (order.getType().equals(BID) && getFreeCny().compareTo(order.getAmount().multiply(order.getPrice())) > 0){
            orderMap.put(order);
        }

        if (order.getType().equals(ASK) && getFreeBtc().compareTo(order.getAmount()) > 0){
            orderMap.put(order);
        }
    }

    private Deque<Trade> tradeQueue = new LinkedList<>();
    private long lastTradeTime = 0;

    private void trade(Trade trade){
        if (tradeQueue.size() > tradeSize){
            tradeQueue.removeFirst();
        }

        tradeQueue.add(trade);

        if (lastTradeTime > 0 && (trade.getCreated().getTime() - lastTradeTime < 200)){
            return;
        }

        orderMap.getBidMap().tailMap(trade.getPrice()).forEach((k, v) -> v.forEach(o -> {
            subtotalBtc = subtotalBtc.add(o.getAmount());
            subtotalCny = subtotalCny.subtract(o.getAmount().multiply(o.getPrice()));

            orderMap.remove(o.getOrderId());

            BigDecimal bv = buyVolume;
            buyVolume = o.getAmount().add(bv);
            buyPrice = buyPrice.multiply(bv).add(o.getPrice().multiply(o.getAmount())).divide(o.getAmount().add(bv), 8, HALF_EVEN);
        }));

        orderMap.getAskMap().headMap(trade.getPrice()).forEach((k, v) -> v.forEach(o -> {
            subtotalBtc = subtotalBtc.subtract(o.getAmount());
            subtotalCny = subtotalCny.add(o.getAmount().multiply(o.getPrice()));

            orderMap.remove(o.getOrderId());

            BigDecimal sv = sellVolume;
            sellVolume= o.getAmount().add(sv);
            sellPrice = sellPrice.multiply(sv).add(o.getPrice().multiply(o.getAmount())).divide(o.getAmount().add(sv), 8, HALF_EVEN);
        }));

        lastTradeTime = trade.getCreated().getTime();
    }

    private long lastCancelTime = 0;

    private void cancel(Trade trade){
        if (lastCancelTime > 0 && (trade.getCreated().getTime() - lastCancelTime < cancelDelay)){
            return;
        }

        BigDecimal range = getSpread().multiply(BigDecimal.valueOf(cancelRange));

        orderMap.getBidMap().headMap(trade.getPrice().subtract(range))
                .forEach((k, v) -> v.forEach(o -> orderMap.remove(o.getOrderId())));

        orderMap.getAskMap().tailMap(trade.getPrice().add(range))
                .forEach((k, v) -> v.forEach(o -> orderMap.remove(o.getOrderId())));

        lastCancelTime = trade.getCreated().getTime();
    }

    private void metric(Trade trade){
        metrics.add(new Metric(trade.getPrice(), subtotalBtc.multiply(trade.getPrice()).add(subtotalCny), trade.getCreated()));
    }

    private SecureRandom random = new SecureRandom();
    private long idGen = 0;

    private void action(Trade trade){
        boolean up = isBalance(trade);
        BigDecimal spread = getSpread();

        BigDecimal p = trade.getPrice();
        BigDecimal buyPrice = up ? p : p.subtract(spread);
        BigDecimal sellPrice = up ? p.add(spread) : p;

        if (!orderMap.contains(buyPrice, spread, BID) && !orderMap.contains(sellPrice, spread, ASK)){
            double buyAmount = (up ? random.nextGaussian()/2 + 2 : random.nextGaussian()/2 + 1) * levelAmount;
            double sellAmount = (up ? random.nextGaussian()/2 + 1 : random.nextGaussian()/2 + 2) * levelAmount;

            if (buyAmount < 0.01){
                buyAmount = 0.01;
            }
            if (sellAmount < 0.01){
                sellAmount = 0.01;
            }

            order(new Order(String.valueOf(++idGen), BID, buyPrice, BigDecimal.valueOf(buyAmount).setScale(3, HALF_EVEN)));
            order(new Order(String.valueOf(++idGen), ASK, sellPrice, BigDecimal.valueOf(sellAmount).setScale(3, HALF_EVEN)));

            metric(trade);
        }

        trade(trade);
        cancel(trade);
    }


}

