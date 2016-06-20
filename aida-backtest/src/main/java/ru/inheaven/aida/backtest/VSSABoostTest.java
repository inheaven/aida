package ru.inheaven.aida.backtest;

import com.google.common.base.Joiner;
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
    public static void main(String[] args){
        Date startDate = Date.from(LocalDateTime.of(2016, 6, 18, 3, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 6, 19, 3, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

        List<Trade> trades = Module.getInjector().getInstance(TradeMapper.class).getLightTrades("BTC/CNY", startDate, endDate, 0, 1000000);

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
                double avgAsk = ask.stream().mapToDouble(s -> s.getPrice().doubleValue()).average().orElse(0);

                filter.add(avgAsk);

                last = t.getCreated().getTime();

                bid.clear();
                ask.clear();
            }
        }

        double[] prices = filter.stream().mapToDouble(d -> d).toArray();

        //vssa boost
        int error = 0;
        int count = 100;
        int n = 512;
        int m = 1;

        VSSABoost vssaBoost = new VSSABoost(0.45, 10, 100, n, m);

        double[] train = new double[prices.length/2];
        System.arraycopy(prices, 0, train, 0, train.length);

        vssaBoost.fit(train);

        Random random = new SecureRandom();

        for (int j = 0; j < count; ++j){
            int start = random.nextInt(prices.length/2 - n - m - 1) + prices.length/2;

            double[] test = new double[n];
            System.arraycopy(prices, start, test, 0, n);

            double up = vssaBoost.execute(test);

            if ((prices[start + n + m - 1] - prices[start + n - 1])*(up > 0.5 ? 1 : -1) < 0){
                error++;
            }
        }

        System.out.println(Joiner.on(" ").join((double)error/count, time));

        vssaBoost.getList().forEach((v) -> System.out.println(v.getError() + " " + v.getName()));
    }
}
