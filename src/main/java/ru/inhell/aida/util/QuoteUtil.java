package ru.inhell.aida.util;

import ru.inhell.aida.entity.Quote;

import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 17.03.11 15:08
 */
public class QuoteUtil {
    public static float[] getClosePrices(List<Quote> quotes){
        int size = quotes.size();

        float[] prices = new float[size];

        for (int i = 0; i < quotes.size(); ++i){
            prices[i] = quotes.get(i).getClose();
        }

        return prices;
    }

    public static float[] getLowPrices(List<Quote> quotes){
        int size = quotes.size();

        float[] prices = new float[size];

        for (int i = 0; i < quotes.size(); ++i){
            prices[i] = quotes.get(i).getLow();
        }

        return prices;
    }

    public static float[] getHighPrices(List<Quote> quotes){
        int size = quotes.size();

        float[] prices = new float[size];

        for (int i = 0; i < quotes.size(); ++i){
            prices[i] = quotes.get(i).getHigh();
        }

        return prices;
    }

    public static float[] getAveragePrices(List<Quote> quotes){
        int size = quotes.size();

        float[] prices = new float[size];

        for (int i = 0; i < quotes.size(); ++i){
            Quote q = quotes.get(i);
            prices[i] = (q.getOpen() + q.getLow() +q.getHigh() + q.getClose())/4;
        }

        return prices;
    }
}
