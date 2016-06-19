package ru.inheaven.aida.backtest;

import com.google.common.base.Joiner;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inhell.aida.ssa.VectorForecastSSA;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author inheaven on 17.06.2016.
 */
public class VSSATest {
    public static void main(String[] args){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 18, 3, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 19, 3, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        List<Trade> trades = Module.getInjector().getInstance(TradeMapper.class).getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);

        System.out.println(trades.size());

        Random random = new SecureRandom();

        for (int i2 = 62; i2 < 129; ++i2) {
            for (int i1 = 2; i1 < i2/2; ++i1) {
                for (int i = 1; i < i2/2; ++i){
                    long time = 15000;

                    //filter
                    List<Double> filter = new ArrayList<>();

                    long last = trades.get(0).getCreated().getTime();

                    List<Trade> bid = new ArrayList<>();
                    List<Trade> ask = new ArrayList<>();

                    for (Trade t : trades){
                        (t.getOrderType().equals(OrderType.BID) ? bid : ask).add(t);

                        if (t.getCreated().getTime() - last > time){
                            double avgBid = bid.stream().mapToDouble(s -> s.getPrice().doubleValue()).average().orElse(0);
                            double avgAsk = ask.stream().mapToDouble(s -> s.getPrice().doubleValue()).average().orElse(0);

                            filter.add(avgAsk);

                            last = t.getCreated().getTime();

                            bid.clear();
                            ask.clear();
                        }
                    }

                    double[] prices = filter.stream().mapToDouble(d -> d).toArray();

                    //vssa
                    int error = 0;
                    int count = 100;

                    VectorForecastSSA vssa = new VectorForecastSSA(512, i2, i1, i);

                    for (int j = 0; j < count; ++j){
                        int start = random.nextInt(prices.length - vssa.getN() - vssa.getM());

                        double[] train = new double[vssa.getN()];

                        for (int k = 0; k < train.length; ++k){
//                            train[k] = prices[start + k + 1]/prices[start + k] - 1;
                            train[k] = prices[start + k + 1];
                        }

                        float[] train_f = new float[train.length];
                        for (int l = 0; l < train.length; ++l){
                            train_f[l] = (float) train[l];
                        }

                        float[] forecasts = vssa.execute(train_f);

//                        double value = 1;
//
//                        for (int m = vssa.getN(); m < forecasts.length; ++m){
//                            value *= forecasts[m] + 1;
//                        }

                        double price = prices[start + vssa.getN()];

//                        double forecast = value*price;
                        double forecast = forecasts[forecasts.length - 1];

                        double predict = prices[start + vssa.getN() + vssa.getM()];

                        if (Double.isNaN(forecast) || (price - predict)*(forecasts[vssa.getN()] - forecast) < 0){
                            error++;
                        }
                    }

                    System.out.println(Joiner.on(" ").join((double)error/count, time, vssa.getN(), vssa.getL(), vssa.getP(), vssa.getM()));
                }
            }
        }
    }
}
