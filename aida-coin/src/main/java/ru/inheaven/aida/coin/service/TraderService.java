package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.ExchangeFactory;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.bittrex.v1.BittrexExchange;
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

import javax.ejb.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static java.math.BigDecimal.ROUND_HALF_DOWN;
import static java.math.BigDecimal.ROUND_HALF_UP;
import static ru.inheaven.aida.coin.entity.ExchangeName.BITTREX;
import static ru.inheaven.aida.coin.util.TraderUtil.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 09.01.14 17:06
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class TraderService {
    private Logger log = LoggerFactory.getLogger(TraderService.class);

    @EJB
    private TraderBean traderBean;

    private Exchange bittrexExchange;

    private Map<ExchangePair, Ticker> tickerMap = new ConcurrentHashMap<>();
    private Map<ExchangeName, OpenOrders> openOrdersMap = new ConcurrentHashMap<>();

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

    @Schedule(second = "*/3", minute="*", hour="*", persistent=false)
    public void scheduleBittrexUpdate(){
        try {
            updateBittrexBalance();
            updateBittrexTicker();
            updateBittrexOpenOrders();
            tradeBittrexAlpha();
        } catch (Exception e) {
            log.error("update ticker error", e);

            broadcast(BITTREX, e);
        }
    }

    public void updateBittrexBalance() throws IOException {
        broadcast(BITTREX, getBittrexExchange().getPollingAccountService().getAccountInfo());
    }

    //todo generalize update ticker
    private void updateBittrexTicker() throws IOException {
        PollingMarketDataService marketDataService = getBittrexExchange().getPollingMarketDataService();

        for (String pair : traderBean.getTraderPairs()) {
            CurrencyPair currencyPair = getCurrencyPair(pair);

            if (currencyPair != null && !currencyPair.baseSymbol.equals("BTC")) {
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

                BigDecimal delta = trader.getSpread().divide(new BigDecimal("2"), 8, ROUND_HALF_DOWN);
                BigDecimal minDelta = ticker.getLast().multiply(new BigDecimal("0.015")).setScale(8, ROUND_HALF_DOWN);
                delta = delta.compareTo(minDelta) > 0 ? delta : minDelta;

                BigDecimal level = trader.getHigh().subtract(trader.getLow()).divide(trader.getSpread(), 8, ROUND_HALF_UP);
                BigDecimal minOrderAmount = new BigDecimal("0.000555").divide(ticker.getLast(), 8, ROUND_HALF_UP);

                for (LimitOrder order : getOpenOrders(BITTREX).getOpenOrders()){
                    if (ticker.getCurrencyPair().equals(order.getCurrencyPair())
                            && order.getLimitPrice().subtract(ticker.getLast()).abs().compareTo(delta) <= 0){
                        hasOrder = true;
                        break;
                    }
                }

                if (!hasOrder){
                    PollingTradeService tradeService = getBittrexExchange().getPollingTradeService();

                    BigDecimal randomAmount;

                    //BID
                    randomAmount = random50(trader.getVolume().divide(level, 8, ROUND_HALF_UP));

                    tradeService.placeLimitOrder(new LimitOrder(Order.OrderType.BID,
                            randomAmount.compareTo(minOrderAmount) > 0 ? randomAmount : minOrderAmount,
                            ticker.getCurrencyPair(), "", new Date(),
                            ticker.getLast().subtract(random20(delta))));

                    //ASK
                    randomAmount = random50(trader.getVolume().divide(level, 8, ROUND_HALF_UP));

                    tradeService.placeLimitOrder(new LimitOrder(Order.OrderType.ASK,
                            randomAmount.compareTo(minOrderAmount) > 0 ? randomAmount : minOrderAmount,
                            ticker.getCurrencyPair(), "", new Date(),
                            ticker.getLast().add(random20(delta))));
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
