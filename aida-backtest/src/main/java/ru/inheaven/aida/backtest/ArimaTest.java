package ru.inheaven.aida.backtest;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inhell.aida.algo.arima.ArimaFitter;
import ru.inhell.aida.algo.arima.ArimaForecaster;
import ru.inhell.aida.algo.arima.DefaultArimaForecaster;

import javax.swing.*;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.DoubleStream;

/**
 * inheaven on 09.05.2016.
 */
public class ArimaTest {
    public static void main(String[] args){
        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        Date startDate = Date.from(LocalDateTime.of(2016, 5, 9, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 5, 9 , 18, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        List<Trade> trades = tradeMapper.getLightTrades("BTC/CNY", startDate, endDate, 0, 90000);

        double[] prices = trades.stream().mapToDouble(t -> t.getPrice().doubleValue()).toArray();

        XYSeriesCollection collection = new XYSeriesCollection();

        //chart
        XYSeries priceSeries = new XYSeries("price");

        for (int i = 0; i < prices.length; ++i){
            priceSeries.add(i, prices[i]);
        }

        collection.addSeries(priceSeries);

        for (int p = 1; p <= 1; ++p){
            for (int d = 1; d <= 1; ++d){
                for (int q = 0; q <= 0; ++q){
                    XYSeries totalSeries = new XYSeries(p + ", " + d + ", " + q);

                    System.out.println(prices[prices.length-1]);

                    double[] logPrices = DoubleStream.of(prices).map(Math::log).toArray();
                    ArimaForecaster forecaster = new DefaultArimaForecaster(ArimaFitter.fit(logPrices, p, d, q), logPrices);
                    double[] forecasts = DoubleStream.of(forecaster.next(10)).map(Math::exp).toArray();
                    System.out.println(p + " " + d + " " + q + " " + Arrays.toString(forecasts));

                    ArimaForecaster forecaster2 = new DefaultArimaForecaster(ArimaFitter.fit(prices, p, d, q), prices);
                    double[] forecasts2 = forecaster2.next(10);
                    System.out.println(p + " " + d + " " + q + " " + Arrays.toString(forecasts2));

                    for (int i = 0; i < forecasts.length; ++i){
                        totalSeries.add(prices.length + i, forecasts[i]);
                    }

                    collection.addSeries(totalSeries);
                }
            }
        }

        XYPlot plot = new XYPlot();
        XYLineAndShapeRenderer renderer1 = new XYLineAndShapeRenderer(true, false);

        //y1
        plot.setDataset(0, collection);
        plot.setRenderer(0, renderer1);
        DateAxis domainAxis = new DateAxis("index");
        plot.setDomainAxis(domainAxis);

        NumberAxis y1 = new NumberAxis("price");
        y1.setAutoRange(true);
        y1.setAutoRangeIncludesZero(false);
        plot.setRangeAxis(y1);

        JFreeChart chart = new JFreeChart("ArimaTest", plot);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
    }
}
