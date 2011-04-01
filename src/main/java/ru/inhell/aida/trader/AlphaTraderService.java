package ru.inhell.aida.trader;

import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.Aida;
import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.AlphaOracleData;
import ru.inhell.aida.entity.AlphaTrader;
import ru.inhell.aida.entity.AlphaTraderData;
import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.oracle.IAlphaOracleListener;
import ru.inhell.aida.quik.*;
import ru.inhell.aida.quotes.CurrentBean;
import ru.inhell.aida.quotes.Forts;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.util.DateUtil;

import java.util.Date;

import static ru.inhell.aida.entity.AlphaOracleData.PREDICTION.*;

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
    private CurrentBean currentBean;

    @Inject
    private QuikService quikService;

    @Inject
    private AlphaTraderBean alphaTraderBean;

    private class AlphaOracleListener implements IAlphaOracleListener{
        private AlphaTrader alphaTrader;

        private AlphaOracleListener(AlphaTrader alphaTrader) {
            this.alphaTrader = alphaTrader;
        }

        @Override
        public void predicted(AlphaOracle alphaOracle, String symbol, AlphaOracleData.PREDICTION prediction,
                              Date date, float price) {
            //уже в позиции
            if ((alphaTrader.getQuantity() > 0 && prediction.equals(LONG))
                    || (alphaTrader.getQuantity() < 0 && prediction.equals(SHORT))){
                log.info("уже в позиции: " + date + ", " + price);

                return;
            }

            if (DateUtil.getAbsMinuteShiftMsk(date) > alphaOracle.getVectorForecast().getM()){
                log.info("предсказание устарело: " + date + ", " + price);

                return;
            }

            //цена и код фьючерса
            String futureSymbol = Forts.valueOf(symbol).getFortsSymbol();
            float futurePrice = currentBean.getCurrent(futureSymbol).getPrice();
            float orderPrice = getOrderPrice(prediction, futurePrice);
            int quantity = getOrderQuantity(alphaTrader);

            //если в противоположной позиции то переворачиваем
            if (alphaTrader.getQuantity() != 0){
                quantity *= 2;
            }

            //создаем транзакцию
            AlphaTraderData alphaTraderData = new AlphaTraderData(alphaTrader.getId(), DateUtil.now(), orderPrice,
                    getOrder(prediction));

            alphaTraderBean.save(alphaTraderData);

            //делаем заявку
            try {
                QuikTransaction qt;

                quikService.connect(Aida.getQuikDir()); //quik connect

                switch (prediction){
                    case LONG:
                        qt = quikService.buyFutures(alphaTraderData.getId(), futureSymbol, futurePrice, quantity);
                        alphaTrader.setQuantity(alphaTrader.getQuantity() + quantity);
                        break;
                    case SHORT:
                        qt = quikService.sellFutures(alphaTraderData.getId(), futureSymbol, futurePrice, quantity);
                        alphaTrader.setQuantity(alphaTrader.getQuantity() - quantity);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }

                quikService.disconnect(); //quik disconnect

                update(qt, alphaTraderData);
                alphaTraderBean.save(alphaTrader);

                log.info(qt.toString());
            } catch (QuikTransactionException e) { //ошибка выставления заявки
                log.error(e.getMessage(), e);

                update(e.getQuikTransaction(), alphaTraderData);
            } catch (QuikException e) { //ошибка подключения
                log.error(e.getMessage(), e);

                update(e.getQuikMessage(), alphaTraderData);
            }
        }

        private void update(QuikTransaction quikTransaction, AlphaTraderData alphaTraderData){
            alphaTraderData.setReplyCode((int) quikTransaction.getReplyCode().getValue());
            alphaTraderData.setResult(quikTransaction.getResult().intValue());
            alphaTraderData.setOrderNum((long) quikTransaction.getOrderNum().getValue());

            alphaTraderBean.save(alphaTraderData);
        }

        private void update(QuikMessage quikMessage, AlphaTraderData alphaTraderData){
            alphaTraderData.setResult(quikMessage.getResult().intValue());

            alphaTraderBean.save(alphaTraderData);
        }

        private float getOrderPrice(AlphaOracleData.PREDICTION prediction, float price){
            switch (prediction){
                case LONG:
                    return price *= 1.02;
                case SHORT:
                    return price /= 1.02;
                default:
                    throw new IllegalArgumentException();
            }
        }

        private int getOrderQuantity(AlphaTrader alphaTrader){
            return 1;
        }

        private AlphaTraderData.ORDER getOrder(AlphaOracleData.PREDICTION prediction){
            switch (prediction){
                case LONG:
                    return AlphaTraderData.ORDER.BUY;
                case SHORT:
                    return AlphaTraderData.ORDER.SELL;
                default:
                    throw new IllegalArgumentException();
            }
        }
    }

    public void process(Long alphaTraderId){
        AlphaTrader alphaTrader = alphaTraderBean.getAlphaTrader(alphaTraderId);

        alphaOracleService.addListener(new AlphaOracleListener(alphaTrader));
        alphaOracleService.process(alphaTrader.getAlphaOracleId());
    }
}
