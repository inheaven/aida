package ru.inhell.aida.quotes;

import au.com.bytecode.opencsv.CSVReader;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.inject.AidaInjector;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.03.11 17:36
 */
public class ImportQuotes {
    public static void main(String... args) throws IOException, ParseException {
        //<TICKER>,<PER>,<DATE>,<TIME>,<OPEN>,<HIGH>,<LOW>,<CLOSE>,<VOL>
        //GAZP,1,20100331,103000,170.00000,170.00000,169.53000,169.88000,333165

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

        CSVReader reader = new CSVReader(new FileReader("C:\\Anatoly\\Java\\aida\\db\\GAZP_110101_110331.txt"));

        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

        reader.readNext();

        String[] line;

        while((line = reader.readNext()) != null){
            try {
                quotesBean.save(new Quote(line[0], simpleDateFormat.parse(line[2]+line[3]),
                        Float.parseFloat(line[4]), Float.parseFloat(line[5]), Float.parseFloat(line[6]),
                        Float.parseFloat(line[7]), Float.parseFloat(line[8])));
            } catch (Exception e) {
                System.out.println(line[2]+" "+line[3]);
            }
        }
    }
}
