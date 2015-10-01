package ru.inheaven.aida.happy.trading.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.mapper.OrderMapper;
import ru.inheaven.aida.happy.trading.service.DepthService;
import ru.inheaven.aida.happy.trading.service.OrderService;
import ru.inheaven.aida.happy.trading.service.TradeService;
import ru.inheaven.aida.happy.trading.service.UserInfoService;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicLong;

import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_EVEN;
import static ru.inheaven.aida.happy.trading.entity.OrderStatus.*;
import static ru.inheaven.aida.happy.trading.entity.OrderType.*;

/**
 * @author inheaven on 08.07.2015 19:39.
 */
public class LevelStrategy extends BaseStrategy{
    private Logger log = LoggerFactory.getLogger(getClass());

    private SecureRandom random = new SecureRandom("LevelStrategy".getBytes());

    private Strategy strategy;

    private BigDecimal risk = ONE;

    private Map<String, Long> levelTimeMap = new ConcurrentHashMap<>();

    private UserInfoService userInfoService;

    private static AtomicLong positionId = new AtomicLong(System.nanoTime());

    public LevelStrategy(Strategy strategy, OrderService orderService, OrderMapper orderMapper, TradeService tradeService,
                         DepthService depthService, UserInfoService userInfoService) {
        super(strategy, orderService, orderMapper, tradeService, depthService);

        this.strategy = strategy;
        this.userInfoService = userInfoService;

        if (strategy.getSymbolType() != null) {
            userInfoService.createUserInfoObservable(strategy.getAccount().getId(), strategy.getSymbol().substring(0, 3))
                    .filter(u -> u.getRiskRate() != null)
                    .subscribe(u -> {
                        if (u.getRiskRate().compareTo(BigDecimal.valueOf(10)) < 0){
                            risk = TEN.divide(u.getRiskRate(), 8, HALF_EVEN).pow(3).setScale(2, HALF_EVEN);
                        }else {
                            risk = ONE;
                        }
                    });
        }
    }

    private void actionLevel(String key, BigDecimal price, OrderType orderType){
        for (int i = 0; i < strategy.getLevelSize().intValue(); ++i){
            action(key, price.add(BigDecimal.valueOf(isReversing(price) ? i : -i).multiply(getSpreadF(price))), orderType);
        }

        //WAIT PUSH
        getOrderMap().forEachValue(64, (o) -> {
            if (o.getStatus().equals(WAIT)) {
                if (BUY_SET.contains(o.getType()) && o.getPrice().compareTo(price) >= 0){
                    pushWaitOrderAsync(o);
                }else if (SELL_SET.contains(o.getType()) && o.getPrice().compareTo(price) <= 0){
                    Order buy = getOrderMap().searchValues(64, (open)->{
                        if (open.getPositionId().equals(o.getPositionId()) && BUY_SET.contains(open.getType())){
                            return open;
                        }

                        return null;
                    });

                    if (buy == null) {
                        pushWaitOrderAsync(o);
                    }
                }
            }
        });
    }

    private boolean isReversing(BigDecimal price){
        UserInfoTotal avg = userInfoService.getAvg(strategy.getAccount().getId());

        if (avg != null){
            if (strategy.getSymbol().contains("LTC")) {
                return price.compareTo(avg.getLtcPrice()) > 0;
            } else {
                return strategy.getSymbol().contains("BTC") && price.compareTo(avg.getBtcPrice()) > 0;
            }
        }

        return false;
    }

    private BigDecimal getSpread(BigDecimal price){
        return strategy.getSymbolType() == null ? strategy.getLevelSpread().multiply(price) : strategy.getLevelSpread();
    }

    private BigDecimal getSpreadF(BigDecimal price){
        return getSpread(price).divide(BigDecimal.valueOf(2), 8, HALF_EVEN);
    }

    private Semaphore semaphore = new Semaphore(1);

