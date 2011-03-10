package ru.inhell.aida.chart;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 10.03.11 18:48
 */
public class AlphaProfitChart {
    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

    public static void main(String... args) throws IOException, ParseException {
        CSVReader reader = new CSVReader(new FileReader("C:\\Anatoly\\Java\\aida\\db\\n1000l125.csv"));

        String[] line;

        reader.readNext();

        while ((line = reader.readNext()) != null){
            int vectorForecastId = Integer.parseInt(line[1]);

            Date date = simpleDateFormat.parse(line[2]);

            double price = Double.parseDouble(line[5]);

            String type = line[6];



        }


    }
}
