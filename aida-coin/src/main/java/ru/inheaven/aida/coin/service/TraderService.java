package ru.inheaven.aida.coin.service;

import com.google.common.base.Throwables;
import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.bitfinex.v1.BitfinexExchange;
import com.xeiam.xchange.bittrex.v1.BittrexExchange;
import com.xeiam.xchange.btce.v3.BTCEExchange;
import com.xeiam.xchange.bter.BTERExchange;
import com.xeiam.xchange.cexio.CexIOExchange;
import com.xeiam.xchange.cryptsy.CryptsyExchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.service.polling.PollingTradeService;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.*;
import ru.inheaven.aida.coin.util.TraderUtil;

import javax.ejb.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static com.xeiam.xchange.ExchangeFactory.INSTANCE;
import static java.math.BigDecimal.ONE;
import static java.math.BigDecimal.ROUND_UP;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.coin.entity.ExchangeType.*;
import static ru.inheaven.aida.coin.util.TraderUtil.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 17:06
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@TransactionManagement(TransactionManagementType.BEAN)
public class TraderService {
    private Logger log = LoggerFactory.getLogger(TraderService.class);

    @EJB
    private TraderBean traderBean;

    private Map<ExchangePair, Ticker> tickerMap = new ConcurrentHashMap<>();
    private Map<ExchangePair, OrderBook> orderBookMap = new ConcurrentHashMap<>();
    private Map<ExchangeType, OpenOrders> openOrdersMap = new ConcurrentHashMap<>();
    private Map<ExchangeType, AccountInfo> accountInfoMap = new ConcurrentHashMap<>();

    private Map<ExchangePair, BalanceHistory> balanceHistoryMap = new ConcurrentHashMap<>();

    private Map<ExchangePair,Integer> errorMap = new ConcurrentHashMap<>();
    private Map<ExchangePair,  Long> errorTimeMap = new ConcurrentHashMap<>();

    private Exchange bittrexExchange = INSTANCE.createExchange(new ExchangeSpecification(BittrexExchange.class){{
        setApiKey("14935ef36d8b4afc8204946be7ddd152");
        setSecretKey("44d84de3865e4fbfa4c17dd42c026d11");
    }});

    private Exchange cexIOExchange = INSTANCE.createExchange(new ExchangeSpecification(CexIOExchange.class){{
        setUserName("inheaven");
        setApiKey("0rt9tOzQG2rGfZfGxsx1CtR9JA");
        setSecretKey("5ZpuaGOfpFdn96JisyCfR6wQvc");
    }});

    private Exchange cryptsyExchange = INSTANCE.createExchange(new ExchangeSpecification(CryptsyExchange.class){{
        setApiKey("d5569da15ae4d9b58c11220e424747289df76627");
        setSecretKey("f3a4732d55f7b9162ea6105187b36f9b1a88323c7f5bc8d1fcb2b8c98d3456384c31d1cf3ae443a6");
    }});

    private Exchange btceExchange = INSTANCE.createExchange(new ExchangeSpecification(BTCEExchange.class){{
        setApiKey("IR3KMDK9-JPP06NXH-GKGO2GPA-EC4BK5W0-L9QG482O");
        setSecretKey("05e3dbb59c2586df33c12e189382e18cb5de5af736a9a0897b6b23a1bca359b6");
    }});

    private Exchange bterExchange = INSTANCE.createExchange(new ExchangeSpecification(BTERExchange.class){{
        setApiKey("2DD5DEB3-720C-404C-95FE-84B52369F6E3");
        setSecretKey("0bf365f96b17f1828736df787c872796be51fe70a588062cc9630c3eedc144ad");
    }});

    private Exchange bitfinexExchange = INSTANCE.createExchange(new ExchangeSpecification(BitfinexExchange.class){{
        setApiKey("mn6dQmAnpKPp3GZyN6Plxhmt5WdJwVVj6zFdIel6fRZ");
        setSecretKey("B8UxOTb6cdKwz7jDu1m1FMjFCxMiz82g21z78Z8tDeB");
    }});

