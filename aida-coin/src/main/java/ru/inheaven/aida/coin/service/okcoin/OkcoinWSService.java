package ru.inheaven.aida.coin.service.okcoin;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.*;
import ru.inheaven.aida.coin.service.DepthService;
import ru.inheaven.aida.coin.service.OrderService;
import ru.inheaven.aida.coin.service.TradeService;
import ru.inhell.aida.common.rx.JsonObservableEndpoint;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.ejb.*;
import javax.json.*;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

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

    @EJB
    private OrderService orderService;

    private final static String OKCOIN_WSS = "wss://real.okcoin.com:10440/websocket/okcoinapi";

    ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());

    private JsonObservableEndpoint marketDataEndpoint = JsonObservableEndpoint.create();
    private JsonObservableEndpoint tradingEndpoint = JsonObservableEndpoint.create();

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

            //trade
            marketDataEndpoint.getJsonObservable()
                    .filter(j -> j.getString("channel").contains("_future_trade_v1_"))
                    .flatMapIterable(j -> j.getJsonArray("data"), (o, v) -> new JsonData(o.getString("channel"), v))
                    .filter(d -> d.value.getValueType().equals(JsonValue.ValueType.ARRAY))
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
                        trade.setExchangeType(ExchangeType.OKCOIN_FUTURES);

                        return trade;
                    }).subscribe(tradeService.getTradeSubject());

            //depth
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
                    }).subscribe(depthService.getDepthObserver());

            //order info
            tradingEndpoint.getJsonObservable()
                    .filter(j -> j.getString("channel").equals("ok_futureusd_order_info"))
                    .map(j -> j.getJsonObject("data"))
                    .filter(j -> j.getBoolean("result"))
                    .flatMapIterable(j -> j.getJsonArray("orders"))
                    .filter(j -> j.getValueType().equals(JsonValue.ValueType.OBJECT)).map(j -> (JsonObject) j)
                    .map(j -> {
                        Order order = new Order();

                        order.setAmount(BigDecimal.valueOf(j.getJsonNumber("amount").doubleValue()));
                        order.setName(j.getString("contract_name"));
                        order.setCreated(new Date(j.getJsonNumber("create_date").longValue()));
                        order.setFilledAmount(BigDecimal.valueOf(j.getJsonNumber("deal_amount").doubleValue()));
                        order.setFee(BigDecimal.valueOf(j.getJsonNumber("fee").doubleValue()));
                        order.setOrderId(j.getJsonNumber("order_id").toString());
                        order.setPrice(BigDecimal.valueOf(j.getJsonNumber("price").doubleValue()));
                        order.setAvgPrice(BigDecimal.valueOf(j.getJsonNumber("price_avg").doubleValue()));
                        order.setSymbol(j.getString("symbol"));

                        switch (j.getInt("status")) {
                            case -1:
                            case 4:
                                order.setStatus(OrderStatus.CANCELED);
                                break;
                            case 2:
                                order.setStatus(OrderStatus.CLOSED);
                                break;
                            default:
                                order.setStatus(OrderStatus.OPEN);
                                break;
                        }

                        switch (j.getInt("type")) {
                            case 1:
                                order.setType(OrderType.OPEN_LONG);
                                break;
                            case 2:
                                order.setType(OrderType.OPEN_SHORT);
                                break;
                            case 3:
                                order.setType(OrderType.CLOSE_LONG);
                                break;
                            case 4:
                                order.setType(OrderType.CLOSE_SHORT);
                                break;
                        }

                        return order;
                    }).subscribe(orderService.getOrderSubject());

            //real trade todo



        } catch (Exception e) {
            log.error("error connect to server", e);
        }
    }

    public void orderInfo(String apiKey, String secretKey, String symbol, String orderId, String contractType,
                          String status, String currentPage, String pageLength){
        try {
            JsonObjectBuilder parameters = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .add("symbol", symbol)
                    .add("order_id", orderId)
                    .add("contract_type", contractType)
                    .add("status", status)
                    .add("current_page", currentPage)
                    .add("page_length", pageLength);

            parameters.add("sign", getMD5String(parameters.build(), secretKey));

            tradingEndpoint.getSession().getBasicRemote().sendText(Json.createObjectBuilder()
                    .add("event", "addChannel")
                    .add("channel", "ok_futureusd_order_info")
                    .add("parameters", parameters).build().toString());
        } catch (IOException e) {
            log.error("order info error", e);
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

    private static final char HEX_DIGITS[] = {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
    };

    public String getMD5String(JsonObject parameters, String secretKey) {
        try {
            List<String> keys = new ArrayList<>(parameters.keySet());
            Collections.sort(keys);
            String str = "";

            for (String key : keys) {
                str += key + "=" + ((JsonString) parameters.get(key)).getString() + "&";
            }

            str = str + "secret_key=" + secretKey;

            byte[] bytes = str.getBytes();
            MessageDigest messageDigest = MessageDigest.getInstance("MD5");
            messageDigest.update(bytes);
            bytes = messageDigest.digest();
            StringBuilder sb = new StringBuilder();
            for (byte aByte : bytes) {
                sb.append(HEX_DIGITS[(aByte & 0xf0) >> 4]).append("").append(HEX_DIGITS[aByte & 0xf]);
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }
}
