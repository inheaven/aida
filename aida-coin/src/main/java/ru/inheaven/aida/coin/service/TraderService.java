package ru.inheaven.aida.coin.service;

import com.google.common.base.Throwables;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinCrossPosition;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinCrossPositionResult;
import com.xeiam.xchange.okcoin.service.polling.OkCoinTradeServiceRaw;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.*;
import ru.inheaven.aida.coin.util.TraderUtil;

import javax.ejb.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.xeiam.xchange.dto.Order.OrderType.ASK;
import static com.xeiam.xchange.dto.Order.OrderType.BID;
import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.coin.entity.ExchangeType.*;
import static ru.inheaven.aida.coin.entity.OrderStatus.CANCELED;
import static ru.inheaven.aida.coin.entity.TraderType.LONG;
import static ru.inheaven.aida.coin.entity.TraderType.SHORT;
import static ru.inheaven.aida.coin.service.ExchangeApi.getExchange;
import static ru.inheaven.aida.coin.util.TraderUtil.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 17:06
 */
@Startup
@Singleton
@Lock(LockType.READ)
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class TraderService extends AbstractService{
    private Logger log = LoggerFactory.getLogger(TraderService.class);

    @EJB
    private EntityBean entityBean;

    @EJB
    private AccountService accountService;

    @EJB
    private DataService dataService;

    @EJB
    private TraderBean traderBean;

    @EJB
    private OrderService orderService;

    @EJB
    private StatService statService;

    private Map<ExchangePair,Integer> errorMap = new ConcurrentHashMap<>();
    private Map<ExchangePair,  Long> errorTimeMap = new ConcurrentHashMap<>();

    //@Schedule(second = "*/5", minute="*", hour="*", persistent=false)
    public void scheduleFuturePosition(){
        try {
            //balance
            //balanceOKCoinWeekPosition("BTC/USD");
            balanceOKCoinWeekPosition("LTC/USD");

            int levels = 150;
            int balancing = 11;

            double spread;
            try {
                spread = getSpread(ExchangePair.of(OKCOIN, "LTC/USD")).doubleValue();
            } catch (Exception e) {
                return;
            }

            OkCoinCrossPositionResult week = ((OkCoinTradeServiceRaw)getExchange(OKCOIN).getPollingTradeService()).getCrossPosition("ltc_usd", "this_week");

            OkCoinCrossPositionResult quarter = ((OkCoinTradeServiceRaw)getExchange(OKCOIN).getPollingTradeService()).getCrossPosition("ltc_usd", "quarter");

            double last = dataService.getTicker(ExchangePair.of(OKCOIN, "LTC/USD")).getLast().doubleValue();
            double delta = spread / last;

            Futures futures = new Futures();

            OkCoinCrossPosition p = week.getPositions()[0];
            OkCoinCrossPosition q = quarter.getPositions()[0];

            //long

            double buyProfit = 0;
            double sellProfit = 0;

            int buyAmount = p.getBuyAmount().intValue() +  q.getBuyAmount().intValue();
            int sellAmount = p.getSellAmount().intValue() + q.getSellAmount().intValue();

            futures.setMargin(BigDecimal.valueOf(buyProfit + sellProfit));
            futures.setRealProfit(p.getBuyProfitReal().add(p.getSellProfitReal()));
            futures.setAvgPosition(BigDecimal.valueOf(last).add(p.getBuyAmount().subtract(p.getSellAmount()).multiply(BigDecimal.valueOf(spread))));

            double price0 = last;
            double price = last * (1 + delta);

            for (int i = 1; i < levels; ++i){
                buyProfit += (buyAmount - i) * (10/price0 - 10/price);
                futures.getBids().add(new Position(buyProfit, price));

                sellProfit -= (sellAmount + i) * (10/price0 - 10/price);
                futures.getAsks().add(new Position(sellProfit, price));

                if (buyAmount - i < balancing){
                    double b = (buyAmount + sellAmount)*0.6;
                    buyAmount += b;
                    sellAmount -= b;
                }

                price0 = price;
                price *= (1 + delta);
            }

            //short

            buyProfit =  0;
            sellProfit =  0;

            buyAmount = p.getBuyAmount().intValue() +  q.getBuyAmount().intValue();
            sellAmount = p.getSellAmount().intValue() + q.getSellAmount().intValue();

            price0 = last;
            price = last * (1 - delta);

            for (int i = -1; i > -levels; --i){
                buyProfit += (buyAmount - i) * (10/price0 - 10/price);
                futures.getBids().add(new Position(buyProfit, price));

                sellProfit -= (sellAmount + i) * (10/price0 - 10/price);
                futures.getAsks().add(new Position(sellProfit, price));

                if (sellAmount + i < balancing){
                    double b = (buyAmount + sellAmount)*0.6;
                    buyAmount -= b;
                    sellAmount += b;
                }

                price0 = price;
                price *= (1 - delta);
            }

            //sort
            futures.getAsks().sort((o1, o2) -> o1.getPrice().compareTo(o2.getPrice()));
            futures.getBids().sort((o1, o2) -> o1.getPrice().compareTo(o2.getPrice()));

            for (int i = 0; i < 2*levels - 2; ++i){
                Position bid = futures.getBids().get(i);
                Position ask = futures.getAsks().get(i);

                futures.getEquity().add(new Position(ask.getAmount().add(bid.getAmount()).setScale(4, ROUND_UP),
                        ask.getPrice().add(bid.getPrice()).divide(BigDecimal.valueOf(2), 4, ROUND_UP)));
            }

            //broadcast
            broadcast(OKCOIN, futures);
        } catch (IOException e) {
            log.error("scheduleFuturePosition error", e);

            broadcast(OKCOIN, e);
        }
    }


    private void balanceOKCoinWeekPosition(String pair) {
        int minAmount = 11;

        try {
            OkCoinCrossPositionResult thisWeek = ((OkCoinTradeServiceRaw)getExchange(OKCOIN).getPollingTradeService())
                    .getCrossPosition(pair.toLowerCase().replace("/", "_"), "this_week");

            if (thisWeek.getPositions().length > 0){
                OkCoinCrossPosition tw = thisWeek.getPositions()[0];

                PollingTradeService tradeService = getExchange(OKCOIN).getPollingTradeService();
                Ticker ticker = dataService.getTicker(ExchangePair.of(OKCOIN, pair));

                boolean _short = tw.getSellAmount().intValue() < minAmount;

                BigDecimal sumAmount = tw.getSellAmount().add(tw.getBuyAmount());

                BigDecimal a1 = sumAmount.multiply(BigDecimal.valueOf(0.22));

                if ((tw.getBuyAmount().intValue() < minAmount && tw.getSellAmount().intValue() > 2*minAmount)
                        || (tw.getSellAmount().intValue() < minAmount && tw.getBuyAmount().intValue() > 2*minAmount)){

                    //balance
                    BigDecimal price = _short
                            ? ticker.getBid().multiply(BigDecimal.valueOf(0.99))
                            : ticker.getAsk().multiply(BigDecimal.valueOf(1.01));

                    com.xeiam.xchange.dto.Order.OrderType orderType = _short ? ASK : BID;

                    String id = tradeService.placeLimitOrder(new LimitOrder(orderType, a1, getCurrencyPair(pair),
                            _short ? "LONG" : "SHORT", new Date(), price));
                    entityBean.save(new Order(id, OKCOIN, pair, OrderType.valueOf(orderType.name()), a1, price, new Date()));

                    id = tradeService.placeLimitOrder(new LimitOrder(orderType, a1, getCurrencyPair(pair),
                            _short ? "SHORT" : "LONG", new Date(), price));
                    entityBean.save(new Order(id, OKCOIN, pair, OrderType.valueOf(orderType.name()), a1, price, new Date()));
                }
            }
        } catch (Exception e) {
            log.error("balanceOKCoinWeekPosition error", e);

            broadcast(OKCOIN, e);
        }
    }

    public BigDecimal getSpread(ExchangePair exchangePair){
        BigDecimal spread;

        Ticker ticker = dataService.getTicker(exchangePair);
        BigDecimal price = ticker.getLast();

        //bitfinex spread
        switch (exchangePair.getExchangeType()){
            case BITFINEX:
                spread = price.multiply(new BigDecimal("0.004")).setScale(8, HALF_UP);
                break;
            case OKCOIN:
                if (exchangePair.getPair().contains("LTC/")){
                    spread = price.multiply(new BigDecimal("0.0022")).setScale(8, HALF_UP);
                }else{
                    spread = price.multiply(new BigDecimal("0.005")).setScale(8, HALF_UP);
                }

                break;
            default:
                spread = price.multiply(new BigDecimal("0.005")).setScale(8, HALF_UP);
        }

        //min spread scale
        if (!exchangePair.getExchangeType().equals(OKCOIN) && exchangePair.getPair().contains("/USD")){
            if (spread.compareTo(new BigDecimal("0.03")) < 0){
                spread = new BigDecimal("0.02").setScale(2, HALF_UP);
            }
        }else if (spread.compareTo(new BigDecimal("0.00000003")) < 0){
            spread = new BigDecimal("0.00000002");
        }

        //volatility
        BigDecimal volatilitySigma = statService.getVolatilitySigma(exchangePair);

        if (volatilitySigma != null && volatilitySigma.compareTo(BigDecimal.valueOf(0.03)) > 0) {
            spread = spread.multiply(ONE.add(volatilitySigma.multiply(BigDecimal.TEN)).pow(2)).setScale(8, HALF_UP);
        }

        //ticker spread
        if (!exchangePair.getExchangeType().equals(OKCOIN)) {
            BigDecimal tickerSpread = ticker.getAsk().subtract(ticker.getBid()).abs();
            if (tickerSpread.compareTo(spread) > 0){
                spread = tickerSpread;
            }
        }

        return spread;
    }

    public BigDecimal getMinOrderVolume(ExchangePair exchangePair) {
        if (BITTREX.equals(exchangePair.getExchangeType())) {
            switch (exchangePair.getCounterSymbol()) {
                case "BTC":
                    return new BigDecimal("0.0013");
                case "LTC":
                    return new BigDecimal("0.08");
                case "BC":
                    return new BigDecimal("13");

                default: return null;
            }
        }else{
            switch (exchangePair.getCounterSymbol()) {
                case "BTC":
                    return new BigDecimal("0.0034");
                case "LTC":
                    return new BigDecimal("0.034");
                case "USD":
                    return new BigDecimal("7");
                case "CNY":
                    return new BigDecimal("34");
                case "BC":
                    return new BigDecimal("34");

                default: return null;
            }
        }
    }

    public BigDecimal getOrderAmount(Trader trader, Ticker ticker){
        if (trader.isFuture()){
            return ONE;
        }

        BigDecimal middlePrice = ticker.getAsk().add(ticker.getBid()).divide(BigDecimal.valueOf(2), 8, HALF_UP);

        BigDecimal minOrderAmount = trader.getLot() != null
                ? trader.getLot().divide(middlePrice, 8, HALF_UP)
                : getMinOrderVolume(trader.getExchangePair()).divide(middlePrice, 8, HALF_UP);

        //volatility todo
//        BigDecimal volatilitySigma = statService.getVolatilitySigma(trader.getExchangePair());
//        minOrderAmount = minOrderAmount.multiply(ONE.add(volatilitySigma.multiply(BigDecimal.valueOf(2*Math.PI)))).setScale(8, HALF_UP);

        return minOrderAmount;
    }

    private void tradeAlpha(Trader trader) throws IOException {
        ExchangeType exchangeType = trader.getExchangeType();

        ExchangePair exchangePair = ExchangePair.of(exchangeType, trader.getPair());
        CurrencyPair currencyPair = getCurrencyPair(trader.getPair());

        Integer errorCount = errorMap.containsKey(exchangePair) ? errorMap.get(exchangePair) : 0;

        try {
            if (errorCount > 2) {
                if (!errorTimeMap.containsKey(exchangePair)) {
                    errorTimeMap.put(exchangePair, System.currentTimeMillis());
                }

                Long errorTime = errorTimeMap.get(exchangePair);
                if (errorTime != null && System.currentTimeMillis() - errorTime > 1000 * 60 * TraderUtil.random.nextInt(10)) {
                    errorMap.remove(exchangePair);
                    errorTimeMap.remove(exchangePair);
                }

                return;
            }

            if (trader.isRunning()) {
                Ticker ticker = dataService.getTicker(exchangePair);

                if (ticker == null || ticker.getLast() == null) {
                    return;
                }

                PollingTradeService tradeService = getExchange(exchangeType).getPollingTradeService();
                AccountInfo accountInfo = accountService.getAccountInfo(exchangeType);

                //middle price
                BigDecimal middlePrice = ticker.getAsk().add(ticker.getBid()).divide(BigDecimal.valueOf(2), 8, HALF_UP);

                if (middlePrice.compareTo(trader.getHigh()) > 0) {
                    errorMap.put(exchangePair, ++errorCount);

                    broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": " + middlePrice.toString()
                            + " > " + trader.getHigh().toString());
                    return;
                }

                if (middlePrice.compareTo(trader.getLow()) < 0) {
                    errorMap.put(exchangePair, ++errorCount);

                    broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": " + middlePrice.toString()
                            + " < " + trader.getLow().toString());

                    return;
                }

                //min spread
                BigDecimal spread = getSpread(exchangePair);

                //min order amount
                BigDecimal orderAmount = getOrderAmount(trader, ticker);

                //open orders
                List<LimitOrder> openOrders = orderService.getOpenOrders(exchangeType).getOpenOrders();

                //cancel orders
                for (LimitOrder order : openOrders) {
                    if ((trader.getType().equals(LONG) && (order.getId().contains("&2") || order.getId().contains("&4")))
                            || ((trader.getType().equals(SHORT) && (order.getId().contains("&1") || order.getId().contains("&3"))))){
                        continue;
                    }

                    if (currencyPair.equals(order.getCurrencyPair())
                            && order.getLimitPrice().subtract(middlePrice).abs()
                            .compareTo(spread.multiply(BigDecimal.valueOf(150))) > 0) {
                        String orderId = order.getId().split("&")[0];

                        //update order status
                        Order h = traderBean.getOrderHistory(orderId);

                        if (h != null){
                            h.setStatus(CANCELED);
                            h.setClosed(new Date());

                            entityBean.save(h);

                            tradeService.cancelOrder(orderId);

                            broadcast(exchangeType, h);
                        }
                    }
                }

                //prediction
                BigDecimal predictionIndex = statService.getPredictionIndex(exchangePair);

                //average
                BigDecimal average = statService.getAverage(exchangePair);

                //internal amount
                BigDecimal internalAmount = ZERO;

                //avg position
                BigDecimal avgPosition = ZERO;

                if (OKCOIN.equals(trader.getExchangeType())){
                    OkCoinCrossPositionResult positions = ((OkCoinTradeServiceRaw)getExchange(OKCOIN)
                            .getPollingTradeService()).getCrossPosition("ltc_usd", "this_week");

                    OkCoinCrossPosition p = positions.getPositions()[0];
                    avgPosition = middlePrice.add(p.getBuyAmount().subtract(p.getSellAmount()).multiply(spread));
                }

                //create order
                for (int index = 1; index <= 11; ++index) {
                    //btc-e spread
                    if (BTCE.equals(trader.getExchangeType())){
                        if (spread.compareTo(new BigDecimal("0.00002")) < 0){
                            spread = new BigDecimal("0.00002").setScale(5, ROUND_UP);
                        }
                    }

                    //magic
                    BigDecimal spreadSumAmount = internalAmount;
                    BigDecimal magic = spread.multiply(BigDecimal.valueOf(index)).add(spread.multiply(BigDecimal.valueOf(1.1)));

                    for (LimitOrder order : openOrders) {
                        if ((trader.getType().equals(LONG) && (order.getId().contains("&2") || order.getId().contains("&4")))
                                || ((trader.getType().equals(SHORT) && (order.getId().contains("&1") || order.getId().contains("&3"))))){
                            continue;
                        }

                        if (currencyPair.equals(order.getCurrencyPair())
                                && order.getLimitPrice().subtract(middlePrice).abs().compareTo(magic) <= 0) {
                            spreadSumAmount = spreadSumAmount.add(order.getTradableAmount());
                        }
                    }
                    if (spreadSumAmount.compareTo(orderAmount.multiply(BigDecimal.valueOf(index*2 - 1))) >= 0) {
                        continue;
                    }

                    //random ask
                    BigDecimal askAmount = orderAmount;
                    BigDecimal bidAmount = orderAmount;

                    //random prediction
                    if (!trader.isFuture()) {
                        if (predictionIndex.compareTo(ZERO) != 0) {
                            askAmount = predictionIndex.compareTo(ZERO) > 0 ? random50(askAmount) : random10(askAmount);
                            bidAmount = predictionIndex.compareTo(ZERO) > 0 ? random10(bidAmount) : random50(bidAmount);
                        }

                        //check ask
                        if (accountInfo.getBalance(currencyPair.counterSymbol).compareTo(askAmount.multiply(middlePrice)) < 0) {
                            broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": Buy "
                                    + askAmount.toString() + " ^ " + middlePrice.toString());

                            errorMap.put(exchangePair, ++errorCount);

                            continue;
                        }

                        //check bid
                        if (accountInfo.getBalance(currencyPair.baseSymbol).compareTo(bidAmount) < 0) {
                            broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": Sell "
                                    + bidAmount.toString() + " ^ " + middlePrice.toString());

                            errorMap.put(exchangePair, ++errorCount);

                            continue;
                        }
                    }else {
                        askAmount = askAmount.setScale(0, HALF_UP);
                        bidAmount = bidAmount.setScale(0, HALF_UP);

                        //[check future create order balance here]
                    }

                    //random shift
                    int shift = 0;
                    if (predictionIndex.compareTo(ZERO) != 0) {
                        shift += predictionIndex.compareTo(ZERO) > 0 ? 1 :  -1;
                    }
                    if (index > 1 && average.compareTo(ZERO) != 0 && avgPosition.compareTo(ZERO) != 0){
                        if (trader.isFuture()) {
                            shift += avgPosition.compareTo(average) > 0
                                    ? LONG.equals(trader.getType()) ? -2 : 2
                                    : LONG.equals(trader.getType()) ? 2 : -2;
                        }else{
                            shift += average.compareTo(avgPosition) > 0 ? 1 : - 1;
                        }
                    }

                    //random ask delta
                    BigDecimal randomAskShift = spread;
                    switch (shift){
                        case -3: randomAskShift = random100(spread).negate(); break;
                        case -2: randomAskShift = random80(spread).negate(); break;
                        case -1:
                            if (index > 1) {
                                randomAskShift = random50(spread).negate();
                            }else {
                                randomAskShift = randomMinus100(spread);
                            }
                            break;
                        case 1: randomAskShift = random50(spread); break;
                        case 2: randomAskShift = random80(spread); break;
                        case 3: randomAskShift = random100(spread); break;
                    }

                    BigDecimal randomAskSpread = spread.multiply(BigDecimal.valueOf(index-1)).add(randomAskShift);

                    if (randomAskSpread.compareTo(ZERO) == 0){
                        randomAskSpread = "USD".equals(currencyPair.counterSymbol)
                                ? new BigDecimal("0.01")
                                : new BigDecimal("0.00000001");
                    }else {
                        if (trader.getExchangeType().equals(OKCOIN) && trader.getPair().contains("LTC/")){
                            randomAskSpread = randomAskSpread.setScale(3, HALF_UP);
                        }else {
                            randomAskSpread = "USD".equals(currencyPair.counterSymbol)
                                    ? randomAskSpread.setScale(2, HALF_UP)
                                    : randomAskSpread.setScale(8, HALF_UP);
                        }
                    }

                    //random bid delta
                    BigDecimal randomBidShift = spread;
                    switch (shift){
                        case -3: randomBidShift = random100(spread); break;
                        case -2: randomBidShift = random80(spread); break;
                        case -1: randomBidShift = random50(spread); break;
                        case 1:
                            if (index > 1) {
                                randomBidShift = random50(spread).negate();
                            }else {
                                randomBidShift = randomMinus100(spread);
                            }
                            break;
                        case 2: randomBidShift = random80(spread).negate(); break;
                        case 3: randomBidShift = random100(spread).negate(); break;
                    }

                    BigDecimal randomBidSpread = spread.multiply(BigDecimal.valueOf(index-1)).add(randomBidShift);

                    if (randomBidSpread.compareTo(ZERO) == 0){
                        randomBidSpread = "`USD".equals(currencyPair.counterSymbol)
                                ? new BigDecimal("0.01")
                                : new BigDecimal("0.00000001");
                    }else {
                        if (trader.getExchangeType().equals(OKCOIN) && trader.getPair().contains("LTC/")){
                            randomBidSpread = randomBidSpread.setScale(3, HALF_UP);
                        }else {
                            randomBidSpread = "USD".equals(currencyPair.counterSymbol)
                                    ? randomBidSpread.setScale(2, HALF_UP)
                                    : randomBidSpread.setScale(8, HALF_UP);
                        }
                    }

                    //update middle price
                    ticker = dataService.getTicker(exchangePair);
                    middlePrice = ticker.getAsk().add(ticker.getBid()).divide(BigDecimal.valueOf(2), 8, HALF_UP);

                    if (trader.getExchangeType().equals(OKCOIN) && trader.getPair().contains("LTC/")){
                        middlePrice = middlePrice.setScale(3, HALF_UP);
                    }else if (trader.getPair().contains("/USD")){
                        middlePrice = middlePrice.setScale(2, HALF_UP);
                    }

                    //bid ask price
                    BigDecimal askPrice = middlePrice.add(randomAskSpread);
                    BigDecimal bidPrice =  middlePrice.subtract(randomBidSpread);
                    String avg = avgPosition.compareTo(ZERO) != 0 ? " : " + avgPosition.toString() : "";

                    //internal amount
                    internalAmount = internalAmount.add(askAmount).add(bidAmount);

                    if (trader.getType().equals(SHORT)){
                        //ASK
                        String id = tradeService.placeLimitOrder(new LimitOrder(ASK, askAmount, currencyPair, trader.getType().name(), new Date(), askPrice));
                        entityBean.save(new Order(id, exchangeType, exchangePair.getPair(), OrderType.ASK, askAmount, askPrice, new Date()));

                        //BID
                        id = tradeService.placeLimitOrder(new LimitOrder(BID, bidAmount, currencyPair, trader.getType().name(), new Date(), bidPrice));
                        entityBean.save(new Order(id, exchangeType, exchangePair.getPair(), OrderType.BID, bidAmount, bidPrice, new Date()));
                    }else{
                        //BID
                        if (bidPrice.compareTo(ZERO) < 0){
                            return;
                        }

                        String id = tradeService.placeLimitOrder(new LimitOrder(BID, bidAmount, currencyPair, trader.getType().name(), new Date(), bidPrice));
                        entityBean.save(new Order(id, exchangeType, exchangePair.getPair(), OrderType.BID, bidAmount, bidPrice, new Date()));

                        //ASK
                        id = tradeService.placeLimitOrder(new LimitOrder(ASK, askAmount, currencyPair, trader.getType().name(), new Date(), askPrice));
                        entityBean.save(new Order(id, exchangeType, exchangePair.getPair(), OrderType.ASK, askAmount, askPrice, new Date()));
                    }

                    //notification
                    broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": " +
                            bidAmount.toString() + " @ " + bidPrice.toString() + " | " +
                            askAmount.toString() + " @ " + askPrice.toString() + avg);
                }
            }
        } catch (Exception e) {
            log.error("alpha trade error", e);

            errorMap.put(exchangePair, ++errorCount);

            //noinspection ThrowableResultOfMethodCallIgnored
            broadcast(exchangeType, trader.getPair() + ": " + Throwables.getRootCause(e).getMessage());
        }
    }
}