    public Exchange getExchange(ExchangeType exchangeType){
        switch (exchangeType){
            case BITTREX:
                return bittrexExchange;
            case CEXIO:
                return cexIOExchange;
            case CRYPTSY:
                return cryptsyExchange;
            case BTCE:
                return btceExchange;
            case BTER:
                return bterExchange;
            case BITFINEX:
                return bitfinexExchange;
        }

        throw new IllegalArgumentException();
    }

    @Schedule(second = "*/3", minute="*", hour="*", persistent=false)
    public void scheduleBittrexUpdate(){
        scheduleUpdate(BITTREX);
    }

    @Schedule(second = "*/30", minute="*", hour="*", persistent=false)
    public void scheduleCexIOUpdate(){
        scheduleUpdate(CEXIO);
    }

    @Schedule(second = "*/5", minute="*", hour="*", persistent=false)
    public void scheduleCryptsyUpdate(){
        scheduleUpdate(CRYPTSY);
    }

    @Schedule(second = "*/5", minute="*", hour="*", persistent=false)
    public void scheduleBTCEUpdate(){
        scheduleUpdate(BTCE);
    }

    @Schedule(second = "*/3", minute="*", hour="*", persistent=false)
    public void scheduleBTERUpdate(){
        scheduleUpdate(BTER);
    }

    @Schedule(second = "*/3", minute="*", hour="*", persistent=false)
    public void scheduleBITFINEXUpdate(){
        scheduleUpdate(BITFINEX);
    }

