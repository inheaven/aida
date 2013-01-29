package ru.inhell.aida.trader;

import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.entity.OrderType;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.oracle.IAlphaOracleListener;
import ru.inhell.aida.quik.QuikService;
import ru.inhell.aida.quik.QuikTransaction;
import ru.inhell.aida.quik.QuikTransactionException;
import ru.inhell.aida.quotes.CurrentBean;
import ru.inhell.aida.common.util.DateUtil;

import java.util.Date;
import java.util.List;

import static ru.inhell.aida.entity.Prediction.*;

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

    public AlphaOracleListener(AlphaTrader alphaTrader, AlphaTraderBean alphaTraderBean,
                               CurrentBean currentBean, QuikService quikService) {
        this.alphaTrader = alphaTrader;
        this.alphaTraderBean = alphaTraderBean;
        this.currentBean = currentBean;
        this.quikService = quikService;
    }

    @Override
    public void predicted(AlphaOracle alphaOracle, Prediction prediction, List<Quote> quotes, float[] forecast) {
        //нет предсказания
        if (prediction == null){
            return;
        }

        Date date = quotes.get(quotes.size()-1).getDate();

        //в локальном интервале уже было предсказание
       /* if (processDate != null && DateUtil.getAbsMinuteShiftMsk(processDate) == 0){
            return;
        }else{
            processDate = date;
        }*/

        if (DateUtil.getAbsMinuteShiftMsk(date) > alphaOracle.getVectorForecast().getM()){
            log.info("предсказание устарело: " + date);

            return;
        }

        //обновление данных
        alphaTrader = alphaTraderBean.getAlphaTrader(alphaTrader.getId());

        //уже в позиции
        if ((alphaTrader.getQuantity() > 0 && prediction.equals(LONG))
                || (alphaTrader.getQuantity() < 0 && prediction.equals(SHORT))
                || (alphaTrader.getQuantity() == 0 && prediction.equals(STOP_BUY))
                || (alphaTrader.getQuantity() == 0 && prediction.equals(STOP_SELL))){
            log.info("уже в позиции: " + date + ", alphaTraderId = " + alphaTrader.getId());

            return;
        }

        //цена и код фьючерса
        float currentFuturePrice = currentBean.getCurrent(alphaTrader.getFutureSymbol()).getPrice();
        float orderFuturePrice = getOrderPrice(prediction, currentFuturePrice);

        //создаем транзакцию
        AlphaTraderData alphaTraderData = new AlphaTraderData(alphaTrader.getId(), DateUtil.nowMsk(),
                currentFuturePrice, 0, getOrder(prediction));

        //формируем заявку
        try {
            QuikTransaction qt;

            int orderQuantity = alphaTrader.getOrderQuantity();
            int reverseQuantity = alphaTrader.getQuantity() == 0 ? orderQuantity : 2*orderQuantity;
            String futureSymbol = alphaTrader.getFutureSymbol();

            alphaTraderData.setQuantity(reverseQuantity);
            alphaTraderBean.save(alphaTraderData);

            Long transactionId = alphaTraderData.getId();

            switch (prediction){
                case LONG:
                    //покупка фьючерса через quik
                    qt = quikService.buyFutures(transactionId, futureSymbol, orderFuturePrice, reverseQuantity);

                    //обновление баланса
                    if (alphaTrader.getQuantity() < 0) {
                        alphaTrader.addBalance(reverseQuantity*(alphaTrader.getPrice() - currentFuturePrice));
                    }

                    //установка цены и количества заявки
                    alphaTrader.setQuantity(orderQuantity);
                    alphaTrader.setPrice(currentFuturePrice);
                    break;
                case SHORT:
                    //продажа фьючерса через quik
                    qt = quikService.sellFutures(transactionId, futureSymbol, orderFuturePrice, reverseQuantity);

                    //обновление баланса
                    if (alphaTrader.getQuantity() > 0) {
                        alphaTrader.addBalance(reverseQuantity*(currentFuturePrice - alphaTrader.getPrice()));
                    }

                    //установка цены и количества заявки
                    alphaTrader.setQuantity(-orderQuantity);
                    alphaTrader.setPrice(currentFuturePrice);
                    break;
                case STOP_BUY:
                    //покупка фьючерса через quik
                    qt = quikService.buyFutures(transactionId, futureSymbol, orderFuturePrice, orderQuantity);

                    //обновление баланса
                    alphaTrader.addBalance(orderQuantity*(alphaTrader.getPrice() - currentFuturePrice));

                    //установка цены и количества заявки
                    alphaTraderData.setQuantity(orderQuantity);
                    alphaTrader.setQuantity(0);
                    alphaTrader.setPrice(0);
                    break;
                case STOP_SELL:
                    //продажа фьючерса через quik
                    qt = quikService.sellFutures(transactionId, futureSymbol, orderFuturePrice, orderQuantity);

                    //обновление баланса
                    alphaTrader.addBalance(orderQuantity*(currentFuturePrice - alphaTrader.getPrice()));

                    //установка цены и количества заявки
                    alphaTraderData.setQuantity(orderQuantity);
                    alphaTrader.setQuantity(0);
                    alphaTrader.setPrice(0);
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            alphaTraderBean.save(alphaTrader);

            update(qt, alphaTraderData);

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
        return alphaTrader.getAlphaOracle().getId();
    }

    private void update(QuikTransaction quikTransaction, AlphaTraderData alphaTraderData){
        alphaTraderData.setReplyCode((int) quikTransaction.getReplyCode().getValue());
        alphaTraderData.setResult(quikTransaction.getResult().intValue());
        alphaTraderData.setOrderNum((long) quikTransaction.getOrderNum().getValue());
        alphaTraderData.setResultMessage(Native.toString(quikTransaction.getResultMessage(), "cp1251"));
        alphaTraderData.setErrorMessage(Native.toString(quikTransaction.getErrorMessage(), "cp1251"));

        alphaTraderBean.save(alphaTraderData);
    }

    private float getOrderPrice(Prediction prediction, float price){
        switch (prediction){
            case LONG:
            case STOP_BUY:
                return price *= 1.002f;
            case SHORT:
            case STOP_SELL:
                return price /= 1.002f;
            default:
                throw new IllegalArgumentException();
        }
    }

    private OrderType getOrder(Prediction prediction){
        switch (prediction){
            case LONG:
            case STOP_BUY:
                return OrderType.BUY;
            case SHORT:
            case STOP_SELL:
                return OrderType.SELL;
            default:
                throw new IllegalArgumentException();
        }
    }
}
