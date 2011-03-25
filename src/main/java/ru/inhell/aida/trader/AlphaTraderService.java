package ru.inhell.aida.trader;

import com.google.inject.Inject;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.AlphaTrader;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.oracle.IAlphaOracleListener;
import ru.inhell.aida.quik.QuikService;
import ru.inhell.aida.quotes.QuotesBean;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 13:48
 */
public class AlphaTraderService {
    @Inject
    private AlphaOracleService alphaOracleService;

    @Inject
    private QuotesBean quotesBean;

    @Inject
    private QuikService quikService;

    public void process(AlphaTrader alphaTrader){

        alphaOracleService.addListener(new IAlphaOracleListener() {

            @Override
            public void predicted(String symbol, AlphaOracleData.PREDICTION type, Date date, float price) {

            }
        });
    }
}
