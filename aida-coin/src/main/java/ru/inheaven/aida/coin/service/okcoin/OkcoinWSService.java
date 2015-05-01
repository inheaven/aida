package ru.inheaven.aida.coin.service.okcoin;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.Depth;
import ru.inheaven.aida.coin.entity.OrderType;
import ru.inheaven.aida.coin.entity.SymbolType;
import ru.inheaven.aida.coin.entity.Trade;
import ru.inheaven.aida.coin.service.DepthService;
import ru.inheaven.aida.coin.service.TradeService;
import ru.inhell.aida.common.rx.ObservableEndpoint;
import rx.Observer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.json.JsonArray;
import javax.json.JsonObject;
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

    @EJB
    private DepthService depthService;

    private final static String OKCOIN_WSS = "wss://real.okcoin.com:10440/websocket/okcoinapi";

    ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());

    private ObservableEndpoint marketDataEndpoint = ObservableEndpoint.create();
    private ObservableEndpoint tradingEndpoint = ObservableEndpoint.create();

    private class JsonData{
        private String channel;
        private JsonValue value;

        public JsonData(String channel, JsonValue value) {
            this.channel = channel;
            this.value = value;
        }
    }

    @PostConstruct
    public void start(){
        client.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);

        try {
            client.connectToServer(marketDataEndpoint, URI.create(OKCOIN_WSS));

            marketDataEndpoint.getSession().getBasicRemote().sendText("[" +
                            "{'event':'addChannel','channel':'ok_btcusd_future_trade_v1_this_week'}," +
                            "{'event':'addChannel','channel':'ok_ltcusd_future_trade_v1_this_week'}," +
                            "{'event':'addChannel','channel':'ok_btcusd_future_depth_this_week_60'}," +
                            "{'event':'addChannel','channel':'ok_ltcusd_future_depth_this_week_60'}]"
            );

            marketDataEndpoint.getJsonObservable()
                    .filter(j -> j.getString("channel").contains("_future_trade_v1_"))
                    .flatMapIterable(j -> j.getJsonArray("data"), (o, v) -> new JsonData(o.getString("channel"), v))
                    .map(j -> {
                        JsonArray a = (JsonArray) j.value;

                        Trade trade = new Trade();

                        trade.setTradeId(a.getString(0));
                        trade.setSymbol(getSymbol(j.channel));
                        trade.setSymbolType(getSymbolType(j.channel));
                        trade.setPrice(new BigDecimal(a.getString(1)));
                        trade.setAmount(new BigDecimal(a.getString(2)));
                        trade.setDate(Date.from(LocalTime.parse(a.getString(3)).atDate(now()).toInstant(ofHours(8))));
                        trade.setOrderType(OrderType.valueOf(a.getString(4).toUpperCase()));

                        return trade;
                    })
                    .subscribe(tradeService.getTradeObserver());

            marketDataEndpoint.getJsonObservable()
                    .filter(j -> j.getString("channel").contains("_future_depth_"))
                    .map(j -> new JsonData(j.getString("channel"), j.getJsonObject("data")))
                    .map(j -> {
                        Depth depth = new Depth();

                        depth.setSymbol(getSymbol(j.channel));
                        depth.setSymbolType(getSymbolType(j.channel));
                        depth.setDate(new Date(Long.parseLong(((JsonObject)j.value).getString("timestamp"))));
                        depth.setData(j.value.toString());

                        return depth;
                    })
                    .subscribe(depthService.getDepthObserver());

            //debug
            marketDataEndpoint.getObservable().subscribe(new Observer<String>() {
                @Override
                public void onCompleted() {
                    System.out.println("marketDataEndpoint::onCompleted");
                }

                @Override
                public void onError(Throwable e) {
                    e.printStackTrace();
                }

                @Override
                public void onNext(String s) {
                    System.out.println(s);
                }
            });

        } catch (Exception e) {
            log.error("error connect to server", e);
        }
    }

    public String getSymbol(String channel){
        if (channel.contains("_btcusd_")){
            return "BTC/USD";
        }else if (channel.contains("_ltcusd_")){
            return "LTC/USD";
        }

        return null;
    }

    public SymbolType getSymbolType(String channel){
        if (channel.contains("this_week")){
            return  SymbolType.THIS_WEEK;
        }else if (channel.contains("next_week")){
            return  SymbolType.NEXT_WEEK;
        }else if (channel.contains("quarter")){
            return  SymbolType.QUARTER;
        }

        return null;
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
