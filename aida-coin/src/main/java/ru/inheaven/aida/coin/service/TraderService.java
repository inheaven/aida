package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.*;
import com.xeiam.xchange.bittrex.v1.BittrexExchange;
import com.xeiam.xchange.bittrex.v1.service.polling.BittrexTradeService;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.service.polling.PollingMarketDataService;
import com.xeiam.xchange.service.polling.PollingTradeService;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.ExchangeMessage;
import ru.inheaven.aida.coin.entity.ExchangeName;
import ru.inheaven.aida.coin.entity.ExchangePair;
import ru.inheaven.aida.coin.entity.Trader;
import ru.inheaven.aida.coin.util.TraderUtil;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.security.SecureRandom;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.BigDecimal.ROUND_HALF_UP;
import static java.math.MathContext.DECIMAL32;
import static java.math.RoundingMode.HALF_UP;
import static ru.inheaven.aida.coin.entity.ExchangeName.BITTREX;
import static ru.inheaven.aida.coin.util.TraderUtil.random20;
import static ru.inheaven.aida.coin.util.TraderUtil.random50;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 17:06
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TraderService {
    private Logger log = LoggerFactory.getLogger(TraderService.class);



    @Resource
    private ManagedScheduledExecutorService executorService;

    @EJB
    private TraderBean traderBean;

    private Map<ExchangePair, Ticker> tickerMap = new ConcurrentHashMap<>();
    private Map<ExchangeName, OpenOrders> openOrdersMap = new ConcurrentHashMap<>();

    private Exchange bittrexExchange;

    public Exchange getBittrexExchange(){
        if (bittrexExchange == null){
            ExchangeSpecification exSpec = new ExchangeSpecification(BittrexExchange.class);
            exSpec.setApiKey("14935ef36d8b4afc8204946be7ddd152");
            exSpec.setSecretKey("44d84de3865e4fbfa4c17dd42c026d11");

            bittrexExchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
        }

        return bittrexExchange;
    }

    public Ticker getTicker(ExchangeName exchange, String pair){
        return tickerMap.get(new ExchangePair(exchange,pair));
    }

    public OpenOrders getOpenOrders(ExchangeName exchangeName){
        return openOrdersMap.get(exchangeName);
    }

    @PostConstruct
    protected void startBittrexUpdate(){
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    updateBittrexTicker();
                    updateBittrexOpenOrders();
                    tradeBittrexAlpha();
                } catch (Exception e) {
                    log.error("Bittrex update error", e);

                    broadcast(BITTREX, e);
                }
            }
        }, new FixedRateTrigger(1000L));
    }

    //todo generalize update ticker
    private void updateBittrexTicker() throws IOException {
        PollingMarketDataService marketDataService = getBittrexExchange().getPollingMarketDataService();

        for (String pair : traderBean.getTraderPairs()) {
            CurrencyPair currencyPair = TraderUtil.getCurrencyPair(pair);

            if (currencyPair != null) {
                Ticker ticker = marketDataService.getTicker(currencyPair);

                tickerMap.put(new ExchangePair(BITTREX, pair), ticker);
                broadcast(BITTREX, ticker);
            }
        }
    }

    private void updateBittrexOpenOrders() throws IOException {
        OpenOrders openOrders = getBittrexExchange().getPollingTradeService().getOpenOrders();

        openOrdersMap.put(BITTREX, openOrders);
        broadcast(BITTREX, openOrders);
    }

    private void tradeBittrexAlpha() throws IOException {
        for (Trader trader : traderBean.getTraders()){
            Ticker ticker = getTicker(BITTREX, trader.getPair());

            if (trader.isRunning()
                    && ticker.getLast().compareTo(trader.getHigh()) < 0
                    && ticker.getLast().compareTo(trader.getLow()) > 0){
                boolean hasOrder = false;

                for (LimitOrder order : getOpenOrders(BITTREX).getOpenOrders()){
                    if (ticker.getCurrencyPair().equals(order.getCurrencyPair())
                            && order.getLimitPrice().subtract(ticker.getLast()).abs().compareTo(trader.getSpread()) < 0){
                        hasOrder = true;
                        break;
                    }
                }

                if (!hasOrder){
                    PollingTradeService tradeService = getBittrexExchange().getPollingTradeService();

                    BigDecimal level = trader.getHigh().subtract(trader.getLow()).divide(trader.getSpread(), 8, ROUND_HALF_UP);

                    //BID
                    tradeService.placeLimitOrder(new LimitOrder(Order.OrderType.BID,
                            random50(trader.getVolume().divide(level, 8, ROUND_HALF_UP)),
                            ticker.getCurrencyPair(), "", new Date(),
                            ticker.getLast().subtract(random20(trader.getSpread()))));

                    //ASK
                    tradeService.placeLimitOrder(new LimitOrder(Order.OrderType.ASK,
                            random50(trader.getVolume().divide(level, 8, ROUND_HALF_UP)),
                            ticker.getCurrencyPair(), "", new Date(),
                            ticker.getLast().add(random20(trader.getSpread()))));
                }
            }
        }
    }

    private void broadcast(ExchangeName exchange, Object payload){
        Application application = Application.get("aida-coin");
        IWebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);

        WebSocketPushBroadcaster broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
        broadcaster.broadcastAll(application, new ExchangeMessage<>(exchange, payload));
    }
}
