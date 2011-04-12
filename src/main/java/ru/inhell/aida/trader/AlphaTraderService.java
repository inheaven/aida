package ru.inhell.aida.trader;

import com.google.inject.Inject;
import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.Aida;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.oracle.IAlphaOracleListener;
import ru.inhell.aida.quik.*;
import ru.inhell.aida.quotes.CurrentBean;
import ru.inhell.aida.quotes.Forts;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.util.DateUtil;

import java.util.Date;
import java.util.List;

import static ru.inhell.aida.entity.AlphaOracleData.PREDICTION.LONG;
import static ru.inhell.aida.entity.AlphaOracleData.PREDICTION.SHORT;

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

    public class AlphaOracleListener implements IAlphaOracleListener{
        private Date processDate;

        private AlphaTrader alphaTrader;

        private AlphaOracleListener(AlphaTrader alphaTrader) {
            this.alphaTrader = alphaTrader;
        }

        @Override
        public void predicted(AlphaOracle alphaOracle, AlphaOracleData.PREDICTION prediction, List<Quote> quotes, float[] forecast) {
            if (prediction == null || !alphaTrader.getAlphaOracleId().equals(alphaOracle.getId())){
                return;
            }

            Date date = quotes.get(alphaOracle.getVectorForecast().getN()-1).getDate();

            //skip
            if (processDate != null && DateUtil.getAbsMinuteShiftMsk(processDate) < 5){
                return;
            }else{
                processDate = date;
            }

            alphaTrader = alphaTraderBean.getAlphaTrader(alphaTrader.getId());

            if (DateUtil.getAbsMinuteShiftMsk(date) > alphaOracle.getVectorForecast().getM()){
                log.info("предсказание устарело: " + date);

                return;
            }

            //уже в позиции
            if ((alphaTrader.getQuantity() > 0 && prediction.equals(LONG))
                    || (alphaTrader.getQuantity() < 0 && prediction.equals(SHORT))){
                log.info("уже в позиции: " + date);

                return;
            }

            //цена и код фьючерса
            String futureSymbol = Forts.valueOf(alphaOracle.getVectorForecast().getSymbol()).getFortsSymbol();
            float orderPrice = getOrderPrice(prediction, currentBean.getCurrent(futureSymbol).getPrice());
            int quantity = getOrderQuantity(alphaTrader);

            //если в противоположной позиции то переворачиваем
            if (alphaTrader.getQuantity() != 0){
                quantity *= 2;
            }

            //создаем транзакцию
            AlphaTraderData alphaTraderData = new AlphaTraderData(alphaTrader.getId(), DateUtil.nowMsk(),
                    orderPrice, quantity, getOrder(prediction));

            alphaTraderBean.save(alphaTraderData);

            //делаем заявку
            try {
                QuikTransaction qt;

                switch (prediction){
                    case LONG:
                        qt = quikService.buyFutures(alphaTraderData.getId(), futureSymbol, (int) orderPrice, quantity);
                        alphaTrader.setQuantity(alphaTrader.getQuantity() + quantity);
                        break;
                    case SHORT:
                        qt = quikService.sellFutures(alphaTraderData.getId(), futureSymbol, (int) orderPrice, quantity);
                        alphaTrader.setQuantity(alphaTrader.getQuantity() - quantity);
                        break;
                    default:
                        throw new IllegalArgumentException();
                }

                update(qt, alphaTraderData);
                alphaTraderBean.save(alphaTrader);

                log.info(qt.toString());
            } catch (QuikTransactionException e) { //ошибка выставления заявки
                log.error(e.getMessage());

                update(e.getQuikTransaction(), alphaTraderData);
            }catch (Exception e){
                log.error(e.getMessage(), e);
            }
        }

        private void update(QuikTransaction quikTransaction, AlphaTraderData alphaTraderData){
            alphaTraderData.setReplyCode((int) quikTransaction.getReplyCode().getValue());
            alphaTraderData.setResult(quikTransaction.getResult().intValue());
            alphaTraderData.setOrderNum((long) quikTransaction.getOrderNum().getValue());
            alphaTraderData.setResultMessage(Native.toString(quikTransaction.getResultMessage(), "cp1251"));
            alphaTraderData.setErrorMessage(Native.toString(quikTransaction.getErrorMessage(), "cp1251"));

            alphaTraderBean.save(alphaTraderData);
        }

        private void update(QuikMessage quikMessage, AlphaTraderData alphaTraderData){
            alphaTraderData.setResult(quikMessage.getResult().intValue());

            alphaTraderBean.save(alphaTraderData);
        }

        private float getOrderPrice(AlphaOracleData.PREDICTION prediction, float price){
            switch (prediction){
                case LONG:
                    return price *= 1.005;
                case SHORT:
                    return price /= 1.005;
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
