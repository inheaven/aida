package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.*;
import com.xeiam.xchange.bittrex.v1.BittrexExchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.service.polling.PollingMarketDataService;
import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.ExchangeMessage;
import ru.inheaven.aida.coin.entity.Exchanges;
import ru.inheaven.aida.coin.entity.ExchangePair;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.*;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

    private Exchange bittrexExchange;

    public Exchange getBittrexExchange(){
        if (bittrexExchange == null){
            ExchangeSpecification exSpec = new ExchangeSpecification(BittrexExchange.class);
            exSpec.setApiKey("52bb28e4e5454efd90b427e4ea1f4e1e ");
            exSpec.setSecretKey("6d4548bfbc404176829a94ae5471fd82");

            bittrexExchange = ExchangeFactory.INSTANCE.createExchange(exSpec);
        }

        return bittrexExchange;
    }

    public Ticker getTicker(Exchanges exchange, String pair){
        return tickerMap.get(new ExchangePair(exchange,pair));
    }

    @PostConstruct
    protected void startBittrexTickerUpdate(){
        executorService.schedule(new Runnable() {
            @Override
            public void run() {
                try {
                    PollingMarketDataService marketDataService = TraderService.this.getBittrexExchange().getPollingMarketDataService();

                    for (String pair : traderBean.getTraderPairs()) {
                        String[] cp = pair.split("/");

                        if (cp.length == 2) {
                            Ticker ticker = marketDataService.getTicker(new CurrencyPair(cp[0], cp[1]));
                            tickerMap.put(new ExchangePair(Exchanges.BITTREX, pair), ticker);

                            TraderService.this.broadcast(Exchanges.BITTREX, ticker);
                        }
                    }
                } catch (Exception e) {
                    log.error("Update ticker error", e);
                }
            }
        }, new FixedRateTrigger(1000L));
    }

    private void broadcast(Exchanges exchange, Object payload){
        Application application = Application.get("aida-coin");
        IWebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);

        WebSocketPushBroadcaster broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
        broadcaster.broadcastAll(application, new ExchangeMessage<>(exchange, payload));
    }
}
