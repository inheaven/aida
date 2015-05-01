package ru.inheaven.aida.coin.service.okcoin;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.OrderType;
import ru.inheaven.aida.coin.entity.SymbolType;
import ru.inheaven.aida.coin.entity.Trade;
import ru.inheaven.aida.coin.service.TradeService;
import ru.inhell.aida.common.rx.ObservableEndpoint;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.json.JsonArray;
import javax.json.JsonValue;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.time.LocalTime;
import java.util.Date;

import static java.time.LocalDate.now;
import static java.time.ZoneOffset.ofHours;

/**
 * @author inheaven on 20.04.2015 22:34.
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class OkcoinWSService {
    private Logger log = LoggerFactory.getLogger(getClass());

    @EJB
    private TradeService tradeService;

    private final static String OKCOIN_WSS = "wss://real.okcoin.com:10440/websocket/okcoinapi";

    ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());

    private ObservableEndpoint marketDataEndpoint = ObservableEndpoint.create();
    private ObservableEndpoint tradingEndpoint = ObservableEndpoint.create();

    @PostConstruct
    public void start(){
        client.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);

        try {
            client.connectToServer(marketDataEndpoint, URI.create(OKCOIN_WSS));

            marketDataEndpoint.getSession().getBasicRemote().sendText("[" +
                            "{'event':'addChannel','channel':'ok_btcusd_future_trade_v1_this_week'}," +
                            "{'event':'addChannel','channel':'ok_ltcusd_future_trade_v1_this_week'}," +
                            "{'event':'addChannel','channel':'ok_btcusd_future_depth_this_week'}]"
            );

            publishTrade("ok_ltcusd_future_trade_v1_this_week", "LTC/USD", SymbolType.THIS_WEEK);
            publishTrade("ok_btcusd_future_trade_v1_this_week", "BTC/USD", SymbolType.THIS_WEEK);

        } catch (Exception e) {
            log.error("error connect to server", e);
        }
    }

    private void publishTrade(String channel, String symbol, SymbolType symbolType){
        marketDataEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel").equals(channel))
                .flatMapIterable(j -> {
                    System.out.println(j.toString());

                    return j.getJsonArray("data");
                })
                .filter(j -> j.getValueType() == JsonValue.ValueType.ARRAY).map(j -> (JsonArray) j)
                .map(j -> {
                    Trade trade = new Trade();

                    trade.setTradeId(j.getString(0));
                    trade.setPrice(new BigDecimal(j.getString(1)));
                    trade.setAmount(new BigDecimal(j.getString(2)));
                    trade.setDate(Date.from(LocalTime.parse(j.getString(3)).atDate(now()).toInstant(ofHours(8))));
                    trade.setOrderType(OrderType.valueOf(j.getString(4).toUpperCase()));
                    trade.setSymbolType(symbolType);
                    trade.setSymbol(symbol);

                    return trade;
                })
                .subscribe(tradeService.getTradeObserver());
    }

    @PreDestroy
    public void stop(){
        try {
            marketDataEndpoint.getSession().close();
            tradingEndpoint.getSession().close();
        } catch (IOException e) {
            log.error("error close session", e);
        }
    }


    /*
    * websocket.send("{'event':'addChannel', 'channel':'ok_futureusd_order_info', 'parameters':{ 'api_key':'XXXX',
     * 'symbol':'XXXX', 'order_id':'XXXX', 'contract_type':'XXXXXX', 'status':'XXXX', 'current_page':'XXXX',
      * 'page_length':'XXXX', sign':'XXXX'} }")
    * */



}
