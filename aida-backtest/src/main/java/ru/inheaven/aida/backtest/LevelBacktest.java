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
import static java.math.RoundingMode.HALF_UP;
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

        Date startDate = Date.from(LocalDateTime.of(2016, 5, 6, 20, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 5, 7, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        List<LevelBacktest> levelBacktestList = new ArrayList<>();

        for (int i = 1; i < 20; ++i){
            for (int j = 1; j < 20; ++j) {
                for (int k = 1; k < 20; ++k) {
                    for (int l =1; l < 20; ++l) {
                        levelBacktestList.add(new LevelBacktest(BigDecimal.valueOf(34213), 60000, 20, 1000*k, 0.5*l, 0.01*i, 5000*j, 1, 1000, 60000));
                    }
                }
            }
        }

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 1000000);

        levelBacktestList.parallelStream().forEach(l -> {
            System.out.println(new Date() + " start " + l);

            long time = System.currentTimeMillis();

            trades.forEach(l::action);

            System.out.println(new Date() + " finish " +
                    (System.currentTimeMillis() - time)/1000 + "s " +
                    l.metrics.getLast().total.setScale(2, HALF_EVEN) + " " +
                    l.getProfit(l.metrics.getLast().price).setScale(2, HALF_EVEN) +
                    l);
        });

        levelBacktestList.sort((l1, l2) -> l2.metrics.getLast().total.compareTo(l1.metrics.getLast().total));

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

    private BigDecimal subtotalCny;
    private BigDecimal subtotalBtc = ZERO;
    private long cancelDelay;
    private double cancelRange;
    private int tradeSize;
    private double spreadDiv;
    private double levelAmount;
    private int balanceDelay;
    private double balanceValue;

    private long tradeDelay;

    private OrderMap orderMap = new OrderMap(2);

    private long metricDelay;
    private Deque<Metric> metrics = new LinkedList<>();

    private BigDecimal buyPrice = ZERO;
    private BigDecimal buyVolume = ZERO;

    private BigDecimal sellPrice = ZERO;
    private BigDecimal sellVolume = ZERO;

    private LevelBacktest(BigDecimal subtotalCny, long cancelDelay, double cancelRange, int tradeSize,
                         double spreadDiv, double levelAmount, int balanceDelay, double balanceValue, long tradeDelay, long metricDelay) {
        this.subtotalCny = subtotalCny;
        this.cancelDelay = cancelDelay;
        this.cancelRange = cancelRange;
        this.tradeSize = tradeSize;
        this.spreadDiv = spreadDiv;
        this.levelAmount = levelAmount;
        this.balanceDelay = balanceDelay;
        this.balanceValue = balanceValue;
        this.tradeDelay = tradeDelay;
        this.metricDelay = metricDelay;
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

    private double stdDev;
    private long lastStdDevTime = 0;

    private double getStdDev(Trade trade){
        if (lastStdDevTime == 0 || trade.getCreated().getTime() - lastStdDevTime > 60000){
            stdDev = standardDeviation.evaluate(tradeQueue.stream().mapToDouble(t -> t.getPrice().doubleValue()).toArray());

            lastStdDevTime = trade.getCreated().getTime();
        }

        return stdDev;
    }

    private BigDecimal getSpread(Trade trade){
        double stdDev = getStdDev(trade);

        return !Double.isNaN(stdDev) ? BigDecimal.valueOf(stdDev / spreadDiv).setScale(2, HALF_EVEN) : BigDecimal.valueOf(0.1);
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

        if (lastTradeTime > 0 && (trade.getCreated().getTime() - lastTradeTime < tradeDelay)){
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

        BigDecimal range = getSpread(trade).multiply(BigDecimal.valueOf(cancelRange));

        orderMap.getBidMap().headMap(trade.getPrice().subtract(range))
                .forEach((k, v) -> v.forEach(o -> orderMap.remove(o.getOrderId())));

        orderMap.getAskMap().tailMap(trade.getPrice().add(range))
                .forEach((k, v) -> v.forEach(o -> orderMap.remove(o.getOrderId())));

        lastCancelTime = trade.getCreated().getTime();
    }

    private long lastMetricTime = 0;

    private void metric(Trade trade){
        if (lastMetricTime > 0 && trade.getCreated().getTime() - lastMetricTime < metricDelay){
            return;
        }

        metrics.add(new Metric(trade.getPrice(), subtotalBtc.multiply(trade.getPrice()).add(subtotalCny), trade.getCreated()));

        lastMetricTime = trade.getCreated().getTime();
    }

    private SecureRandom random = new SecureRandom();
    private long idGen = 0;

    private BigDecimal lastBuyPrice = ZERO;
    private BigDecimal lastSellPrice = ZERO;

    private BigDecimal BD_0_01 = new BigDecimal(0.01);

    private BigDecimal lastActionPrice = ZERO;

    private void action(Trade trade){
        if (lastActionPrice.equals(trade.getPrice())){
            return;
        }

        boolean up = isBalance(trade);
        BigDecimal spread = getSpread(trade);

        BigDecimal p = trade.getPrice();
        BigDecimal buyPrice = up ? p : p.subtract(spread);
        BigDecimal sellPrice = up ? p.add(spread) : p;

        if (!orderMap.contains(buyPrice, spread, BID) && !orderMap.contains(sellPrice, spread, ASK)){
            //amount
            BigDecimal buyAmount = BigDecimal.valueOf((up ? random.nextGaussian()/2 + 2 : 0) * levelAmount);
            BigDecimal sellAmount = BigDecimal.valueOf((up ? 0 : random.nextGaussian()/2 + 2) * levelAmount);

            //less
            if (buyAmount.compareTo(BD_0_01) < 0){
                buyAmount = BD_0_01;
            }
            if (sellAmount.compareTo(BD_0_01) < 0){
                sellAmount = BD_0_01;
            }

            //slip
            if (lastBuyPrice.compareTo(ZERO) > 0 && buyPrice.compareTo(lastBuyPrice) < 0){
                buyAmount = buyAmount.multiply(lastBuyPrice.subtract(buyPrice).abs().divide(spread, 8, HALF_UP));
            }
            if (lastSellPrice.compareTo(ZERO) > 0 && sellPrice.compareTo(lastSellPrice) > 0){
                sellAmount = sellAmount.multiply(sellPrice.subtract(lastSellPrice).abs().divide(spread, 8, HALF_UP));
            }
            lastBuyPrice = buyPrice;
            lastSellPrice = sellPrice;

            //order
            order(new Order(String.valueOf(++idGen), BID, buyPrice, buyAmount.setScale(3, HALF_EVEN)));
            order(new Order(String.valueOf(++idGen), ASK, sellPrice, sellAmount.setScale(3, HALF_EVEN)));

            metric(trade);
        }

        trade(trade);
        cancel(trade);

        lastActionPrice = trade.getPrice();
    }

    private BigDecimal getProfit(BigDecimal price) {
        return sellVolume.min(buyVolume.multiply(sellPrice.subtract(buyPrice))
                .add(buyVolume.subtract(sellVolume.abs())
                        .multiply(buyVolume.compareTo(sellVolume) > 0
                                ? price.subtract(buyPrice)
                                : sellPrice.subtract(price))));
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
                ", levelAmount=" + levelAmount +
                ", balanceDelay=" + balanceDelay +
                ", balanceValue=" + balanceValue +
                ", tradeDelay=" + tradeDelay +
                ", metricDelay=" + metricDelay +
                '}';
    }
}


