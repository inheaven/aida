package ru.inhell.aida.statistic;

import com.google.inject.Inject;
import ru.inhell.aida.entity.*;
import ru.inhell.aida.oracle.AlphaOracleBean;
import ru.inhell.aida.oracle.AlphaOracleService;
import ru.inhell.aida.trader.AlphaTraderBean;
import ru.inhell.aida.trader.AlphaTraderService;
import ru.inhell.aida.util.DateUtil;

import java.util.Date;
import java.util.List;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.04.11 16:31
 */
public class AlphaStatisticService {
    @Inject
    private AlphaOracleBean alphaOracleBean;

    @Inject
    private AlphaTraderBean alphaTraderBean;

     public float getBalance(Long alphaTraderId, Date startDate, Date endDate){
        List<AlphaTraderData> list = alphaTraderBean.getAlphaTraderDatas(new AlphaTraderFilter(alphaTraderId, startDate, endDate));

        float currentBalance = 0;

        for (int i = 1; i < list.size(); ++i){
            AlphaTraderData d0 = list.get(i-1);
            AlphaTraderData d1 = list.get(i);

            currentBalance += (d1.getPrice() - d0.getPrice())*d1.getQuantity()*(d1.getOrder().equals(Order.BUY)? 1 : -1);
        }

        return currentBalance;
    }

    public float getCurrentBalance(Long alphaTraderId){
        return getBalance(alphaTraderId, DateUtil.getCurrentStartTradeTime(), DateUtil.getCurrentEndTradeTime());
    }

    public float getPreviousBalance(Long alphaTraderId){
        return getBalance(alphaTraderId, DateUtil.getPreviousStartTradeTime(), DateUtil.getPreviousEndTradeTime());
    }

    public float getAllBalance(Long alphaTraderId){
        return 1000000;
    }

    public float getScore(Long alphaOracleId, Date startDate, Date endDate){
        List<AlphaOracleData> list = alphaOracleBean.getAlphaOracleDatas(new AlphaOracleFilter(alphaOracleId, startDate, endDate));

        float currentScore = 0;

        for (int i = 1; i < list.size(); ++i){
            AlphaOracleData d0 = list.get(i-1);
            AlphaOracleData d1 = list.get(i);

            int quantity = 0;

            switch (d1.getPrediction()){
                case STOP_BUY: quantity = 1; break;
                case STOP_SELL: quantity = -1; break;
                case LONG: quantity = 2; break;
                case SHORT: quantity = -2; break;
            }

            currentScore += (d1.getPrice() - d0.getPrice())*quantity;
        }

        return currentScore;
    }

    public float getCurrentScore(Long alphaOracleId){
        return getScore(alphaOracleId, DateUtil.getCurrentStartTradeTime(), DateUtil.getCurrentEndTradeTime());
    }

    public float getPreviousScore(Long alphaOracleId){
        return getScore(alphaOracleId, DateUtil.getPreviousStartTradeTime(), DateUtil.getPreviousEndTradeTime());
    }

    public float getAllScore(Long alphaOracleId){
        return 1000000;
    }

    public long getCurrentOrderCount(Long alphaTraderId){
        return alphaTraderBean.getAlphaTraderDatasCount(new AlphaTraderFilter(alphaTraderId, DateUtil.getCurrentStartTradeTime(),
                DateUtil.getCurrentEndTradeTime()));
    }
}
