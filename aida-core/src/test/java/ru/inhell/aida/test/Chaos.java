package ru.inhell.aida.test;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.07.11 19:20
 */
public class Chaos {
    private static final Random PSEUDO_RANDOM = new Random();

    private static final int COUNT = 10000;

    public static void main(String... args){
        JFrame frame = new JFrame("Jaos");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel(new BorderLayout());
        frame.add(panel);

        final XYSeries[] series = new XYSeries[1];
        XYSeriesCollection xySeriesCollection = new XYSeriesCollection();

        for (int i = 0, seriesLength = series.length; i < seriesLength; i++) {
            series[i] = new XYSeries(i);
            xySeriesCollection.addSeries(series[i]);
        }

        JFreeChart chart = ChartFactory.createXYLineChart("Jaos", "interval", "price", xySeriesCollection ,
                PlotOrientation.VERTICAL, false, false, false);
        panel.add(new ChartPanel(chart), BorderLayout.CENTER);

        JButton button = new JButton("Random");

        button.setAction(new AbstractAction() {
            public void actionPerformed(ActionEvent e) {
                for (XYSeries s : series) {
                    s.clear();
                    s.setNotify(false);

                    int price = 0;

                    for (int i = 0; i < COUNT; ++i) {
                        price += nextBoolean() ? 1 : -1;

                        s.add(i, price);
                    }

                    s.setNotify(true);
                }
            }
        });

        panel.add(button, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    private static boolean nextBoolean(){
        return PSEUDO_RANDOM.nextBoolean();
    }
}