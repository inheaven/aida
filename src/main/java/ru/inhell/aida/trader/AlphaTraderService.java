package ru.inhell.aida.trader;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.AlphaTrader;
import ru.inhell.aida.entity.AlphaTraderData;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.oracle.IAlphaOracleListener;
import ru.inhell.aida.quik.QuikService;
import ru.inhell.aida.quik.QuikTransaction;
import ru.inhell.aida.quik.QuikTransactionException;
import ru.inhell.aida.quotes.Forts;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.util.DateUtil;

import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.03.11 13:48
 */
public class AlphaTraderService {
    private final static Logger log = LoggerFactory.getLogger(AlphaTraderService.class);

    @Inject
    private AlphaOracleService alphaOracleService;

    @Inject
    private QuotesBean quotesBean;

    @Inject
    private QuikService quikService;

    @Inject
    private AlphaTraderBean alphaTraderBean;

    public void process(final AlphaTrader alphaTrader){

        alphaOracleService.addListener(new IAlphaOracleListener() {

            @Override
            public void predicted(AlphaOracle alphaOracle, String symbol, AlphaOracleData.PREDICTION prediction,
                                  Date date, float price) {
                String futureSymbol = Forts.valueOf(symbol).getFortsSymbol();
                float futurePrice = quotesBean.getClosePrice(futureSymbol);

                AlphaTraderData alphaTraderData = new AlphaTraderData(alphaTrader.getId(), DateUtil.nowMsk(),
                        getOrderPrice(prediction, futurePrice), getOrder(prediction));

                alphaTraderBean.save(alphaTraderData);

                try {
                    QuikTransaction qt = quikService.buyFutures(alphaTraderData.getId(), futureSymbol, futurePrice, 1);

                    update(qt, alphaTraderData);
                } catch (QuikTransactionException e) {
                    log.error(e.getMessage(), e);

                    update(e.getQuikTransaction(), alphaTraderData);

                    //todo update
                }
            }
        });
    }

    private void update(QuikTransaction quikTransaction, AlphaTraderData alphaTraderData){
        alphaTraderData.setReplyCode((int) quikTransaction.getReplyCode().getValue());
        alphaTraderData.setResult(quikTransaction.getResult().intValue());

        alphaTraderBean.save(alphaTraderData);
    }

    private float getOrderPrice(AlphaOracleData.PREDICTION prediction, float price){
        if (prediction.equals(AlphaOracleData.PREDICTION.LONG)){
            return price *= 1.02;
        }else if (prediction.equals(AlphaOracleData.PREDICTION.SHORT)){
            return price /= 1.02;
        }

        throw new IllegalArgumentException();
    }

    private AlphaTraderData.ORDER getOrder(AlphaOracleData.PREDICTION prediction){
        if (prediction.equals(AlphaOracleData.PREDICTION.LONG)){
            return AlphaTraderData.ORDER.SELL;
        }else if (prediction.equals(AlphaOracleData.PREDICTION.SHORT)){
            return AlphaTraderData.ORDER.BUY;
        }

        throw new IllegalArgumentException();
    }
}
