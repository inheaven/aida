package ru.inhell.aida.common.service;

import au.com.bytecode.opencsv.CSVReader;
import ru.inhell.aida.common.entity.Bar;

import java.io.FileReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import static java.lang.Float.parseFloat;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.01.13 20:23
 */
public class QuotesService {
    private DateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public List<Bar> loadBars(String path){
        List<Bar> bars = new ArrayList<>();

        try {
            CSVReader reader = new CSVReader(new FileReader(path), ',');

            String[] line;

            //<TICKER>,<PER>,<DATE>,<TIME>,<OPEN>,<HIGH>,<LOW>,<CLOSE>,<VOL>
            reader.readNext();

            while ((line = reader.readNext()) != null){
                bars.add(new Bar(dateFormat.parse(line[2]+line[3]), parseFloat(line[4]), parseFloat(line[5]),
                        parseFloat(line[6]), parseFloat(line[7]), parseFloat(line[8])));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bars;
    }


}
