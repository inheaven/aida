package ru.inhell.aida.trader;

import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.IAlphaOracleListener;
import ru.inhell.aida.quik.QuikMessage;
import ru.inhell.aida.quik.QuikService;
import ru.inhell.aida.quik.QuikTransaction;
import ru.inhell.aida.quik.QuikTransactionException;
import ru.inhell.aida.quotes.CurrentBean;
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
    private static final int MAX_STOP_COUNT = 5;

    private final Logger log = LoggerFactory.getLogger(AlphaTraderService.class);

    private AlphaTrader alphaTrader;

    private AlphaTraderBean alphaTraderBean;
    private AlphaOracleBean alphaOracleBean;
    private CurrentBean currentBean;
    private QuikService quikService;

    private Date processDate;

    public AlphaOracleListener(AlphaTrader alphaTrader, AlphaTraderBean alphaTraderBean, AlphaOracleBean alphaOracleBean,
                               CurrentBean currentBean, QuikService quikService) {
        this.alphaTrader = alphaTrader;
        this.alphaTraderBean = alphaTraderBean;
        this.alphaOracleBean = alphaOracleBean;
        this.currentBean = currentBean;
        this.quikService = quikService;
    }

    @Override
    public void predicted(AlphaOracle alphaOracle, AlphaOracleData.PREDICTION prediction, List<Quote> quotes, float[] forecast) {
        if (!alphaOracle.getId().equals(alphaTrader.getAlphaOracleId())){
            return;
        }

        int n = alphaOracle.getVectorForecast().getN();
        alphaTrader = alphaTraderBean.getAlphaTrader(alphaTrader.getId());

        //max stop count
        if (alphaTrader.getStopCount() > MAX_STOP_COUNT){
            return;
        }

        //stop loss
        if (prediction == null){
            int quantity = alphaTrader.getQuantity();

            if (alphaTrader.getStopType().equals(AlphaTrader.STOP_TYPE.F_STOP) && quantity != 0){
                float currentPrice = currentBean.getCurrent(alphaTrader.getSymbol()).getPrice();
                float stopPrice = alphaTrader.getStopPrice();

                if (quantity > 0 && currentPrice < stopPrice){
                    prediction = AlphaOracleData.PREDICTION.STOP_SELL;
                }else if (quantity < 0 &&  currentPrice > stopPrice){
                    prediction = AlphaOracleData.PREDICTION.STOP_BUY;
                }else{
                    return;
                }
            }else{
                return;
            }

            //save prediction
            try {
                alphaOracleBean.save(new AlphaOracleData(alphaOracle.getId(), quotes.get(n-1).getDate(),
                        forecast[n-1], prediction, DateUtil.now()));
            } catch (Exception e) {
                //skip duplicates
            }
        }

        Date date = quotes.get(quotes.size()-1).getDate();

        //skip
        if (processDate != null && DateUtil.getAbsMinuteShiftMsk(processDate) < 5){
            return;
        }else{
            processDate = date;
        }

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
        AlphaTraderData alphaTraderData = new AlphaTraderData(alphaTrader.getId(), DateUtil.nowMsk(), orderPrice, 0,
                getOrder(prediction));

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
                    alphaTrader.setStopPrice(currentBean.getCurrent(alphaTrader.getSymbol()).getPrice()/alphaTrader.getStopFactor());
                    break;
                case SHORT:
                    qt = quikService.sellFutures(transactionId, futureSymbol, orderPrice, reverseQuantity);
                    alphaTrader.setQuantity(-orderQuantity);
                    alphaTrader.setStopPrice(currentBean.getCurrent(alphaTrader.getSymbol()).getPrice()*alphaTrader.getStopFactor());
                    break;
                case STOP_BUY:
                    qt = quikService.buyFutures(transactionId, futureSymbol, orderPrice, orderQuantity);
                    alphaTrader.setQuantity(0);
                    alphaTrader.setStopCount(alphaTrader.getStopCount() + 1);
                    break;
                case STOP_SELL:
                    qt = quikService.sellFutures(transactionId, futureSymbol, orderPrice, orderQuantity);
                    alphaTrader.setQuantity(0);
                    alphaTrader.setStopCount(alphaTrader.getStopCount() + 1);
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            alphaTraderData.setQuantity(reverseQuantity);

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
            case STOP_BUY:
                return price *= 1.005;
            case SHORT:
            case STOP_SELL:
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
