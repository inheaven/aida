package ru.inheaven.aida.backtest;

import com.google.common.primitives.Doubles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ujmp.core.util.UJMPSettings;
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
 * @author inheaven on 04.08.2016.
 */
public class VSSABoostSymTest {
    private static Logger log = LoggerFactory.getLogger(VSSABoostTest.class);

    public static void main(String[] args){
        Date startDate = Date.from(LocalDateTime.of(2016, 8, 2, 12, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 4, 20, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        List<Trade> trades = new ArrayList<>();

        long delay = (long) (1000*60*5);

        for (long t = startDate.getTime(); t < endDate.getTime(); t += delay){
            trades.addAll(Module.getInjector().getInstance(TradeMapper.class).getLightTrades("BTC/CNY", null, new Date(t), new Date(t + delay)));
        }

        System.out.println("trades " + trades.size());

        UJMPSettings.getInstance().setNumberOfThreads(2);

        //filter
        long time = 1000;
        List<Double> filter = new ArrayList<>();

        long last = trades.get(0).getCreated().getTime();

        List<Trade> avg = new ArrayList<>();

        //noinspection Duplicates
        for (Trade t : trades){
            avg.add(t);

            if (t.getCreated().getTime() - last > time){
                double sum = avg.stream().mapToDouble(s -> s.getPrice().doubleValue()*s.getAmount().doubleValue()).sum();
                double volume = avg.stream().mapToDouble(s -> s.getAmount().doubleValue()).sum();

                filter.add(sum/volume);

                last = t.getCreated().getTime();
                avg.clear();
            }
        }

        double[] prices = filter.stream().mapToDouble(d -> d).toArray();

        System.out.println("prices " + prices.length);

        Random random = new SecureRandom("aira".getBytes());

        //vssa boost
        for (int r = 0; r < 100; r++) {
            int count = 10;
            int n = 150 + random.nextInt(150);
            int m = 5;

            VSSABoost vssaBoost = new VSSABoost(0.3819660112501453, 11, 100, n, m);

            double[] train = new double[2*prices.length/3];
            System.arraycopy(prices, 0, train, 0, train.length);

            for (int f = 0; f < 100; ++f) {
                long t = System.currentTimeMillis();

                vssaBoost.fit(Doubles.asList(train));

                log.info("fit " + (System.currentTimeMillis() - t));

                int error = 0;

                for (int j = 0; j < count; ++j){
                    int start = random.nextInt(Math.abs(prices.length/3 - n - m - 1)) + (2*prices.length/3);

                    double[] series = new double[n];
                    System.arraycopy(prices, start, series, 0, n);

                    double forecast = vssaBoost.execute(series);
                    double test = vssaBoost.getTarget(prices, start + n, m) - prices[start + n - 1];

                    if ((int)Math.signum(test) != (int)Math.signum(forecast)){
                        error++;
                    }
                }

                log.info("error " + (double)error/count);
            }
        }
    }
}
