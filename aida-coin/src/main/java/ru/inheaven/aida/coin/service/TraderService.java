package ru.inheaven.aida.coin.service;

import com.google.common.base.Function;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.xeiam.xchange.cryptsy.CryptsyExchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.*;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinCrossPosition;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinCrossPositionResult;
import com.xeiam.xchange.okcoin.service.polling.OkCoinTradeServiceRaw;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.apache.wicket.util.collections.ConcurrentHashSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.*;
import ru.inheaven.aida.coin.util.TraderUtil;
import ru.inheaven.aida.predictor.service.PredictorService;

import javax.annotation.Nullable;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.xeiam.xchange.dto.Order.OrderType.ASK;
import static com.xeiam.xchange.dto.Order.OrderType.BID;
import static java.math.BigDecimal.*;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.coin.entity.ExchangeType.*;
import static ru.inheaven.aida.coin.entity.OrderStatus.*;
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
public class TraderService {
    private Logger log = LoggerFactory.getLogger(TraderService.class);

    @EJB
    private TraderBean traderBean;

    @EJB
    private PredictorService predictorService;

    @Resource
    private TimerService timerService;

    private Map<ExchangePair, Ticker> tickerMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, OrderBook> orderBookMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, BalanceHistory> balanceHistoryMap = new ConcurrentHashMap<>();

    private Map<ExchangePair, BigDecimal> predictionIndexMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, BigDecimal> volatilitySigmaMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, BigDecimal> averageMap = new ConcurrentHashMap<>();

    private Map<ExchangeType, OpenOrders> openOrdersMap = new ConcurrentHashMap<>();
    private Map<ExchangeType, AccountInfo> accountInfoMap = new ConcurrentHashMap<>();

    private Map<ExchangeType, Equity> equityMap = new ConcurrentHashMap<>();
    private Equity equity;

    private List<OrderStat> orderStatMap = new CopyOnWriteArrayList<>();

    private Map<ExchangePair,Integer> errorMap = new ConcurrentHashMap<>();
    private Map<ExchangePair,  Long> errorTimeMap = new ConcurrentHashMap<>();

    private Set<String> tradesHash = new ConcurrentHashSet<>(10000);

    private WebSocketPushBroadcaster broadcaster;

    @PreDestroy
    public void cancelTimers(){
        for (Timer timer : timerService.getAllTimers()){
            timer.cancel();
        }
    }

    @Schedule(second = "*/2", minute="*", hour="*", persistent=false)
    public void scheduleTradeFuture(){
        trade(OKCOIN);
    }

    @Schedule(second = "*/30", minute="*", hour="*", persistent=false)
    public void scheduleTrade(){
        trade(BITTREX);
        trade(CRYPTSY);
        trade(BTCE);
        trade(BTER);
        trade(CEXIO);
        trade(BITFINEX);
    }

    @Schedule(second = "0", minute="*", hour="*", persistent=false)
    public void scheduleOrders(){
        for(ExchangeType exchangeType : ExchangeType.values()){
            updateClosedOrders(exchangeType);
        }

        traderBean.getTraders().stream().filter(Trader::isRunning).forEach(trader -> {
            if (trader.isPredicting()) {
                updatePredictionIndex(trader.getExchangePair());
            }

            updateVolatility(trader.getExchangePair());

            updateAverage(trader.getExchangePair());
        });

        scheduleBalanceHistory();
    }

