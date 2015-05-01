package ru.inheaven.aida.coin.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.rx.ObservableEndpoint;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonValue;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

/**
 * @author inheaven on 20.04.2015 22:34.
 */
@Startup
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class OkcoinWSService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final static String OKCOIN_WSS = "wss://real.okcoin.com:10440/websocket/okcoinapi";

    ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());

    private ObservableEndpoint marketDataEndpoint = ObservableEndpoint.create();
    //private ObservableEndpoint tradingEndpoint = ObservableEndpoint.create();

    @PostConstruct
    public void start(){
        client.getProperties().put(ClientProperties.LOG_HTTP_UPGRADE, true);

        try {
            client.connectToServer(marketDataEndpoint, URI.create(OKCOIN_WSS));

            marketDataEndpoint.getSession().getBasicRemote().sendText("" +
                            "[{'event':'addChannel','channel':'ok_btcusd_future_trade_v1_this_week'}," +
                            "{'event':'addChannel','channel':'ok_btcusd_future_depth_this_week'}]"
            );

            ObjectMapper mapper = new ObjectMapper();

            marketDataEndpoint.getObservable()
                    .flatMapIterable(s ->
                            Json.createReader(new StringReader(s)).readArray())
                    .filter(j -> j.getValueType().equals(JsonValue.ValueType.OBJECT)
                            && ((JsonObject) j).getString("channel").equals("ok_btcusd_future_trade_v1_this_week"))
                    .subscribe(j -> {
                        System.out.println(j.toString());
                    });


        } catch (Exception e) {
            log.error("error connect to server", e);
        }
    }

    @PreDestroy
    public void stop(){
        try {
            marketDataEndpoint.getSession().close();
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
