package ru.inhell.aida.trader;

import com.google.inject.Inject;
import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.oracle.IAlphaOracleListener;
import ru.inhell.aida.quik.QuikMessage;
import ru.inhell.aida.quik.QuikService;
import ru.inhell.aida.quik.QuikTransaction;
import ru.inhell.aida.quik.QuikTransactionException;
import ru.inhell.aida.quotes.CurrentBean;
import ru.inhell.aida.quotes.QuotesBean;
import ru.inhell.aida.util.DateUtil;

import java.util.Date;
import java.util.List;

import static ru.inhell.aida.entity.AlphaOracleData.PREDICTION.LONG;
import static ru.inhell.aida.entity.AlphaOracleData.PREDICTION.SHORT;

/**
* @author Anatoly A. Ivanov java@inhell.ru
*         Date: 12.04.11 22:09
*/
public class AlphaOracleListener implements IAlphaOracleListener {
    private final Logger log = LoggerFactory.getLogger(AlphaTraderService.class);

    private AlphaTrader alphaTrader;

    private AlphaTraderBean alphaTraderBean;
    private CurrentBean currentBean;
    private QuikService quikService;

    private Date processDate;

    public AlphaOracleListener(AlphaTrader alphaTrader, AlphaTraderBean alphaTraderBean, CurrentBean currentBean,
                               QuikService quikService) {
        this.alphaTrader = alphaTrader;
        this.alphaTraderBean = alphaTraderBean;
        this.currentBean = currentBean;
        this.quikService = quikService;
    }

    @Override
    public void predicted(AlphaOracle alphaOracle, AlphaOracleData.PREDICTION prediction, List<Quote> quotes, float[] forecast) {
        //stop loss
        if (prediction == null){
            int quantity = alphaTrader.getQuantity();

            if (alphaTrader.getStopType().equals(AlphaTrader.STOP_TYPE.M_STOP) && quantity != 0){
                float currentPrice = currentBean.getCurrent(alphaTrader.getSymbol()).getPrice();
                float stopPrice = alphaTrader.getStopPrice();

                if (quantity > 0 && currentPrice < stopPrice){
                    prediction = AlphaOracleData.PREDICTION.STOP_SELL;
                }else if (quantity < 0 &&  currentPrice > stopPrice){
                    prediction = AlphaOracleData.PREDICTION.STOP_BUY;
                }
            }else{
                return;
            }
        }

        Date date = quotes.get(quotes.size()-1).getDate();

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
        int orderPrice = (int) getOrderPrice(prediction, currentBean.getCurrent(alphaTrader.getFutureSymbol()).getPrice());

        //создаем транзакцию
        AlphaTraderData alphaTraderData = new AlphaTraderData(alphaTrader.getId(), DateUtil.nowMsk(),
                orderPrice, 0, getOrder(prediction));

        alphaTraderBean.save(alphaTraderData);

        //делаем заявку
        try {
            QuikTransaction qt;

            int orderQuantity = alphaTrader.getOrderQuantity();
            int reverseQuantity = alphaTrader.getQuantity() == 0 ? orderQuantity : 2*orderQuantity;
            Long transactionId = alphaTraderData.getId();
            String futureSymbol = alphaTrader.getFutureSymbol();

            switch (prediction){
                case LONG:
                    qt = quikService.buyFutures(transactionId, futureSymbol, orderPrice, reverseQuantity);
                    alphaTrader.setQuantity(orderQuantity);
                    alphaTrader.setStopPrice(currentBean.getCurrent(alphaTrader.getSymbol()).getPrice());
                    break;
                case SHORT:
                    qt = quikService.sellFutures(transactionId, futureSymbol, orderPrice, reverseQuantity);
                    alphaTrader.setQuantity(-orderQuantity);
                    alphaTrader.setStopPrice(currentBean.getCurrent(alphaTrader.getSymbol()).getPrice());
                    break;
                case STOP_BUY:
                    qt = quikService.buyFutures(transactionId, futureSymbol, orderPrice, orderQuantity);
                    alphaTrader.setQuantity(0);
                    break;
                case STOP_SELL:
                    qt = quikService.sellFutures(transactionId, futureSymbol, orderPrice, orderQuantity);
                    alphaTrader.setQuantity(0);
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

    @Override
    public Long getFilteredId() {
        return alphaTrader.getAlphaOracleId();
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

    private float getStopFactor(){
        return 1;
    }

    private int getOrderQuantity(AlphaTrader alphaTrader){
        return 1;
    }

    private AlphaTraderData.ORDER getOrder(AlphaOracleData.PREDICTION prediction){
        switch (prediction){
            case LONG:
            case STOP_BUY:
                return AlphaTraderData.ORDER.BUY;
            case SHORT:
            case STOP_SELL:
                return AlphaTraderData.ORDER.SELL;
            default:
                throw new IllegalArgumentException();
        }
    }
}
