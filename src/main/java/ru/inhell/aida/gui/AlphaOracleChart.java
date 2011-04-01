package ru.inhell.aida.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.Timeline;
import org.jfree.chart.renderer.xy.XYDotRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.chart.renderer.xy.XYShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.time.TimeTableXYDataset;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.data.xy.OHLCDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.RectangleInsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.VectorForecastBean;
import ru.inhell.aida.quotes.CurrentBean;
import ru.inhell.aida.quotes.QuotesBean;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.03.11 18:15
 */
public class AlphaOracleChart extends JPanel{
    private final static Logger log = LoggerFactory.getLogger(AlphaOracleChart.class);

    public AlphaOracleChart() {
        setLayout(new BorderLayout());

        final QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);
        final AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);
        final VectorForecastBean vectorForecastBean = AidaInjector.getInstance(VectorForecastBean.class);
        final CurrentBean currentBean = AidaInjector.getInstance(CurrentBean.class);

        final List<AlphaOracle> alphaOracles = alphaOracleBean.getAlphaOracles();

        final int size = 60;

        //model
        final Date[] date = new Date[size];
        final double[] open = new double[size];
        final double[] low = new double[size];
        final double[] high = new double[size];
        final double[] close = new double[size];
        final double[] volume = new double[size];

        //chart
        final JFreeChart chart = ChartFactory.createCandlestickChart("", "date", "price", null, true);
        ((NumberAxis)chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);
        add(new ChartPanel(chart));

        //prediction
        final TimeSeriesCollection timeSeriesCollection = new TimeSeriesCollection();

        for (AlphaOracle ao : alphaOracles){
            timeSeriesCollection.addSeries(new TimeSeries("long"+ao.getId()));
            timeSeriesCollection.addSeries(new TimeSeries("short"+ao.getId()));
        }

        chart.getXYPlot().setDataset(1, timeSeriesCollection);
        chart.getXYPlot().setRenderer(1, new XYLineAndShapeRenderer(false, true));

        //forecast
        final TimeSeriesCollection forecast = new TimeSeriesCollection();
        forecast.addSeries(new TimeSeries("forecast"));

        chart.getXYPlot().setDataset(2, forecast);
        chart.getXYPlot().setRenderer(2, new XYLineAndShapeRenderer(true, false));

        //executor
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    //Quotes
                    List<Quote> quotes = quotesBean.getQuotes("GAZP", size);

                    for (int i = 0; i < size; ++i) {
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

                    //Prediction
                    for (AlphaOracle ao : alphaOracles) {
                        TimeSeries timeSeriesLong = timeSeriesCollection.getSeries("long" + ao.getId());
                        TimeSeries timeSeriesShort = timeSeriesCollection.getSeries("short" + ao.getId());

                        for (AlphaOracleData d : alphaOracleBean.getAlphaOracleDatas(ao.getId(), date[0])){
                            if (d.getPrediction().equals(AlphaOracleData.PREDICTION.LONG)){
                                timeSeriesLong.addOrUpdate(new Minute(d.getDate()), d.getPrice());
                            }else if (d.getPrediction().equals(AlphaOracleData.PREDICTION.SHORT)){
                                timeSeriesShort.addOrUpdate(new Minute(d.getDate()), d.getPrice());
                            }
                        }

                        List<VectorForecastData> vfdList = vectorForecastBean.getVectorForecastData(
                                new VectorForecastFilter(ao.getVectorForecast().getId(), date[0]));

                        for (VectorForecastData vfd : vfdList){
                            if (vfd.getDate().equals(date[size-2])) {
                                forecast.getSeries("forecast").addOrUpdate(new Minute(vfd.getIndexDate()), vfd.getPrice());
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("Ошибка рисования графика", e);
                }
            }
        }, 0, 1, TimeUnit.SECONDS);
    }
}

