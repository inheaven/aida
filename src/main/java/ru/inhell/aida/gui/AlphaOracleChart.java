package ru.inhell.aida.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.DatasetRenderingOrder;
import org.jfree.chart.plot.DefaultDrawingSupplier;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.time.Minute;
import org.jfree.data.time.MovingAverage;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.oracle.IAlphaOracleListener;
import ru.inhell.aida.oracle.VectorForecastBean;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.util.DateUtil;

import javax.swing.*;
import java.awt.*;
import java.util.Calendar;
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

    public AlphaOracleChart(Long alphaOracleId){
        this(alphaOracleId, 1);
    }

    public AlphaOracleChart(Long alphaOracleId, int updateInterval) {
        setLayout(new BorderLayout());

        final QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);
        final AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);
        final AlphaOracleService alphaOracleService = AidaInjector.getInstance(AlphaOracleService.class);
        final VectorForecastBean vectorForecastBean = AidaInjector.getInstance(VectorForecastBean.class);

        final AlphaOracle alphaOracle = alphaOracleBean.getAlphaOracle(alphaOracleId);
        final VectorForecast vf = alphaOracle.getVectorForecast();

        final int size = 120;

        //model
        final Date[] date = new Date[size];
        final double[] open = new double[size];
        final double[] low = new double[size];
        final double[] high = new double[size];
        final double[] close = new double[size];
        final double[] volume = new double[size];

        //chart
        final JFreeChart chart = ChartFactory.createHighLowChart(vf.getSymbol(), null, null, null, true);
        chart.getXYPlot().setDatasetRenderingOrder(DatasetRenderingOrder.FORWARD);
        ((NumberAxis)chart.getXYPlot().getRangeAxis()).setAutoRangeIncludesZero(false);
        chart.getLegend().setVisible(false);
        chart.getXYPlot().getRenderer(0).setSeriesPaint(0, Color.BLACK);

        add(new ChartPanel(chart), BorderLayout.CENTER);

        chart.setTitle(alphaOracle.getId() + "-" + vf.getSymbol() + "-"
                    + "n" + vf.getN() + "l" + vf.getL() + "p" + vf.getP() + "m" + vf.getM());

        //prediction
        final TimeSeriesCollection predictionPoint = new TimeSeriesCollection();

        predictionPoint.addSeries(new TimeSeries("long"));
        predictionPoint.addSeries(new TimeSeries("short"));
        predictionPoint.addSeries(new TimeSeries("stopBuy"));
        predictionPoint.addSeries(new TimeSeries("stopSell"));

        chart.getXYPlot().setDataset(1, predictionPoint);
        XYLineAndShapeRenderer predictionPointRenderer = new XYLineAndShapeRenderer(false, true);

        Shape[] shapes = DefaultDrawingSupplier.createStandardSeriesShapes();

        predictionPointRenderer.setAutoPopulateSeriesShape(false);
        predictionPointRenderer.setAutoPopulateSeriesPaint(false);

        predictionPointRenderer.setSeriesShape(0, shapes[2]);
        predictionPointRenderer.setSeriesPaint(0, Color.GREEN);

        predictionPointRenderer.setSeriesShape(1, shapes[5]);
        predictionPointRenderer.setSeriesPaint(1, Color.RED);

        predictionPointRenderer.setSeriesShape(2, shapes[1]);
        predictionPointRenderer.setSeriesPaint(2, Color.CYAN);

        predictionPointRenderer.setSeriesShape(3, shapes[1]);
        predictionPointRenderer.setSeriesPaint(3, Color.ORANGE);

        chart.getXYPlot().setRenderer(1, predictionPointRenderer);

        //forecastLine
        final TimeSeriesCollection forecastLine = new TimeSeriesCollection();
        forecastLine.addSeries(new TimeSeries("forecast"));

        chart.getXYPlot().setDataset(2, forecastLine);
        chart.getXYPlot().setRenderer(2, new XYLineAndShapeRenderer(true, false));

        //currentForecastLine
        final TimeSeriesCollection currentForecastLine = new TimeSeriesCollection();
        currentForecastLine.addSeries(new TimeSeries("currentForecast"));

        chart.getXYPlot().setDataset(3, currentForecastLine);
        chart.getXYPlot().setRenderer(3, new XYLineAndShapeRenderer(true, false));

        //Moving Average
        chart.getXYPlot().setRenderer(4, new XYLineAndShapeRenderer(true, false));
        chart.getXYPlot().setRenderer(5, new XYLineAndShapeRenderer(true, false));

        alphaOracleService.addListener(new IAlphaOracleListener() {
            @Override
            public void predicted(AlphaOracle ao, Prediction prediction, List<Quote> quotes, float[] forecast) {
                if (alphaOracle.getId().equals(ao.getId())) {
                    TimeSeries forecastTimeSeries = forecastLine.getSeries("forecast");

                    int n = ao.getVectorForecast().getN();

                    Date p = quotes.get(n-1).getDate();

                    if (date[0].after(p)){
                        return;
                    }

                    for (int i = 0; i < n; ++i){
                        Date d = quotes.get(i).getDate();
                        if (d.after(date[0])){
                            forecastTimeSeries.addOrUpdate(new Minute(d), forecast[i]);
                        }
                    }

                    for (int i = 0; i < ao.getVectorForecast().getM(); ++i){
                        Date d = DateUtil.getOneMinuteIndexDate(p, i+1);
                        forecastTimeSeries.addOrUpdate(new Minute(d), forecast[n+i]);
                    }
                }
            }

            @Override
            public Long getFilteredId() {
                return null;
            }
        });

        //executor
        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);

        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                try {
                    //Quotes
                    List<Quote> quotes = quotesBean.getQuotes(alphaOracle.getVectorForecast().getSymbol(), size);

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
                    DefaultHighLowDataset dataset = new DefaultHighLowDataset(alphaOracle.getVectorForecast().getSymbol(),
                            date, high, low, open, close, volume);
                    chart.getXYPlot().setDataset(dataset);



                    //Prediction
                    TimeSeries timeSeriesLong = predictionPoint.getSeries("long");

                    TimeSeries timeSeriesShort = predictionPoint.getSeries("short");

                    TimeSeries timeSeriesStopBuy = predictionPoint.getSeries("stopBuy");

                    TimeSeries timeSeriesStopSell = predictionPoint.getSeries("stopSell");

                    for (AlphaOracleData d : alphaOracleBean.getAlphaOracleDatas(alphaOracle.getId(), date[0])){
                        switch (d.getPrediction()){
                            case LONG:
                                timeSeriesLong.addOrUpdate(new Minute(d.getDate()), d.getPrice());
                                break;
                            case SHORT:
                                timeSeriesShort.addOrUpdate(new Minute(d.getDate()), d.getPrice());
                                break;
                            case STOP_BUY:
                                timeSeriesStopBuy.addOrUpdate(new Minute(d.getDate()), d.getPrice());
                                break;
                            case STOP_SELL:
                                timeSeriesStopSell.addOrUpdate(new Minute(d.getDate()), d.getPrice());
                                break;
                        }
                    }

                    TimeSeries timeSeriesCurrentForecast = currentForecastLine.getSeries("currentForecast");

                    //Vector Forecast
                    VectorForecastFilter filter = new VectorForecastFilter();
                    filter.setVectorForecastId(vf.getId());
                    filter.setDate(date[size - 2]);

                    for (VectorForecastData d :  vectorForecastBean.getVectorForecastData(filter)){
                        timeSeriesCurrentForecast.addOrUpdate(new Minute(d.getIndexDate()), d.getPrice());
                    }

                    Calendar c1 = Calendar.getInstance();
                    c1.add(Calendar.HOUR_OF_DAY, -3);
                    c1.add(Calendar.MINUTE, -120);

//                    c1.set(Calendar.HOUR_OF_DAY, 10);
//                    c1.set(Calendar.MINUTE, 30);

                    Calendar c2 = Calendar.getInstance();
                    c2.add(Calendar.HOUR_OF_DAY, -3);
                    c2.add(Calendar.MINUTE, 10);
//                    c2.set(Calendar.HOUR_OF_DAY, 18);
//                    c2.set(Calendar.MINUTE, 45);
                    ((DateAxis) chart.getXYPlot().getDomainAxis()).setMinimumDate(c1.getTime());
                    ((DateAxis) chart.getXYPlot().getDomainAxis()).setMaximumDate(c2.getTime());

                    if (c1.get(Calendar.HOUR_OF_DAY) > 11) {
//                        chart.getXYPlot().setDataset(4, MovingAverage.createMovingAverage(dataset, "_MA100", 100*60*1000, 0));
//                        chart.getXYPlot().setDataset(5, MovingAverage.createMovingAverage(dataset, "_MA50", 50*60*1000, 0));
                    }
                } catch (Exception e) {
                    log.error("Ошибка рисования графика: " + e.getMessage());
                }
            }
        }, 0, updateInterval, TimeUnit.SECONDS);
    }
}
