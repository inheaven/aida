package ru.inhell.aida.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.quotes.QuotesBean;

import javax.swing.*;
import java.awt.geom.Rectangle2D;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.03.11 18:15
 */
public class AlphaOracleChart extends JPanel{
    public AlphaOracleChart() {
        final QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

        final int size = 60;

        final Date[] date = new Date[size];
        final double[] open = new double[size];
        final double[] low = new double[size];
        final double[] high = new double[size];
        final double[] close = new double[size];
        final double[] volume = new double[size];

        final JFreeChart chart = ChartFactory.createCandlestickChart("", "date", "price", null, false);

        add(new ChartPanel(chart));

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                List<Quote> quotes = quotesBean.getQuotes("GAZP", size);

                for (int i = 0; i < size; ++i){
                    Quote q = quotes.get(i);

                    date[i] = q.getDate();
                    open[i] = q.getOpen();
                    low[i] = q.getLow();
                    high[i] = q.getHigh();
                    close[i] = q.getClose();
                    volume[i] = q.getVolume();
                }

                //todo improve object creation
                chart.getXYPlot().setDataset(new DefaultHighLowDataset("", date, high, low, open, close, volume));

                ((NumberAxis)chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);
            }
        }, 0, 1, TimeUnit.SECONDS);

        TimeSeries prediction = new TimeSeries("prediction");

    }
}

