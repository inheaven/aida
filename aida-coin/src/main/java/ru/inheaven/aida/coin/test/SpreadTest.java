package ru.inheaven.aida.coin.test;

import au.com.bytecode.opencsv.CSVReader;

import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * @author inheaven on 05.04.2015 19:51.
 */
public class SpreadTest {
    private static List<Double> ticker = new ArrayList<>();

    public static void main(String... args) throws IOException {
        CSVReader reader = new CSVReader(new FileReader("D:\\AIDA\\okcoin-equity-feb.csv"));

        reader.readNext();

        String [] nextLine;
        while ((nextLine = reader.readNext()) != null) {
            ticker.add(Double.valueOf(nextLine[4]));
        }

        for (double spread = 0.01; spread < 0.1; spread += 0.01){
            System.out.println(BigDecimal.valueOf(spread).setScale(2, BigDecimal.ROUND_UP) + "  " + BigDecimal.valueOf(count(spread)).setScale(0, RoundingMode.HALF_UP));
        }
    }

    private static double count(double spread){
        int count = 0;
        double price = ticker.get(0);

        for (Double t : ticker){
            int c = (int) (Math.abs(price - t)/spread);

            if (c > 0){
                count += c;
                price = t;
            }
        }

        return count;
    }
}
