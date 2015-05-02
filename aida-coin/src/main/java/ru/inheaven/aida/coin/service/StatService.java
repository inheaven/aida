package ru.inheaven.aida.coin.service;

import com.google.common.base.Function;
import com.google.common.collect.Maps;
import com.xeiam.xchange.dto.marketdata.Ticker;
import ru.inheaven.aida.coin.entity.*;
import ru.inheaven.aida.predictor.service.PredictorService;

import javax.annotation.Nullable;
import javax.ejb.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.coin.entity.ExchangeType.*;
import static ru.inheaven.aida.coin.entity.OrderStatus.CLOSED;

/**
 * @author inheaven on 16.02.2015 23:35.
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class StatService {
    @EJB
    private StatBean statBean;

    @EJB
    private PredictorService predictorService;

    @EJB
    private TraderBean traderBean;

    @EJB
    private DataService dataService;

    private Map<ExchangePair, BigDecimal> predictionIndexMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, BigDecimal> volatilitySigmaMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, BigDecimal> averageMap = new ConcurrentHashMap<>();

    //@Schedule(second = "*/42", minute="*", hour="*", persistent=false)
    public void scheduleStats(){
        traderBean.getTraders().stream().filter(Trader::isRunning).forEach(trader -> {
            if (trader.isPredicting()) {
                updatePredictionIndex(trader.getExchangePair());
            }

            updateVolatility(trader.getExchangePair());

            updateAverage(trader.getExchangePair());
        });
    }

    public void updateVolatility(ExchangePair exchangePair){
        volatilitySigmaMap.put(exchangePair, statBean.getVolatilitySigma(exchangePair));
    }

    public void updateAverage(ExchangePair exchangePair){
        averageMap.put(exchangePair, statBean.getAverage(exchangePair));
    }

    public void updatePredictionIndex(ExchangePair exchangePair){
        BigDecimal predictionIndex = ZERO;
        int size = PredictorService.SIZE;

        List<TickerHistory> tickerHistories = statBean.getTickerHistories(exchangePair, size);

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

    public BigDecimal getPredictionTestIndex(ExchangePair exchangePair){
        int size = 64;
        int step = 32;

        List<TickerHistory> list = statBean.getTickerHistories(exchangePair, size);

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

    public BigDecimal getBTCVolume(ExchangePair ep, BigDecimal amount, BigDecimal price){
        try {

            if (OKCOIN.equals(ep.getExchangeType())){
                if ("BTC".equals(ep.getCurrency())) {
                    amount =  amount.multiply(BigDecimal.valueOf(10)).divide(dataService.getTicker(ExchangePair.of(OKCOIN, "BTC/USD")).getLast(), 8 , HALF_UP);
                }else if ("LTC".equals(ep.getCurrency())){
                    amount = amount.multiply(BigDecimal.valueOf(1)).divide(dataService.getTicker(ExchangePair.of(OKCOIN, "LTC/USD")).getLast(), 8, HALF_UP);
                }
            }

            BigDecimal volume = amount.multiply(price);

            String pair = ep.getPair();

            if (pair.contains("/BTC")) {
                return volume.setScale(8, HALF_UP);
            } else if (pair.contains("/LTC")) {
                return volume.multiply(dataService.getTicker(ExchangePair.of(CEXIO, "LTC/BTC")).getLast()).setScale(8, HALF_UP);
            } else if (pair.contains("/BC")) {
                return volume.multiply(dataService.getTicker(ExchangePair.of(BITTREX, "BC/BTC")).getLast()).setScale(8, HALF_UP);
            } else if (pair.contains("/USD")) {
                return volume.divide(dataService.getTicker(ExchangePair.of(BTCE, "BTC/USD")).getLast(), 8, HALF_UP);
            } else if (pair.contains("/CNY")) {
                return volume.divide(dataService.getTicker(ExchangePair.of(BTER, "BTC/CNY")).getLast(), 8, HALF_UP);
            }
        } catch (Exception e) {
            //no ticker
        }

        return ZERO;
    }

    public BigDecimal getEstimateBalance(ExchangeType exchangeType, String currency, BigDecimal balance){
        try {
            switch (currency){
                case "BTC":
                    return balance;
                case "USD":
                    return balance.divide(dataService.getTicker(ExchangePair.of(BTCE, "BTC/USD")).getLast(), 8, HALF_UP);
                case "CNY":
                    return balance.divide(dataService.getTicker(ExchangePair.of(BTER, "BTC/CNY")).getLast(), 8, HALF_UP);
                default:
                    Ticker ticker = dataService.getTicker(ExchangePair.of(exchangeType, currency + "/BTC"));

                    if (ticker == null){
                        ticker = dataService.getTicker(ExchangePair.of(BITFINEX, currency + "/BTC"));
                    }

                    return balance.multiply(ticker.getLast()).setScale(8, HALF_UP);
            }
        } catch (Exception e) {
            if (OKCOIN.equals(exchangeType)) {
                throw e;
            } else {
                return ZERO;
            }
        }
    }

    public List<OrderVolume> getOrderVolumeRates(ExchangePair exchangePair, Date startDate){
        List<Volume> volumes = getVolumes(exchangePair, startDate);

        List<OrderVolume> orderVolumes = new ArrayList<>();

        for (int i = 0; i < volumes.size(); ++i){
            OrderVolume orderVolume = new OrderVolume(volumes.get(i).getDate());
            orderVolumes.add(orderVolume);

            for (int j = i; j >= 0; --j){
                Volume v = volumes.get(j);

                orderVolume.addVolume(v.getVolume());

                if (v.getVolume().compareTo(ZERO) > 0){
                    orderVolume.addAskVolume(v.getVolume());
                } else {
                    orderVolume.addBidVolume(v.getVolume());
                }

                if (j == 0 || orderVolume.getDate().getTime() - v.getDate().getTime() > 1000*60*60){
                    break;
                }
            }
        }

        return orderVolumes;
    }

    public OrderVolume getOrderVolumeRate(Date startDate){
        return getOrderVolumeRate(null, startDate);
    }

    public OrderVolume getOrderVolumeRate(ExchangePair exchangePair, Date startDate){
        List<Volume> volumes = getVolumes(exchangePair, startDate);

        OrderVolume orderVolume = new OrderVolume(new Date());

        for (int j = volumes.size() - 1; j >= 0; --j){
            Volume v = volumes.get(j);
            orderVolume.addVolume(v.getVolume());

            if (orderVolume.getDate().getTime() - v.getDate().getTime() < 1000*60*60) {
                if (v.getVolume().compareTo(ZERO) > 0){
                    orderVolume.addAskVolume(v.getVolume());
                } else {
                    orderVolume.addBidVolume(v.getVolume());
                }
            }
        }

        return orderVolume;
    }

    public List<Volume> getVolumes(Date startDate){
        return getVolumes(null, startDate);
    }

    public List<Volume> getVolumes(ExchangePair exchangePair, Date startDate){
        List<Volume> volumes = new ArrayList<>();

        List<Order> orders = exchangePair != null
                ? traderBean.getOrderHistories(exchangePair, CLOSED, startDate)
                : traderBean.getOrderHistories(CLOSED, startDate);

        volumes.addAll(orders.stream().map(order -> new Volume(getBTCVolume(ExchangePair.of(order.getExchangeType(), order.getSymbol()),
                order.getAmount(), order.getPrice()).multiply(BigDecimal.valueOf(OrderType.ASK.equals(order.getType()) ? 1 : -1)),
                order.getClosed())).collect(Collectors.toList()));

        volumes.sort((o1, o2) -> o1.getDate().compareTo(o2.getDate()));

        return volumes;
    }

    public Volume getVolume(BalanceHistory history){
        if (OKCOIN.equals(history.getExchangeType())) {
            return new Volume(history.getPrevious().getBalance().subtract(history.getBalance()), history.getDate());
        } else {
            return new Volume(getBTCVolume(ExchangePair.of(history.getExchangeType(), history.getPair()),
                    history.getPrevious().getBalance().add(history.getPrevious().getAskAmount())
                            .subtract(history.getBalance().add(history.getAskAmount())),
                    (history.getPrice().subtract(history.getPrevious().getPrice()))), history.getDate());
        }
    }





    public BigDecimal getOrderStatProfit(ExchangePair exchangePair, Date startDate){
        List<OrderStat> orderStats = traderBean.getOrderStats(exchangePair, startDate);

        if (orderStats.size() < 2){
            return ZERO;
        }


        Map<OrderType, OrderStat> map = Maps.uniqueIndex(orderStats, new Function<OrderStat, OrderType>() {
            @Nullable
            @Override
            public OrderType apply(@Nullable OrderStat input) {
                return input != null ? input.getType() : null;
            }
        });

        if (exchangePair.getExchangeType().equals(OKCOIN)){
            int contract = exchangePair.getPair().contains("BTC/") ? 100 :10;

            return BigDecimal.valueOf((contract/map.get(OrderType.BID).getAvgPrice().doubleValue() - contract/map.get(OrderType.ASK).getAvgPrice().doubleValue())
                    * (map.get(OrderType.ASK).getSumAmount().add(map.get(OrderType.BID).getSumAmount()).intValue())).setScale(8, HALF_UP);
        }

        BigDecimal priceDiff = ZERO;
        BigDecimal minAmount = ZERO;

        for (OrderStat orderStat : orderStats){
            priceDiff = orderStat.getType().equals(OrderType.ASK)
                    ? priceDiff.add(orderStat.getAvgPrice())
                    : priceDiff.subtract(orderStat.getAvgPrice());

            if (minAmount.equals(ZERO) || minAmount.compareTo(orderStat.getSumAmount()) > 0){
                minAmount = orderStat.getSumAmount();
            }
        }

        return getBTCVolume(exchangePair, minAmount, priceDiff);
    }

    public List<TickerHistory> getTickerHistories(ExchangePair exchangePair, Date startDate) {
        return statBean.getTickerHistories(exchangePair, startDate);
    }
}
