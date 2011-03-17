package ru.inhell.aida.chart;

import au.com.bytecode.opencsv.CSVReader;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;

import javax.swing.*;
import java.awt.*;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.03.11 18:48
 */
public class AlphaProfitChart {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    private static class AlphaTrader {
        private Long id;
        private TimeSeries timeSeries;
        private float balance = 0;
        private boolean inTheMoney;

        private AlphaTrader(Long id) {
            this.id = id;
            timeSeries = new TimeSeries(names.get(id) + "-" + id);
        }
    }

    private static Map<Long, AlphaTrader> alphaTraderMap = new HashMap<Long, AlphaTrader>();

    private static Map<Long, String> names = new HashMap<Long, String>();

    private static AlphaTrader getAlphaTrader(Long id){
        AlphaTrader alphaTrader = alphaTraderMap.get(id);

        if (alphaTrader == null){
            alphaTrader = new AlphaTrader(id);
            alphaTraderMap.put(id, alphaTrader);
        }

        return alphaTrader;
    }

    private static void loadVectorForecast() throws IOException {
        CSVReader reader = new CSVReader(new FileReader("C:\\Anatoly\\Java\\aida\\db\\vector_forecast.csv"));

        String[] line;

        reader.readNext();

        while ((line = reader.readNext()) != null){
            names.put(Long.parseLong(line[0]), "n"+line[5]+"l"+line[6]+"p"+line[7]+"m"+line[8]);
        }
    }

    public static void main(String... args) throws IOException, ParseException {
        loadVectorForecast();

//        TimeSeries priceTimeSeries = new TimeSeries("price");

        CSVReader reader = new CSVReader(new FileReader("C:\\Anatoly\\Java\\aida\\db\\n1024l512.csv"));

        String[] line;

        reader.readNext();

        while ((line = reader.readNext()) != null){
            long vectorForecastId = Long.parseLong(line[1]);
            Date date = simpleDateFormat.parse(line[2]);
            float price = Float.parseFloat(line[5]);
            String type = line[6];

//            priceTimeSeries.addOrUpdate(new Minute(date), price);

            if (vectorForecastId > 250){
                continue;
            }

            AlphaTrader at = getAlphaTrader(vectorForecastId);

            if (type.indexOf("MIN") > -1 && at.inTheMoney){
                at.balance -= price;
                at.inTheMoney = false;
            }else if (type.indexOf("MAX") > -1 && !at.inTheMoney){
                at.balance += price;
                at.inTheMoney = true;
                at.timeSeries.add(new Minute(date), at.balance);
            }
        }

        TimeSeriesCollection collection = new TimeSeriesCollection();

        for (AlphaTrader at : alphaTraderMap.values()){
            collection.addSeries(at.timeSeries);
        }

        JFreeChart chart = ChartFactory.createTimeSeriesChart("AlphaOracleTest", "Date", "Balance", collection, true,
                true, true);

        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new ChartPanel(chart));
        frame.pack();
        frame.setVisible(true);
    }
}
