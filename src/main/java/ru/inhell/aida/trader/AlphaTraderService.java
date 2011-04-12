package ru.inhell.aida.trader;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.quik.*;
import ru.inhell.aida.quotes.CurrentBean;
import ru.inhell.aida.quotes.QuotesBean;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 13:48
 */
public class AlphaTraderService {
    private final static Logger log = LoggerFactory.getLogger(AlphaTraderService.class);

    @Inject
    private AlphaOracleService alphaOracleService;

    @Inject
    private AlphaTraderBean alphaTraderBean;

    @Inject
    private CurrentBean currentBean;

    @Inject
    private QuotesBean quotesBean;

    @Inject
    private QuikService quikService;

    public void process(Long alphaTraderId){
        AlphaTrader alphaTrader = alphaTraderBean.getAlphaTrader(alphaTraderId);

        alphaOracleService.addListener(new AlphaOracleListener(alphaTrader, alphaTraderBean, currentBean, quikService));
        alphaOracleService.process(alphaTrader.getAlphaOracleId());
    }
}