    private void action(String key, BigDecimal price, OrderType orderType) {
        try {
            semaphore.acquire();

            //REFUSED
            if (System.currentTimeMillis() - getRefusedTime() < 10000){
                return;
            }

            //MEAN REVERSING
            boolean reversing = isReversing(price);

            BigDecimal priceF = reversing ? price.subtract(getStep()) : price.add(getStep());
            BigDecimal spread = getSpread(priceF);
            BigDecimal spreadF = getSpreadF(priceF);

            if (strategy.getSymbolType() == null && strategy.getAccount().getExchangeType().equals(ExchangeType.OKCOIN)) {
                BigDecimal minSpread = price.multiply(new BigDecimal("0.0014"));

                if (spread.compareTo(minSpread) < 0){
                    spread = minSpread;

                    log.warn("LOW Spread -> {} {} {} {}", strategy.getId(), minSpread.setScale(3, HALF_EVEN), strategy.getSymbol(),
                            Objects.toString(strategy.getSymbolType(), ""));
                }
            }

            BigDecimal spreadPF = spread.add(spreadF);
            BigDecimal level = spreadF.divideToIntegralValue(spread);

            Order search = getOrderMap().searchValues(64, (o) -> {
                if (LONG.contains(o.getType()) && (OPEN.equals(o.getStatus()) || CREATED.equals(o.getStatus()) || WAIT.equals(o.getStatus())) &&
                        ((SELL_SET.contains(o.getType()) &&
                                o.getPrice().subtract(priceF).abs().compareTo(reversing ? spreadF : spreadPF) <= 0) ||
                        (BUY_SET.contains(o.getType()) &&
                                priceF.subtract(o.getPrice()).abs().compareTo(reversing ? spreadPF : spreadF) <= 0))){
                    return o;
                }

                return null;
            });

            if (search == null){
                log.info(key + " {} {} {}", price.setScale(3, HALF_EVEN), orderType, reversing);

                BigDecimal amountHFT = strategy.getLevelLot();

//                if (levelTimeMap.get(level.toString() + reversing) != null){
//                    Long time = System.currentTimeMillis() - levelTimeMap.get(level.toString() + reversing);
//
//                    if (strategy.getSymbolType() == null){
//                        if (time < 600000 && strategy.getAccount().getExchangeType().equals(ExchangeType.OKCOIN)){
//                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(10.0 - 9.0*time/600000));
//
//                            log.info("HFT -> {}s {} {} -> {}", time/1000, price.setScale(3, HALF_EVEN),
//                                    strategy.getLevelLot().setScale(3, HALF_EVEN), amountHFT.setScale(3, HALF_EVEN));
//                        }
//
//                        if (time < 5000 && strategy.getAccount().getExchangeType().equals(ExchangeType.OKCOIN_CN)){
//                            amountHFT = amountHFT.multiply(BigDecimal.valueOf(3.0 - 2.0*time/5000));
//
//                            log.info("HFT -> {}ms {} {} -> {}", time, price.setScale(3, HALF_EVEN),
//                                    strategy.getLevelLot().setScale(3, HALF_EVEN), amountHFT.setScale(3, HALF_EVEN));
//                        }
//                    }else if (time < 15000){
//                        amountHFT = amountHFT.multiply(BigDecimal.valueOf(2));
//
//                        log.info("HFT -> {}s {} {} {}", time/1000, price.setScale(3, HALF_EVEN),
//                                amountHFT.setScale(3, HALF_EVEN), strategy.getSymbolType());
//                    }
//                }

                if (risk.compareTo(ONE) > 0){
                    log.warn("RISK RATE {} {} {}", risk.setScale(3, HALF_EVEN), strategy.getSymbol(),
                            Objects.toString(strategy.getSymbolType(), ""));
                }

                Long positionId = LevelStrategy.positionId.incrementAndGet();

                BigDecimal buyAmount = amountHFT;
                BigDecimal sellAmount = amountHFT;

                if (reversing){
                    if (strategy.getSymbolType() == null){
                        sellAmount = amountHFT;
                        buyAmount = amountHFT.add(amountHFT.multiply(spread).divide(priceF.subtract(spread), 8, HALF_EVEN));
                    }

                    createWaitOrder(new Order(strategy, positionId, strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                            priceF, sellAmount));
                    createWaitOrder(new Order(strategy, positionId, strategy.getSymbolType() != null ? OPEN_LONG : BID,
                            priceF.subtract(spread), buyAmount));
                }else{
                    if (strategy.getSymbolType() == null){
                        buyAmount = amountHFT.add(amountHFT.multiply(spread).divide(priceF, 8, HALF_EVEN));
                        sellAmount = amountHFT;
                    }

                    createWaitOrder(new Order(strategy, positionId, strategy.getSymbolType() != null ? OPEN_LONG : BID,
                            priceF, buyAmount));
                    createWaitOrder(new Order(strategy, positionId, strategy.getSymbolType() != null ? CLOSE_LONG : ASK,
                            priceF.add(spread), sellAmount));
                }

                if (orderType.equals(ASK)) {
                    levelTimeMap.put(level.toString() + reversing,  System.currentTimeMillis());
                }
            }
        } catch (Exception e) {
            log.error("action error -> ", e);
        }finally {
            semaphore.release();
        }
    }

    private BigDecimal lastTrade = ZERO;

    @Override
    protected void onTrade(Trade trade) {
        if (lastTrade.compareTo(ZERO) == 0 || lastTrade.subtract(trade.getPrice()).abs()
                .divide(lastTrade, 8, HALF_EVEN).compareTo(new BigDecimal("0.05")) < 0){
            actionLevel("on trade", trade.getPrice(), trade.getOrderType());
        }else{
            log.warn("trade price diff 5% than last trade {} {} {}", trade.getPrice(), trade.getSymbol(), Objects.toString(trade.getSymbolType(), ""));
        }

        lastTrade = trade.getPrice();
    }

    @Override
    protected void onDepth(Depth depth) {
        BigDecimal spread = strategy.getLevelSpread();

        BigDecimal ask = depth.getAsk();
        BigDecimal bid = depth.getBid();

        if (ask != null && bid != null) {
            if (ask.subtract(bid).abs().compareTo(spread.multiply(BigDecimal.valueOf(2))) > 0 && ask.compareTo(bid) > 0){
                action("on depth ask", ask, ASK);
                action("on depth bid", bid, BID);

                action("on depth ask spread", ask.subtract(spread), ASK);
                action("on depth bid spread", bid.add(spread), BID);
            }
        }
    }

    @Override
    protected void onRealTrade(Order order) {
        if (order.getStatus().equals(CLOSED) && order.getAvgPrice().compareTo(ZERO) > 0){
            actionLevel("on real trade", order.getAvgPrice(), order.getType());
        }
    }

    private BigDecimal getStep(){
        switch (strategy.getSymbol()){
            case "BTC/USD":
            case "BTC/CNY":
            case "LTC/CNY":
                return new BigDecimal("0.01");
            case "LTC/USD":
                return new BigDecimal("0.001");
        }

        return ZERO;
    }
}
