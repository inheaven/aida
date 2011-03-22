package ru.inhell.aida.gui;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.xy.DefaultHighLowDataset;
import org.jfree.ui.RectangleInsets;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.quotes.QuotesBean;

import javax.swing.*;
import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.03.11 18:15
 */
public class AlphaOracleChart extends JPanel{
    public AlphaOracleChart() {
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

        int size = 50;

        Date[] date = new Date[size];
        double[] open = new double[size];
        double[] low = new double[size];
        double[] high = new double[size];
        double[] close = new double[size];
        double[] volume = new double[size];

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

        DefaultHighLowDataset dataset = new DefaultHighLowDataset("", date, high, low, open, close, volume);

        JFreeChart chart = ChartFactory.createCandlestickChart("", "date", "price", dataset, false);

        add(new ChartPanel(chart));
    }
}
