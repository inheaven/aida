package ru.inheaven.aida.backtest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inhell.aida.ssa.VSSABoost;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

/**
 * @author inheaven on 20.06.2016.
 */
public class VSSABoostTest {
    private static Logger log = LoggerFactory.getLogger(VSSABoostTest.class);

    public static void main(String[] args){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 19, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 20, 0, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        List<Trade> trades = Module.getInjector().getInstance(TradeMapper.class).getLightTrades("BTC/CNY", startDate, endDate, 0, 10000000);

        System.out.println(trades.size());

        //filter
        long time = 15000;
        List<Double> filter = new ArrayList<>();

        long last = trades.get(0).getCreated().getTime();

        List<Trade> bid = new ArrayList<>();
        List<Trade> ask = new ArrayList<>();

        //noinspection Duplicates
        for (Trade t : trades){
            (t.getOrderType().equals(OrderType.BID) ? bid : ask).add(t);

            if (t.getCreated().getTime() - last > time){
                double avgBid = bid.stream().mapToDouble(s -> s.getPrice().doubleValue()).average().orElse(0);
                //double avgAsk = ask.stream().mapToDouble(s -> s.getPrice().doubleValue()).average().orElse(0);

                filter.add(avgBid);

                last = t.getCreated().getTime();

                bid.clear();
                ask.clear();
            }
        }

        double[] prices = filter.stream().mapToDouble(d -> d).toArray();

        //vssa boost
        int count = 50;
        int n = 500;
        int m = 20;

        VSSABoost vssaBoost = new VSSABoost(0.48, 11, 50, n, m);

        double[] train = new double[prices.length/2];
        System.arraycopy(prices, 0, train, 0, train.length);

        for (int f = 0; f < 100; ++f) {
            long t = System.currentTimeMillis();

            vssaBoost.fit(train);

            log.info("fit " + (System.currentTimeMillis() - t));

            Random random = new SecureRandom();

            int error = 0;

            for (int j = 0; j < count; ++j){
                int start = random.nextInt(prices.length/2 - n - m - 1) + prices.length/2;

                double[] series = new double[n];
                System.arraycopy(prices, start, series, 0, n);

                double forecast = vssaBoost.execute(series);
                double test = prices[start + n + m - 1] - prices[start + n - 1];

                if ((int)(Math.signum(test) + Math.signum(forecast)) != 0){
                    error++;
                }
            }

            log.info("error " + (double)error/count);
        }
    }
}
