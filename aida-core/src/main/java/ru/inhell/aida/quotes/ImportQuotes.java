package ru.inhell.aida.quotes;

import au.com.bytecode.opencsv.CSVReader;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.common.inject.AidaInjector;

import java.io.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 31.03.11 17:36
 */
public class ImportQuotes {
    private final static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMddHHmmss");

    public static void main(String... args) throws IOException, ParseException {

        File importDir = new File("E:\\AIDA\\import");

        File[] files = importDir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.contains(".txt");
            }
        });

        for (File f : files){
            System.out.println("Импорт файла: " + f.getName());

            process(f);
        }
    }

    /**
     * <TICKER>,<PER>,<DATE>,<TIME>,<OPEN>,<HIGH>,<LOW>,<CLOSE>,<VOL>
     *  GAZP,1,20100331,103000,170.00000,170.00000,169.53000,169.88000,333165
     * @param file File
     * @throws java.io.IOException IOException
     */
    private static void process(File file) throws IOException {
        CSVReader reader = new CSVReader(new FileReader(file));

        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);

        reader.readNext();

        String[] line;

        int duplicate = 0;
            int count = 0;

        while((line = reader.readNext()) != null){
            try {
                count++;

                quotesBean.save(new Quote(getSymbol(line[0]), simpleDateFormat.parse(line[2]+line[3]),
                        Float.parseFloat(line[4]), Float.parseFloat(line[5]), Float.parseFloat(line[6]),
                        Float.parseFloat(line[7]), Float.parseFloat(line[8])));
            } catch (Exception e) {
                duplicate++;
            }
        }

        System.out.println("Импорт завершен.  Количество записей:" + count + ". Количество дубликатов: " +duplicate);
    }

    private static String getSymbol(String s){
        if (s.equals("SBER")){
            return "SBER03";
        }

        return s;
    }
}
