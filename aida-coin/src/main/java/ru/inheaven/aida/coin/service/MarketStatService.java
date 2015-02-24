package ru.inheaven.aida.coin.service;

import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.TickerHistory;
import ru.inheaven.aida.predictor.service.PredictorService;

import javax.ejb.*;
import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;

/**
 * @author inheaven on 16.02.2015 23:35.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class MarketStatService {
    @EJB
    private StatBean statBean;

    private Map<ExchangePair, BigDecimal> predictionIndexMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, BigDecimal> volatilitySigmaMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, BigDecimal> averageMap = new ConcurrentHashMap<>();

    @Schedule
    public void updateVolatility(ExchangePair exchangePair){
        volatilitySigmaMap.put(exchangePair, traderBean.getSigma(exchangePair));
    }

    public void updateAverage(ExchangePair exchangePair){
        averageMap.put(exchangePair, traderBean.getAverage(exchangePair));
    }

    public BigDecimal getVolatilitySigma(ExchangePair exchangePair){
        return volatilitySigmaMap.get(exchangePair);
    }

    public BigDecimal getAverage(ExchangePair exchangePair){
        BigDecimal average = averageMap.get(exchangePair);

        return average != null ? average : ZERO;
    }

    public BigDecimal getPredictionIndex(ExchangePair exchangePair){
        return predictionIndexMap.get(exchangePair) != null ? predictionIndexMap.get(exchangePair) : ZERO;
    }

    public void updatePredictionIndex(ExchangePair exchangePair){
        BigDecimal predictionIndex = ZERO;
        int size = PredictorService.SIZE;

        List<TickerHistory> tickerHistories = traderBean.getTickerHistories(exchangePair, size);

        if (tickerHistories.size() == size){
            double[] timeSeries = new double[size];

            for (int i=0; i < size; ++i){
                timeSeries[i] = tickerHistories.get(i).getPrice().doubleValue();
            }

            double index = (predictorService.getPrediction(timeSeries) - timeSeries[size-1]) / timeSeries[size-1];

            try {
                predictionIndex = BigDecimal.valueOf(Math.abs(index) < 1 ? index : Math.signum(index));
            } catch (Exception e) {
                //
            }
        }

        predictionIndexMap.put(exchangePair, predictionIndex);
    }

    public BigDecimal getPredictionTestIndex(ExchangePair exchangePair){
        int size = 64;
        int step = 32;

        List<TickerHistory> list = traderBean.getTickerHistories(exchangePair, size);

        if (list.size() == size){
            int p = 0;

            for (int i = 0; i < size-step; ++i){
                for (int j = step/2; j < step; ++j){
                    if (list.get(i).getPrediction() != null
                            && (list.get(i + j).getPrice().doubleValue() - list.get(i).getPrice().doubleValue() *
                            list.get(i).getPrediction().doubleValue()) >= 0){
                        p++;
                        break;
                    }
                }
            }

            return BigDecimal.valueOf(100* p / (size-step)).setScale(2, HALF_UP);
        }

        return ZERO;
    }
}