    @Schedule(second = "*/5", minute="*", hour="*", persistent=false)
    public void scheduleFuturePosition(){
        try {
            //balance
            //balanceOKCoinWeekPosition("BTC/USD");
            balanceOKCoinWeekPosition("LTC/USD");

            int levels = 200;
            int balancing = 11;

            double spread;
            try {
                spread = getSpread(ExchangePair.of(OKCOIN, "LTC/USD")).doubleValue();
            } catch (Exception e) {
                return;
            }

            OkCoinCrossPositionResult week = ((OkCoinTradeServiceRaw)getExchange(OKCOIN).getPollingTradeService()).getCrossPosition("ltc_usd", "this_week");

            OkCoinCrossPositionResult quarter = ((OkCoinTradeServiceRaw)getExchange(OKCOIN).getPollingTradeService()).getCrossPosition("ltc_usd", "quarter");

            double last = getTicker(ExchangePair.of(OKCOIN, "LTC/USD")).getLast().doubleValue();
            double delta = spread / last;

            Futures futures = new Futures();

            OkCoinCrossPosition p = week.getPositions()[0];
            OkCoinCrossPosition q = quarter.getPositions()[0];

            //long

            double buyProfit =  p.getBuyProfitReal().doubleValue() + q.getBuyProfitReal().doubleValue();
            double sellProfit =  p.getSellProfitReal().doubleValue() + q.getSellProfitReal().doubleValue();

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

            buyProfit =  p.getBuyProfitReal().doubleValue() + q.getBuyProfitReal().doubleValue();
            sellProfit =  p.getSellProfitReal().doubleValue() + q.getSellProfitReal().doubleValue();

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
                Ticker ticker = getTicker(ExchangePair.of(OKCOIN, pair));

                boolean _short = tw.getSellAmount().intValue() < minAmount;

                BigDecimal sumAmount = tw.getSellAmount().add(tw.getBuyAmount());

                BigDecimal a1 = sumAmount.multiply(BigDecimal.valueOf(0.22));

                if ((tw.getBuyAmount().intValue() < minAmount && tw.getSellAmount().intValue() > 2*minAmount)
                        || (tw.getSellAmount().intValue() < minAmount && tw.getBuyAmount().intValue() > 2*minAmount)){

                    //balance
                    BigDecimal price = _short
                            ? ticker.getBid().multiply(BigDecimal.valueOf(0.99))
                            : ticker.getAsk().multiply(BigDecimal.valueOf(1.01));

                    Order.OrderType orderType = _short ? ASK : BID;

                    String id = tradeService.placeLimitOrder(new LimitOrder(orderType, a1, getCurrencyPair(pair),
                            _short ? "LONG" : "SHORT", new Date(), price));
                    traderBean.save(new OrderHistory(id, OKCOIN, pair, orderType, a1, price, new Date()));

                    id = tradeService.placeLimitOrder(new LimitOrder(orderType, a1, getCurrencyPair(pair),
                            _short ? "SHORT" : "LONG", new Date(), price));
                    traderBean.save(new OrderHistory(id, OKCOIN, pair, orderType, a1, price, new Date()));
                }
            }
        } catch (Exception e) {
            log.error("balanceOKCoinWeekPosition error", e);

            broadcast(OKCOIN, e);
        }
    }

    public void trade(ExchangeType exchangeType){
        try {
            updateAccountInfo(exchangeType);
            updateOpenOrders(exchangeType);
            updateTicker(exchangeType);

            tradeAlpha(exchangeType);

            updateClosedOrders(exchangeType);
            updateEquity(exchangeType);
            updateEquity();
        } catch (Exception e) {
            log.error("Schedule trade error", e);

            //noinspection ThrowableResultOfMethodCallIgnored
            broadcast(exchangeType, exchangeType.name() + ": " + Throwables.getRootCause(e).getMessage());
        }
    }

    public void scheduleBalanceHistory(){
        try {
            for (ExchangeType exchangeType : ExchangeType.values()){
                AccountInfo accountInfo = getAccountInfo(exchangeType);
                OpenOrders openOrders = getOpenOrders(exchangeType);

                if (accountInfo != null && openOrders != null){
                    //check ask amount
                    boolean zero = true;

                    for (LimitOrder limitOrder : openOrders.getOpenOrders()){
                        if (limitOrder.getType().equals(ASK)
                                && limitOrder.getLimitPrice().compareTo(BigDecimal.ZERO) != 0){
                            zero = false;
                            break;
                        }
                    }

                    if (zero){
                        continue;
                    }

                    List<Trader> traders = traderBean.getTraders(exchangeType);

                    for (Trader trader : traders){
                        Ticker ticker = getTicker(trader.getExchangePair());

                        if (ticker != null) {
                            CurrencyPair currencyPair = TraderUtil.getCurrencyPair(trader.getPair());

                            BigDecimal askAmount = ZERO;
                            BigDecimal bidAmount =  ZERO;

                            for (LimitOrder limitOrder : openOrders.getOpenOrders()){
                                if (currencyPair.equals(limitOrder.getCurrencyPair())){
                                    if (limitOrder.getType().equals(ASK)){
                                        askAmount = askAmount.add(limitOrder.getTradableAmount());
                                    }else{
                                        bidAmount = bidAmount.add(limitOrder.getTradableAmount());
                                    }
                                }
                            }

                            ExchangePair exchangePair = trader.getExchangePair();
                            BalanceHistory previous = balanceHistoryMap.get(exchangePair);

                            BalanceHistory h = new BalanceHistory();

                            h.setExchangeType(exchangeType);
                            h.setPair(trader.getPair());
                            h.setBalance(accountInfo.getBalance(trader.getCurrency()));
                            h.setAskAmount(askAmount);
                            h.setBidAmount(bidAmount);
                            h.setPrice(ticker.getLast());
                            h.setPrevious(previous);

                            if (previous != null &&  h.getPrice() != null){
                                boolean changed;

                                if (OKCOIN.equals(trader.getExchange())){
                                    double p1 = previous.getBalance().doubleValue();
                                    double p2 = h.getBalance().doubleValue();

                                    changed = Math.abs(p1 - p2) / p1 > 0.005;
                                }else{
                                    changed = !h.equals(previous);
                                }

                                if (changed) {
                                    try {
                                        traderBean.save(h);
                                    } catch (Exception e) {
                                        log.error("save balance history error", e);
                                    }
                                }

                                broadcast(exchangeType, h);
                            }

                            balanceHistoryMap.put(exchangePair, h);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("schedule balance history error", e);
        }
    }

    private void updateAccountInfo(ExchangeType exchangeType) throws IOException {
        AccountInfo accountInfo = getExchange(exchangeType).getPollingAccountService().getAccountInfo();
        accountInfoMap.put(exchangeType, accountInfo);

        broadcast(exchangeType, accountInfo);
    }

    private void updateOpenOrders(ExchangeType exchangeType) throws IOException {
        OpenOrders openOrders = getExchange(exchangeType).getPollingTradeService().getOpenOrders();
        openOrdersMap.put(exchangeType, openOrders);

        broadcast(exchangeType, openOrders);
    }

    private void updateTicker(ExchangeType exchangeType) throws Exception {
        List<Trader> traders = traderBean.getTraders(exchangeType);

        for (Trader trader : traders) {
            try {
                CurrencyPair currencyPair = getCurrencyPair(trader.getPair());

                if (currencyPair != null && trader.getType().equals(LONG)) {
                    Ticker ticker;

                    if (CRYPTSY.equals(exchangeType)) {
                        ticker = ((CryptsyExchange)getExchange(exchangeType)).getPublicPollingMarketDataService().getTicker(currencyPair);
                    }else{
                        ticker = getExchange(exchangeType).getPollingMarketDataService().getTicker(currencyPair);
                    }

                    if (ticker.getLast() != null && ticker.getLast().compareTo(ZERO) != 0 && ticker.getBid() != null && ticker.getAsk() != null) {
                        ExchangePair ep = new ExchangePair(exchangeType, trader.getPair(), trader.getType());

                        Ticker previous = tickerMap.put(ep, ticker);

                        //ticker history
                        TickerHistory tickerHistory = new TickerHistory(exchangeType, trader.getPair(), ticker.getLast(),
                                ticker.getBid(), ticker.getAsk(), ticker.getVolume(),
                                getVolatilityIndex(ep), getPredictionIndex(ep));

                        if (previous == null || previous.getLast().compareTo(ticker.getLast()) != 0){
                            traderBean.save(tickerHistory);
                        }

                        if (previous == null || previous.getAsk().compareTo(ticker.getAsk()) != 0
                                || previous.getBid().compareTo(ticker.getBid()) != 0){
                            broadcast(exchangeType, tickerHistory);
                        }
                    }
                }
            } catch (Exception e) {
                log.error("Error update ticker {} {}", exchangeType, trader.getPair());

                throw e;
            }
        }
    }

    private void updateOrderBook(ExchangeType exchangeType) throws IOException {
        List<String> pairs = traderBean.getTraderPairs(exchangeType);

        for (String pair : pairs) {
            CurrencyPair currencyPair = getCurrencyPair(pair);

            if (currencyPair != null) {
                try {
                    OrderBook orderBook = getExchange(exchangeType).getPollingMarketDataService().getOrderBook(currencyPair);

                    if (orderBook != null && !orderBook.getBids().isEmpty()) {
                        orderBook.getBids().sort(new Comparator<LimitOrder>() {
                            @Override
                            public int compare(LimitOrder o1, LimitOrder o2) {
                                return o1.getLimitPrice().compareTo(o2.getLimitPrice());
                            }
                        });

                        orderBook.getAsks().sort(new Comparator<LimitOrder>() {
                            @Override
                            public int compare(LimitOrder o1, LimitOrder o2) {
                                return o1.getLimitPrice().compareTo(o2.getLimitPrice());
                            }
                        });

                        orderBookMap.put(new ExchangePair(exchangeType, pair), orderBook);

                        broadcast(exchangeType, orderBook);
                    }
                } catch (Exception e) {
                    log.error("updateOrderBook error", e);

                    //noinspection ThrowableResultOfMethodCallIgnored
                    broadcast(exchangeType, exchangeType.name() + ": " + Throwables.getRootCause(e).getMessage());
                }
            }
        }
    }

    public void updateTrades(ExchangeType exchangeType){
        try {
            UserTrades userTrades = getExchange(exchangeType).getPollingTradeService().getTradeHistory();

            for (UserTrade t : userTrades.getUserTrades()){
                String key = exchangeType.name() + t.getCurrencyPair().toString() + t.getId() + t.getOrderId();

                if (!tradesHash.contains(key)){
                    TradeHistory tradeHistory = new TradeHistory(exchangeType, TraderUtil.getPair(t.getCurrencyPair()),
                            t.getType(), t.getTradableAmount(), t.getPrice(), t.getTimestamp(), t.getId(), t.getOrderId(),
                            t.getFeeAmount(), t.getFeeCurrency());

                    try {
                        traderBean.save(tradeHistory);
                    } catch (Exception e) {
                        log.error("updateTrades error save db", e);
                    }

                    broadcast(exchangeType, tradeHistory);
                }
            }

        } catch (Exception e) {
            log.error("updateTrades error", e);

            //noinspection ThrowableResultOfMethodCallIgnored
            broadcast(exchangeType, exchangeType.name() + ": " + Throwables.getRootCause(e).getMessage());
        }
    }

    public void updateClosedOrders(ExchangeType exchangeType){
        OpenOrders openOrders = getOpenOrders(exchangeType);

        for (OrderHistory h : traderBean.getOrderHistories(exchangeType, OPENED)) {
            if (openOrders == null || System.currentTimeMillis() - h.getOpened().getTime() < 60000){
                continue;
            }

            try {
                boolean found = false;

                for (LimitOrder o : openOrders.getOpenOrders()){
                    if (o.getId().split("&")[0].equals(h.getOrderId())){
                        found = true;
                        break;
                    }
                }

                if (!found){
                    h.setStatus(CLOSED);
                    h.setFilledAmount(h.getTradableAmount());
                    h.setClosed(new Date());

                    traderBean.save(h);
                    broadcast(exchangeType, h);
                }
            } catch (Exception e) {
                log.error("updateClosedOrders error", e);

                //noinspection ThrowableResultOfMethodCallIgnored
                broadcast(exchangeType, exchangeType.name() + ": " + Throwables.getRootCause(e).getMessage());
            }
        }
    }

    public void updateEquity(ExchangeType exchangeType){
        AccountInfo accountInfo = getAccountInfo(exchangeType);

        if (accountInfo != null){
            BigDecimal volume = ZERO;

            for (Wallet wallet : accountInfo.getWallets()){
                volume = volume.add(getEstimateBalance(exchangeType, wallet.getCurrency(), wallet.getBalance()));
            }

            if (BTCE.equals(exchangeType)){ //do check it
                for (LimitOrder limitOrder : getOpenOrders(ExchangeType.BTCE).getOpenOrders()){
                    volume = volume.add(getBTCVolume(
                            ExchangePair.of(ExchangeType.BTCE, TraderUtil.getPair(limitOrder.getCurrencyPair())),
                            limitOrder.getTradableAmount(), limitOrder.getLimitPrice()));
                }
            }

            Equity equity = equityMap.get(exchangeType);

            if (equity == null || equity.getVolume().compareTo(volume) != 0){
                equity = new Equity(exchangeType, volume);

                equityMap.put(exchangeType, equity);

                traderBean.save(equity);

                broadcast(exchangeType, equity);
            }
        }
    }

    public void updateEquity(){
        BigDecimal volume = ZERO;

        for (ExchangeType exchangeType : ExchangeType.values()){
            Equity e = equityMap.get(exchangeType);

            if(e == null){
                return;
            }

            volume = volume.add(e.getVolume());
        }

        equity = new Equity(volume);

        traderBean.save(equity);

        broadcast(null, equity);
    }

    public BigDecimal getSpread(ExchangePair exchangePair){
        BigDecimal spread;

        Ticker ticker = getTicker(exchangePair);
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
        BigDecimal volatility = volatilitySigmaMap.get(exchangePair) != null
                ? volatilitySigmaMap.get(exchangePair).divide(ticker.getLast(), 8, HALF_UP)
                : ZERO;

        if (volatility.compareTo(BigDecimal.valueOf(0.05)) > 0) {
            spread = spread.multiply(ONE.add(volatility.multiply(BigDecimal.TEN)).pow(2)).setScale(8, HALF_UP);
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

        //volatility
        BigDecimal volatility = volatilitySigmaMap.get(trader.getExchangePair()) != null
                ? volatilitySigmaMap.get(trader.getExchangePair()).divide(ticker.getLast(), 8, HALF_UP)
                : ZERO;

        minOrderAmount = minOrderAmount.multiply(ONE.add(volatility.multiply(BigDecimal.valueOf(2*Math.PI)))).setScale(8, HALF_UP);

        return minOrderAmount;
    }

    private void tradeAlpha(ExchangeType exchangeType) throws IOException {
        List<Trader> traders = traderBean.getTraders(exchangeType);

        Collections.shuffle(traders);

        for (Trader trader : traders) {
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

                    continue;
                }

                if (trader.isRunning()) {
                    Ticker ticker = getTicker(exchangePair);

                    if (ticker == null || ticker.getLast() == null) {
                        continue;
                    }

                    PollingTradeService tradeService = getExchange(exchangeType).getPollingTradeService();
                    AccountInfo accountInfo = getAccountInfo(exchangeType);

                    //middle price
                    BigDecimal middlePrice = ticker.getAsk().add(ticker.getBid()).divide(BigDecimal.valueOf(2), 8, HALF_UP);

                    if (middlePrice.compareTo(trader.getHigh()) > 0) {
                        errorMap.put(exchangePair, ++errorCount);

                        broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": " + middlePrice.toString()
                                + " > " + trader.getHigh().toString());
                        continue;
                    }

                    if (middlePrice.compareTo(trader.getLow()) < 0) {
                        errorMap.put(exchangePair, ++errorCount);

                        broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": " + middlePrice.toString()
                                + " < " + trader.getLow().toString());

                        continue;
                    }

                    //min spread
                    BigDecimal spread = getSpread(exchangePair);

                    //min order amount
                    BigDecimal orderAmount = getOrderAmount(trader, ticker);

                    //open orders
                    List<LimitOrder> openOrders = getOpenOrders(exchangeType).getOpenOrders();

                    //cancel orders
                    for (LimitOrder order : openOrders) {
                        if ((trader.getType().equals(LONG) && (order.getId().contains("&2") || order.getId().contains("&4")))
                                || ((trader.getType().equals(SHORT) && (order.getId().contains("&1") || order.getId().contains("&3"))))){
                            continue;
                        }

                        if (currencyPair.equals(order.getCurrencyPair())
                                && order.getLimitPrice().subtract(middlePrice).abs()
                                .compareTo(spread.multiply(BigDecimal.valueOf(22))) > 0) {
                            String orderId = order.getId().split("&")[0];

                            //update order status
                            OrderHistory h = traderBean.getOrderHistory(orderId);

                            if (h != null){
                                h.setStatus(CANCELED);
                                h.setClosed(new Date());

                                traderBean.save(h);

                                tradeService.cancelOrder(orderId);

                                broadcast(exchangeType, h);
                            }
                        }
                    }

                    //prediction
                    BigDecimal predictionIndex = getPredictionIndex(exchangePair);

                    //average
                    BigDecimal average = getAverage(exchangePair);

                    //internal amount
                    BigDecimal internalAmount = ZERO;

                    //avg position
                    BigDecimal avgPosition = ZERO;

                    if (OKCOIN.equals(trader.getExchange())){
                        OkCoinCrossPositionResult positions = ((OkCoinTradeServiceRaw)getExchange(OKCOIN)
                                .getPollingTradeService()).getCrossPosition("ltc_usd", "this_week");

                        OkCoinCrossPosition p = positions.getPositions()[0];
                        avgPosition = middlePrice.add(p.getBuyAmount().subtract(p.getSellAmount()).multiply(spread));
                    }

                    //create order
                    for (int index = 1; index <= 11; ++index) {
                        //btc-e spread
                        if (BTCE.equals(trader.getExchange())){
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

                        //random ask delta
                        BigDecimal randomAskSpread = spread;
                        if (OKCOIN.equals(trader.getExchange()) && index > 1){
                            randomAskSpread = trader.getType().equals(LONG)
                                    ? random50(randomAskSpread)
                                    : randomMinus50(randomAskSpread);
                        }
                        if (predictionIndex.compareTo(ZERO) != 0) {
                            randomAskSpread = predictionIndex.compareTo(ZERO) > 0
                                    ? random50(randomAskSpread)
                                    : randomMinus50(randomAskSpread);
                        }
                        if (average.compareTo(ZERO) != 0 && avgPosition.compareTo(ZERO) != 0 && index > 1){
                            randomAskSpread = average.compareTo(avgPosition) > 0
                                    ? random50(randomAskSpread)
                                    : randomMinus50(randomAskSpread);
                        }

                        randomAskSpread = randomAskSpread.add(spread.multiply(BigDecimal.valueOf(index-1)));

                        if (randomAskSpread.compareTo(ZERO) == 0){
                            randomAskSpread = "USD".equals(currencyPair.counterSymbol)
                                    ? new BigDecimal("0.01")
                                    : new BigDecimal("0.00000001");
                        }else {
                            if (trader.getExchange().equals(OKCOIN) && trader.getPair().contains("LTC/")){
                                randomAskSpread = randomAskSpread.setScale(3, HALF_UP);
                            }else {
                                randomAskSpread = "USD".equals(currencyPair.counterSymbol)
                                        ? randomAskSpread.setScale(2, HALF_UP)
                                        : randomAskSpread.setScale(8, HALF_UP);
                            }
                        }

                        //random bid delta
                        BigDecimal randomBidSpread = spread;

                        if (OKCOIN.equals(trader.getExchange()) && index > 1){
                            randomBidSpread = trader.getType().equals(LONG)
                                    ? randomMinus50(randomBidSpread)
                                    : random50(randomBidSpread);
                        }
                        if (predictionIndex.compareTo(ZERO) != 0) {
                            randomBidSpread = predictionIndex.compareTo(ZERO) > 0
                                    ? randomMinus50(randomBidSpread)
                                    : random50(randomBidSpread);
                        }
                        if (average.compareTo(ZERO) != 0 && avgPosition.compareTo(ZERO) != 0 && index > 1){
                            randomBidSpread = average.compareTo(avgPosition) > 0
                                    ? randomMinus50(randomBidSpread)
                                    : random50(randomBidSpread);
                        }

                        randomBidSpread = randomBidSpread.add(spread.multiply(BigDecimal.valueOf(index-1)));

                        if (randomBidSpread.compareTo(ZERO) == 0){
                            randomBidSpread = "`USD".equals(currencyPair.counterSymbol)
                                    ? new BigDecimal("0.01")
                                    : new BigDecimal("0.00000001");
                        }else {
                            if (trader.getExchange().equals(OKCOIN) && trader.getPair().contains("LTC/")){
                                randomBidSpread = randomBidSpread.setScale(3, HALF_UP);
                            }else {
                                randomBidSpread = "USD".equals(currencyPair.counterSymbol)
                                        ? randomBidSpread.setScale(2, HALF_UP)
                                        : randomBidSpread.setScale(8, HALF_UP);
                            }
                        }

                        //update middle price
                        ticker = getTicker(exchangePair);
                        middlePrice = ticker.getAsk().add(ticker.getBid()).divide(BigDecimal.valueOf(2), 8, HALF_UP);

                        if (trader.getExchange().equals(OKCOIN) && trader.getPair().contains("LTC/")){
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
                            traderBean.save(new OrderHistory(id, exchangeType, exchangePair.getPair(), ASK, askAmount, askPrice, new Date()));

                            //BID
                            id = tradeService.placeLimitOrder(new LimitOrder(BID, bidAmount, currencyPair, trader.getType().name(), new Date(), bidPrice));
                            traderBean.save(new OrderHistory(id, exchangeType, exchangePair.getPair(), BID, bidAmount, bidPrice, new Date()));
                        }else{
                            //BID
                            if (bidPrice.compareTo(ZERO) < 0){
                                return;
                            }

                            String id = tradeService.placeLimitOrder(new LimitOrder(BID, bidAmount, currencyPair, trader.getType().name(), new Date(), bidPrice));
                            traderBean.save(new OrderHistory(id, exchangeType, exchangePair.getPair(), BID, bidAmount, bidPrice, new Date()));

                            //ASK
                            id = tradeService.placeLimitOrder(new LimitOrder(ASK, askAmount, currencyPair, trader.getType().name(), new Date(), askPrice));
                            traderBean.save(new OrderHistory(id, exchangeType, exchangePair.getPair(), ASK, askAmount, askPrice, new Date()));
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

    @Asynchronous
    private void broadcast(ExchangeType exchange, Object payload){
        try {
            if (payload != null) {
                Application application = Application.get("aida-coin");

                if (broadcaster == null){
                    IWebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
                    broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
                }

                broadcaster.broadcastAll(application, new ExchangeMessage<>(exchange, payload));
            }
        } catch (Exception e) {
            log.error("broadcast error", e);
        }
    }

    public AccountInfo getAccountInfo(ExchangeType exchangeType){
        return accountInfoMap.get(exchangeType);
    }

    public Ticker getTicker(ExchangePair exchangePair){
        return tickerMap.get(exchangePair);
    }

    public Ticker getTickerNotNull(ExchangePair exchangePair){
        Ticker ticker = tickerMap.get(exchangePair);

        if (ticker == null){
            ticker = new Ticker.Builder().currencyPair(TraderUtil.getCurrencyPair(exchangePair.getPair()))
                    .last(ZERO).bid(ZERO).ask(ZERO).high(ZERO).low(ZERO).volume(ZERO).timestamp(new Date()).build();
        }

        return ticker;
    }

    public OrderBook getOrderBook(ExchangePair exchangePair){
        return orderBookMap.get(exchangePair);
    }

    public OpenOrders getOpenOrders(ExchangeType exchangeType){
        return openOrdersMap.get(exchangeType);
    }

    public BalanceHistory getBalanceHistory(ExchangePair exchangePair){ return balanceHistoryMap.get(exchangePair); }

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

        List<OrderHistory> orders = exchangePair != null
                ? traderBean.getOrderHistories(exchangePair, CLOSED, startDate)
                : traderBean.getOrderHistories(CLOSED, startDate);

        for (OrderHistory order : orders){
            volumes.add(new Volume(getBTCVolume(ExchangePair.of(order.getExchangeType(), order.getPair()),
                    order.getTradableAmount(), order.getPrice()).multiply(BigDecimal.valueOf(ASK.equals(order.getType()) ? 1 : -1)),
                    order.getClosed()));
        }

        volumes.sort(new Comparator<Volume>() {
            @Override
            public int compare(Volume o1, Volume o2) {
                return o1.getDate().compareTo(o2.getDate());
            }
        });

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

    public BigDecimal getBTCVolume(ExchangePair ep, BigDecimal amount, BigDecimal price){
        try {

            if (OKCOIN.equals(ep.getExchangeType())){
                if ("BTC".equals(ep.getCurrency())) {
                    amount =  amount.multiply(BigDecimal.valueOf(10)).divide(getTicker(ExchangePair.of(OKCOIN, "BTC/USD")).getLast(), 8 , HALF_UP);
                }else if ("LTC".equals(ep.getCurrency())){
                    amount = amount.multiply(BigDecimal.valueOf(1)).divide(getTicker(ExchangePair.of(OKCOIN, "LTC/USD")).getLast(), 8, HALF_UP);
                }
            }

            BigDecimal volume = amount.multiply(price);

            String pair = ep.getPair();

            if (pair.contains("/BTC")) {
                return volume.setScale(8, HALF_UP);
            } else if (pair.contains("/LTC")) {
                return volume.multiply(getTicker(ExchangePair.of(CEXIO, "LTC/BTC")).getLast()).setScale(8, HALF_UP);
            } else if (pair.contains("/BC")) {
                return volume.multiply(getTicker(ExchangePair.of(BITTREX, "BC/BTC")).getLast()).setScale(8, HALF_UP);
            } else if (pair.contains("/USD")) {
                return volume.divide(getTicker(ExchangePair.of(BTCE, "BTC/USD")).getLast(), 8, HALF_UP);
            } else if (pair.contains("/CNY")) {
                return volume.divide(getTicker(ExchangePair.of(BTER, "BTC/CNY")).getLast(), 8, HALF_UP);
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
                    return balance.divide(getTicker(ExchangePair.of(BTCE, "BTC/USD")).getLast(), 8, HALF_UP);
                case "CNY":
                    return balance.divide(getTicker(ExchangePair.of(BTER, "BTC/CNY")).getLast(), 8, HALF_UP);
                default:
                    Ticker ticker = getTicker(ExchangePair.of(exchangeType, currency + "/BTC"));

                    if (ticker == null){
                        ticker = getTicker(ExchangePair.of(BITFINEX, currency + "/BTC"));
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

    public void updateVolatility(ExchangePair exchangePair){
        volatilitySigmaMap.put(exchangePair, traderBean.getSigma(exchangePair));
    }

    public BigDecimal getVolatilityIndex(ExchangePair exchangePair){
        try {
            return volatilitySigmaMap.get(exchangePair).multiply(BigDecimal.valueOf(100))
                    .divide(tickerMap.get(exchangePair).getLast(), 2, HALF_UP);
        } catch (Exception e) {
            return ZERO;
        }
    }

    public void updateAverage(ExchangePair exchangePair){
        averageMap.put(exchangePair, traderBean.getAverage(exchangePair));
    }

    public BigDecimal getAverage(ExchangePair exchangePair){
        BigDecimal average = averageMap.get(exchangePair);

        return average != null ? average : ZERO;
    }

    public BigDecimal getOrderStatProfit(ExchangePair exchangePair, Date startDate){
        List<OrderStat> orderStats = traderBean.getOrderStats(exchangePair, startDate);

        if (orderStats.size() < 2){
            return ZERO;
        }


        Map<Order.OrderType, OrderStat> map = Maps.uniqueIndex(orderStats, new Function<OrderStat, Order.OrderType>() {
            @Nullable
            @Override
            public Order.OrderType apply(@Nullable OrderStat input) {
                return input != null ? input.getType() : null;
            }
        });

        if (exchangePair.getExchangeType().equals(OKCOIN)){
            int contract = exchangePair.getPair().contains("BTC/") ? 100 :10;

            return BigDecimal.valueOf((contract/map.get(BID).getAvgPrice().doubleValue() - contract/map.get(ASK).getAvgPrice().doubleValue())
                    * (map.get(ASK).getSumAmount().add(map.get(BID).getSumAmount()).intValue())).setScale(8, HALF_UP);
        }

        BigDecimal priceDiff = ZERO;
        BigDecimal minAmount = ZERO;

        for (OrderStat orderStat : orderStats){
            priceDiff = orderStat.getType().equals(ASK)
                    ? priceDiff.add(orderStat.getAvgPrice())
                    : priceDiff.subtract(orderStat.getAvgPrice());

            if (minAmount.equals(ZERO) || minAmount.compareTo(orderStat.getSumAmount()) > 0){
                minAmount = orderStat.getSumAmount();
            }
        }

        return getBTCVolume(exchangePair, minAmount, priceDiff);
    }

}
