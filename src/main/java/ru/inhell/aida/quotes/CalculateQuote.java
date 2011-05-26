package ru.inhell.aida.quotes;

import ru.inhell.aida.entity.AllTrade;
import ru.inhell.aida.entity.Quote;
import ru.inhell.aida.inject.AidaInjector;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 26.05.11 21:54
 */
public class CalculateQuote {
    private final static SimpleDateFormat SIMPLE_DATE_FORMAT = new SimpleDateFormat("dd.MM.yyyy");

    public static void main(String... args) throws Exception {
        calculate5Sec("GZM1");
    }

    private static void calculate5Sec(String symbol) throws ParseException {
        QuotesBean quotesBean = AidaInjector.getInstance(QuotesBean.class);
        AllTradeBean allTradeBean = AidaInjector.getInstance(AllTradeBean.class);

        Long count = allTradeBean.getAllTradesCount(symbol);

        int buffer = 1000;

        Calendar calendar = Calendar.getInstance();

        for (int i=0; i < count/buffer; ++i){
            List<AllTrade> allTrades = allTradeBean.getAllTrades(symbol, i*buffer, buffer);

            float open = 0, high = 0, low = 100000, close = 0, volume = 0;

            int second_index = 1;

            for (AllTrade allTrade : allTrades){
                calendar.setTime(allTrade.getTime());
                int second = calendar.get(Calendar.SECOND);

                if (second_index != second/5 && open != 0 && close != 0){
                    Calendar day = Calendar.getInstance();
                    day.setTime(SIMPLE_DATE_FORMAT.parse(allTrade.getDate()));

                    Calendar date = Calendar.getInstance();
                    date.setTime(allTrade.getTime());
                    date.set(day.get(Calendar.YEAR), day.get(Calendar.MONTH), day.get(Calendar.DAY_OF_MONTH));
                    date.set(Calendar.SECOND, second_index*5);

                    try {
                        quotesBean.save5Sec(new Quote(symbol, date.getTime(), open, high, low, close, volume));
                    } catch (Exception e) {
                        //skip duplicate
                    }

                    open = 0;
                    high = 0;
                    low = 100000;
                    volume = 0;

                    second_index = second/5;
                }

                close = allTrade.getPrice();

                if (open == 0) open = close;

                if (high < close) high = close;

                if (low > close) low = close;

                volume += allTrade.getVolume();
            }

        }









    }
}
