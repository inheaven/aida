package ru.inheaven.aida.backtest;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.mapper.TradeMapper;
import ru.inheaven.aida.happy.trading.service.Module;
import ru.inheaven.aida.happy.trading.util.OrderMap;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

import static java.math.BigDecimal.ZERO;

/**
 * inheaven on 04.05.2016.
 */
public class LevelBacktest {
    public static void main(String[] args){
        TradeMapper tradeMapper = Module.getInjector().getInstance(TradeMapper.class);

        Date startDate = Date.from(LocalDateTime.of(2016, 1, 1, 0, 0).atZone(ZoneId.systemDefault()).toInstant());
        Date endDate = Date.from(LocalDateTime.of(2016, 5, 5, 0, 0).atZone(ZoneId.systemDefault()).toInstant());

//        LevelBacktest levelBacktest = new LevelBacktest();
    }

    private BigDecimal subtotalCny;
    private BigDecimal subtotalBtc;
    private long clearDelay;
    private double clearRange;
    private int tradeSize;
    private double spreadDiv;
    private double levelAmount;
    private int balanceDelay;
    private double balanceValue;

    private OrderMap orderMap = new OrderMap(2);

    private List<BigDecimal> totalMetric = new ArrayList<>();

    private BigDecimal buyPrice = ZERO;
    private BigDecimal buyVolume = ZERO;

    private BigDecimal sellPrice = ZERO;
    private BigDecimal sellVolume = ZERO;

    public LevelBacktest(BigDecimal subtotalCny, long clearDelay, double clearRange, int tradeSize,
                         double spreadDiv, double levelAmount, int balanceDelay, double balanceValue) {
        this.subtotalCny = subtotalCny;
        this.clearDelay = clearDelay;
        this.clearRange = clearRange;
        this.tradeSize = tradeSize;
        this.spreadDiv = spreadDiv;
        this.levelAmount = levelAmount;
        this.balanceDelay = balanceDelay;
        this.balanceValue = balanceValue;
    }

    private void clear(BigDecimal price){
        BigDecimal range = BigDecimal.valueOf(getSpread() * clearRange);

        orderMap.getBidMap().headMap(price.subtract(range)).forEach((k, v) -> v.forEach(o -> orderMap.remove(o.getInternalId())));
        orderMap.getAskMap().tailMap(price.add(range)).forEach((k, v) -> v.forEach(o -> orderMap.remove(o.getInternalId())));
    }

    private BigDecimal getFreeBtc(){
        return subtotalBtc.subtract(orderMap.getAskMap().values().stream()
                .flatMap(Collection::stream)
                .map(Order::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private BigDecimal getFreeCny(){
        return subtotalCny.subtract(orderMap.getBidMap().values().stream()
                .flatMap(Collection::stream)
                .map(o -> o.getAmount().multiply(o.getPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add));
    }

    private Deque<Trade> tradeQueue = new LinkedList<>();

    private void addTrade(Trade trade){
        if (tradeQueue.size() > tradeSize){
            tradeQueue.removeFirst();
        }

        tradeQueue.add(trade);
    }

    private StandardDeviation standardDeviation = new StandardDeviation(true);

    private double getStdDev(){
        return standardDeviation.evaluate(tradeQueue.stream().mapToDouble(t -> t.getPrice().doubleValue()).toArray());
    }

    private double getSpread(){
        return getStdDev() / spreadDiv;
    }

    private boolean isUp(BigDecimal price){
        return subtotalCny.divide(subtotalBtc.multiply(price), 8, RoundingMode.HALF_UP).compareTo(BigDecimal.valueOf(balanceValue)) > 0;
    }

    private void action(Trade trade){

    }
}