    @Schedule(second = "*/1", minute="*", hour="*", persistent=false)
    public void scheduleBalanceHistory() throws Exception{
        try {
            for (ExchangeType exchangeType : ExchangeType.values()){
                AccountInfo accountInfo = getAccountInfo(exchangeType);
                OpenOrders openOrders = getOpenOrders(exchangeType);

                if (accountInfo != null && openOrders != null){
                    //check ask amount
                    boolean zero = true;

                    for (LimitOrder limitOrder : openOrders.getOpenOrders()){
                        if (limitOrder.getType().equals(Order.OrderType.ASK)
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
                                    if (limitOrder.getType().equals(Order.OrderType.ASK)){
                                        askAmount = askAmount.add(limitOrder.getTradableAmount());
                                    }else{
                                        bidAmount = bidAmount.add(limitOrder.getTradableAmount());
                                    }
                                }
                            }

                            ExchangePair exchangePair = trader.getExchangePair();
                            BalanceHistory previous = balanceHistoryMap.get(exchangePair);

                            BalanceHistory balanceHistory = new BalanceHistory();
                            balanceHistory.setExchangeType(exchangeType);
                            balanceHistory.setPair(trader.getPair());
                            balanceHistory.setBalance(accountInfo.getBalance(trader.getCurrency()));
                            balanceHistory.setAskAmount(askAmount);
                            balanceHistory.setBidAmount(bidAmount);
                            balanceHistory.setPrice(ticker.getLast());
                            balanceHistory.setPrevious(previous);

                            if (previous != null && !balanceHistory.equals(previous) && balanceHistory.getPrice() != null){
                                try {
                                    traderBean.save(balanceHistory);
                                } catch (Exception e) {
                                    log.error("save balance history error", e);
                                }

                                broadcast(exchangeType, balanceHistory);
                            }

                            balanceHistoryMap.put(exchangePair, balanceHistory);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("schedule balance history error", e);
        }
    }

    public List<OrderVolume> getOrderVolumeRates(Date startDate){
        List<Volume> volumes = getVolumes(startDate);

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
        List<Volume> volumes = getVolumes(startDate);

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
        List<Volume> volumes = new ArrayList<>();

        Map<ExchangePair, BalanceHistory> previousMap = new HashMap<>();

        List<BalanceHistory> balanceHistories = traderBean.getBalanceHistories(startDate);

        for (BalanceHistory history : balanceHistories){
            ExchangePair exchangePair = ExchangePair.of(history.getExchangeType(), history.getPair());

            BalanceHistory previous = previousMap.get(exchangePair);

            if (previous != null && previous.getBalance().compareTo(history.getBalance()) != 0) {
                history.setPrevious(previous);
                volumes.add(getVolume(history));
            }

            previousMap.put(exchangePair, history);
        }

        return volumes;
    }

    public Volume getVolume(BalanceHistory history){
        return new Volume(getBTCVolume(history.getPair(), history.getPrevious().getBalance()
                .add(history.getPrevious().getAskAmount())
                .subtract(history.getBalance().add(history.getAskAmount()))
                , (history.getPrice().subtract(history.getPrevious().getPrice()))), history.getDate());
    }

    public BigDecimal getBTCVolume(String pair, BigDecimal amount, BigDecimal price){
        BigDecimal volume = amount.multiply(price);

        try {
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
                    return balance.multiply(getTicker(new ExchangePair(exchangeType, currency + "/BTC")).getLast())
                            .setScale(8, HALF_UP);
            }
        } catch (Exception e) {
            return ZERO;
        }
    }

    public BigDecimal getMinOrderVolume(ExchangeType exchangeType, String counterSymbol) {
        if (BITTREX.equals(exchangeType)) {
            switch (counterSymbol) {
                case "BTC":
                    return new BigDecimal("0.008");
                case "LTC":
                    return new BigDecimal("0.08");
                case "BC":
                    return new BigDecimal("13");

                default: return null;
            }
        }else{
            switch (counterSymbol) {
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

    private void scheduleUpdate(ExchangeType exchangeType){
        try {
            updateBalance(exchangeType);
            updateOpenOrders(exchangeType);
            updateTicker(exchangeType);

            tradeAlpha(exchangeType);
        } catch (Exception e) {
            log.error("Schedule update error", e);

            //noinspection ThrowableResultOfMethodCallIgnored
            broadcast(exchangeType, exchangeType.name() + ": " + Throwables.getRootCause(e).getMessage());
        }
    }

    private void updateBalance(ExchangeType exchangeType) throws IOException {
        AccountInfo accountInfo = getExchange(exchangeType).getPollingAccountService().getAccountInfo();
        accountInfoMap.put(exchangeType, accountInfo);

        broadcast(exchangeType, accountInfo);
    }

    private void updateTicker(ExchangeType exchangeType) throws IOException {
        List<String> pairs = traderBean.getTraderPairs(exchangeType);

        for (String pair : pairs) {
            CurrencyPair currencyPair = getCurrencyPair(pair);

            if (currencyPair != null) {
                Ticker ticker;

                if (CRYPTSY.equals(exchangeType)) {
                    ticker = ((CryptsyExchange)cryptsyExchange).getPublicPollingMarketDataService().getTicker(currencyPair);
                }else{
                    ticker = getExchange(exchangeType).getPollingMarketDataService().getTicker(currencyPair);
                }

                if (ticker.getLast() != null && ticker.getLast().compareTo(ZERO) != 0 && ticker.getBid() != null && ticker.getAsk() != null) {
                    ExchangePair exchangePair = new ExchangePair(exchangeType, pair);

                    //ticker history
                    Ticker previous = tickerMap.get(exchangePair);

                    if (previous != null && previous.getLast().compareTo(ticker.getLast()) != 0){
                        traderBean.save(new TickerHistory(exchangeType, pair, ticker.getLast(), ticker.getVolume()));
                    }

                    //ticker map
                    tickerMap.put(exchangePair, ticker);

                    broadcast(exchangeType, ticker);
                }
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

    private void updateOpenOrders(ExchangeType exchangeType) throws IOException {
        OpenOrders openOrders = getExchange(exchangeType).getPollingTradeService().getOpenOrders();
        openOrdersMap.put(exchangeType, openOrders);

        broadcast(exchangeType, openOrders);
    }

    private void tradeAlpha(ExchangeType exchangeType) throws IOException {
        List<Trader> traders = traderBean.getTraders(exchangeType);

        for (Trader trader : traders) {
            try {
                ExchangePair exchangePair = ExchangePair.of(exchangeType, trader.getPair());
                Integer errorCount = errorMap.containsKey(exchangePair) ? errorMap.get(exchangePair) : 0;

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
                    Ticker ticker = getTicker(new ExchangePair(exchangeType, trader.getPair()));
                    CurrencyPair currencyPair = getCurrencyPair(trader.getPair());

                    PollingTradeService tradeService = getExchange(exchangeType).getPollingTradeService();

                    if (ticker == null) {
                        continue;
                    }

                    BigDecimal middlePrice;

                    switch (currencyPair.counterSymbol) {
                        case "BC":
                            try {
                                middlePrice = getTicker(new ExchangePair(BITTREX, currencyPair.baseSymbol + "/BTC")).getLast()
                                        .divide(getTicker(new ExchangePair(BITTREX, "BC/BTC")).getLast(), 8, HALF_UP);
                            } catch (Exception e) {
                                continue;
                            }
                            break;
                        default:
                            middlePrice = ticker.getAsk().add(ticker.getBid()).divide(new BigDecimal("2"), 8, HALF_UP);
                    }

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

                    BigDecimal minSpread = middlePrice.multiply(new BigDecimal("0.013")).setScale(8, HALF_UP);
                    BigDecimal minOrderAmount = getMinOrderVolume(exchangeType, currencyPair.counterSymbol).divide(middlePrice, 8, HALF_UP);

                    //volatility
                    BigDecimal volatility = traderBean.getSigma(exchangePair).divide(ticker.getLast(), 8, ROUND_UP);

                    minSpread = minSpread.multiply(ONE.add(volatility.multiply(BigDecimal.valueOf(Math.PI)))).setScale(8, HALF_UP);
                    minOrderAmount = minOrderAmount.multiply(ONE.add(volatility.multiply(BigDecimal.valueOf(Math.PI)))).setScale(8, HALF_UP);

                    if (minSpread.compareTo(ZERO) == 0) {
                        minSpread = trader.getPair().contains("/USD") ? new BigDecimal("0.02") : new BigDecimal("0.00000002");
                    }

                    //cancel orders
                    for (LimitOrder order : getOpenOrders(exchangeType).getOpenOrders()) {
                        if (currencyPair.equals(order.getCurrencyPair()) && order.getLimitPrice().subtract(middlePrice)
                                .abs().compareTo(minSpread.multiply(BigDecimal.valueOf(2))) > 0) {
                            tradeService.cancelOrder(order.getId());

                            broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": Cancel "
                                    + order.getTradableAmount().toString() + " @ " + order.getLimitPrice().toString());
                        }
                    }

                    //create order
                    for (int index : Arrays.asList(1, 2, 3)) {
                        BigDecimal delta = minSpread.multiply(BigDecimal.valueOf(index)).divide(new BigDecimal("2"), 8, HALF_UP);

                        BigDecimal spreadSumAmount = ZERO;
                        for (LimitOrder order : getOpenOrders(exchangeType).getOpenOrders()) {
                            if (currencyPair.equals(order.getCurrencyPair()) && order.getLimitPrice().subtract(middlePrice)
                                    .abs().compareTo(delta.multiply(BigDecimal.valueOf(1.5))) <= 0) {
                                spreadSumAmount = spreadSumAmount.add(order.getTradableAmount());
                            }
                        }

                        if (spreadSumAmount.compareTo(minOrderAmount.multiply(BigDecimal.valueOf(index))) > 0) {
                            break;
                        }

                        AccountInfo accountInfo = getAccountInfo(exchangeType);


                        BigDecimal randomAskAmount = ZERO;
                        BigDecimal minRandomAskAmount = BITTREX.equals(exchangeType)
                                ? random10(minOrderAmount.multiply(BigDecimal.valueOf(index)))
                                : random30(minOrderAmount.multiply(BigDecimal.valueOf(index)));

                        randomAskAmount = randomAskAmount.compareTo(minRandomAskAmount) > 0
                                ? randomAskAmount
                                : minRandomAskAmount;

                        BigDecimal randomBidAmount = ZERO;
                        BigDecimal minRandomBidAmount = random30(minOrderAmount.multiply(BigDecimal.valueOf(index)));

                        randomBidAmount = randomBidAmount.compareTo(minOrderAmount) > 0
                                ? randomBidAmount
                                : minRandomBidAmount;

                        //check ask
                        if (accountInfo.getBalance(currencyPair.counterSymbol).compareTo(randomAskAmount.multiply(middlePrice)) < 0) {
                            broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": Buy "
                                    + randomAskAmount.toString() + " ^ " + middlePrice.toString());

                            errorMap.put(exchangePair, ++errorCount);

                            continue;
                        }

                        //check bid
                        if (accountInfo.getBalance(currencyPair.baseSymbol).compareTo(randomBidAmount) < 0) {
                            broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": Sell "
                                    + randomBidAmount.toString() + " ^ " + middlePrice.toString());

                            errorMap.put(exchangePair, ++errorCount);

                            continue;
                        }

                        //BID
                        BigDecimal randomDelta = random10(delta);
                        BigDecimal bidPrice;

                        if ("USD".equals(currencyPair.counterSymbol)) {
                            randomDelta = randomDelta.setScale(2, HALF_UP);

                            bidPrice = middlePrice.subtract(randomDelta.compareTo(ZERO) == 0
                                    ? new BigDecimal("0.01")
                                    : randomDelta).setScale(2, HALF_UP);
                        } else {
                            bidPrice = middlePrice.subtract(randomDelta.compareTo(ZERO) == 0
                                    ? new BigDecimal("0.00000001")
                                    : randomDelta);
                        }

                        tradeService.placeLimitOrder(new LimitOrder(Order.OrderType.BID,
                                randomBidAmount,
                                currencyPair, "", new Date(),
                                bidPrice));

                        broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": Buy "
                                + randomBidAmount.toString() + " @ " + bidPrice.toString() + " | " + delta.toString());

                        //ASK
                        randomDelta = random10(delta);
                        BigDecimal askPrice;

                        if ("USD".equals(currencyPair.counterSymbol)) {
                            randomDelta = randomDelta.setScale(2, HALF_UP);

                            askPrice = middlePrice.add(randomDelta.compareTo(ZERO) == 0
                                    ? new BigDecimal("0.01")
                                    : randomDelta).setScale(2, HALF_UP);
                        } else {
                            askPrice = middlePrice.add(randomDelta.compareTo(ZERO) == 0
                                    ? new BigDecimal("0.00000001")
                                    : randomDelta);
                        }

                        tradeService.placeLimitOrder(new LimitOrder(Order.OrderType.ASK,
                                randomAskAmount,
                                currencyPair, "", new Date(),
                                askPrice));

                        broadcast(exchangeType, exchangeType.name() + " " + trader.getPair() + ": Sell "
                                + randomAskAmount.toString() + " @ " + askPrice.toString() + " | " + delta.toString());

                    }
                }
            } catch (Exception e) {
                log.error("alpha trade error", e);

                //noinspection ThrowableResultOfMethodCallIgnored
                broadcast(exchangeType, trader.getPair() + ": " + Throwables.getRootCause(e).getMessage());
            }
        }
    }

    private void broadcast(ExchangeType exchange, Object payload){
        try {
            Application application = Application.get("aida-coin");
            IWebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);

            WebSocketPushBroadcaster broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
            broadcaster.broadcastAll(application, new ExchangeMessage<>(exchange, payload));
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

    public OrderBook getOrderBook(ExchangePair exchangePair){
        return orderBookMap.get(exchangePair);
    }

    public OpenOrders getOpenOrders(ExchangeType exchangeType){
        return openOrdersMap.get(exchangeType);
    }

    public BalanceHistory getBalanceHistory(ExchangePair exchangePair){ return balanceHistoryMap.get(exchangePair); }
}
