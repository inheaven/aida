package ru.inheaven.aida.happy.trading.service;

import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.glassfish.tyrus.container.jdk.client.JdkClientContainer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inhell.aida.common.rx.JsonObservableEndpoint;
import rx.Observable;

import javax.inject.Singleton;
import javax.json.*;
import javax.websocket.CloseReason;
import javax.websocket.EndpointConfig;
import javax.websocket.Session;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static java.time.LocalDate.now;
import static java.time.ZoneOffset.ofHours;

/**
 * @author inheaven on 20.04.2015 22:34.
 */
@Singleton
public class OkcoinService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private final static String OKCOIN_WSS = "wss://real.okcoin.com:10440/websocket/okcoinapi";

    private JsonObservableEndpoint marketEndpoint;
    private JsonObservableEndpoint tradingEndpoint;

    private Observable<Long> marketDataHeartbeatObservable;
    private Observable<Long> tradingHeartbeatObservable;

    private long lastTrade = System.currentTimeMillis();
    private long lastOrder = System.currentTimeMillis();

    private Set<String> tradesChannels = new HashSet<>();

    private boolean destroy = false;

    private class JsonData{
        private String channel;
        private JsonValue value;

        public JsonData(String channel, JsonValue value) {
            this.channel = channel;
            this.value = value;
        }
    }

    private String markerChannels = "[" +
            "{'event':'addChannel','channel':'ok_ltcusd_future_trade_v1_this_week'}," +
            "{'event':'addChannel','channel':'ok_ltcusd_future_trade_v1_next_week'}," +
            "{'event':'addChannel','channel':'ok_ltcusd_future_trade_v1_quarter'}" +
            "{'event':'addChannel','channel':'ok_ltcusd_future_depth_this_week'}," +
            "{'event':'addChannel','channel':'ok_ltcusd_future_depth_next_week'}," +
            "{'event':'addChannel','channel':'ok_ltcusd_future_depth_quarter_week'}," +

            "{'event':'addChannel','channel':'ok_btcusd_future_trade_v1_this_week'}," +
            "{'event':'addChannel','channel':'ok_btcusd_future_trade_v1_quarter'}," +

            "{'event':'addChannel','channel':'ok_ltcusd_trades_v1', 'parameters':{'api_key':'00dff9d7-7d99-45f9-bd41-23d08d4665ce','sign':'CB58C8091A0605AAD1F5815F215BB93B'}}," +
            "{'event':'addChannel','channel':'ok_ltcusd_depth', 'parameters':{'api_key':'00dff9d7-7d99-45f9-bd41-23d08d4665ce','sign':'CB58C8091A0605AAD1F5815F215BB93B'}}," +

            "{'event':'addChannel','channel':'ok_btcusd_trades_v1', 'parameters':{'api_key':'00dff9d7-7d99-45f9-bd41-23d08d4665ce','sign':'CB58C8091A0605AAD1F5815F215BB93B'}}," +
            "{'event':'addChannel','channel':'ok_btcusd_depth', 'parameters':{'api_key':'00dff9d7-7d99-45f9-bd41-23d08d4665ce','sign':'CB58C8091A0605AAD1F5815F215BB93B'}}" +
            "]";


    public OkcoinService(){
        try {
            ClientManager client = ClientManager.createClient(JdkClientContainer.class.getName());
            ClientManager client2 = ClientManager.createClient(JdkClientContainer.class.getName());

            ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler(){
                private int reconnectCount = 0;

                @Override
                public boolean onDisconnect(CloseReason closeReason) {
                    log.warn("reconnect -> {}", closeReason.toString());

                    return !destroy;
                }

                @Override
                public boolean onConnectFailure(Exception exception) {
                    log.warn("connection failure -> {}", reconnectCount++);

                    return !destroy;
                }
            };

            client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);
            client2.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);


            marketEndpoint = new JsonObservableEndpoint(){
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    super.onOpen(session, config);

                    try {
                        session.getBasicRemote().sendText(markerChannels);
                    } catch (IOException e) {
                        log.error("error add channel ->", e);
                    }
                }
            };
            client.connectToServer(marketEndpoint, URI.create(OKCOIN_WSS));

//            tradingEndpoint = marketEndpoint;

            tradingEndpoint = new JsonObservableEndpoint(){
                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    super.onOpen(session, config);

                    tradesChannels.forEach(s -> {
                        try {
                            session.getBasicRemote().sendText(s);
                        } catch (IOException e) {
                            log.error("error add channel -> ", e);
                        }
                    });
                }
            };
            client2.connectToServer(tradingEndpoint, URI.create(OKCOIN_WSS));


            //last order trade check
            Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
                try {
                    long now = System.currentTimeMillis();

                    if (now - lastOrder > 300000 || now - lastTrade > 300000) {
                        marketEndpoint.getSession().getBasicRemote().sendText(markerChannels);

                        tradesChannels.forEach(s -> {
                            try {
                                tradingEndpoint.getSession().getBasicRemote().sendText(s);
                            } catch (IOException e) {
                                log.error("error add channel ->", e);
                            }
                        });

                        log.info("add channels");
                    }
                } catch (IOException e) {
                    log.error("error add channel ->", e);
                }
            }, 0, 1, TimeUnit.MINUTES);

            //success
            marketEndpoint.getJsonObservable().filter(j -> j.getString("success", null) != null).subscribe(System.out::println);
            tradingEndpoint.getJsonObservable().filter(j -> j.getString("success", null) != null).subscribe(System.out::println);

            //heartbeat
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    if (marketEndpoint.getSession().isOpen()) {
                        marketEndpoint.getSession().getBasicRemote().sendText("{'event':'ping'}");
                    }
                } catch (Exception e) {
                    log.error("marketEndpoint heartbeat error ->", e);
                }
            }, 0, 1, TimeUnit.SECONDS);

            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    if (tradingEndpoint.getSession().isOpen()) {
                        tradingEndpoint.getSession().getBasicRemote().sendText("{'event':'ping'}");
                    }
                } catch (Exception e) {
                    log.error("tradingEndpoint heartbeat error ->", e);
                }
            }, 0, 1, TimeUnit.SECONDS);

            marketDataHeartbeatObservable = marketEndpoint.getJsonObservable()
                    .filter(j -> j.getString("event", "").equals("pong"))
                    .map(j -> System.currentTimeMillis());

            tradingHeartbeatObservable = tradingEndpoint.getJsonObservable()
                    .filter(j -> j.getString("event", "").equals("pong"))
                    .map(j -> System.currentTimeMillis());

        } catch (Exception e) {
            log.error("error connect to server ->", e);
        }

        //reconnect
        tradingEndpoint.getJsonObservable()
                .throttleLast(5, TimeUnit.SECONDS)
                .filter(j -> !j.getString("errorcode", "").isEmpty())
                .subscribe(j -> reconnect());

        //log
        tradingEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").equals("ok_usd_future_realtrades"))
                .subscribe(j -> log.info(j.toString()));
        tradingEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").equals("ok_futureusd_order_info"))
                .subscribe(j -> log.info(j.toString()));
        tradingEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").equals("ok_usd_realtrades"))
                .subscribe(j -> log.info(j.toString()));
        tradingEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").equals("ok_spotusd_order_info"))
                .subscribe(j -> log.info(j.toString()));
    }


    public Observable<Long> getMarketDataHeartbeatObservable() {
        return marketDataHeartbeatObservable;
    }

    public Observable<Long> getTradingHeartbeatObservable() {
        return tradingHeartbeatObservable;
    }

    private void reconnect(){
        try {
            marketEndpoint.getSession().getBasicRemote().sendText(markerChannels);
        } catch (IOException e) {
            log.error("error add channel ->", e);
        }


        tradesChannels.forEach(s -> {
            try {
                tradingEndpoint.getSession().getBasicRemote().sendText(s);
            } catch (IOException e) {
                log.error("error add channel ->", e);
            }
        });
    }

    //TRADE

    public Observable<Trade> createFutureTradeObservable() {
        return marketEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").contains("_future_trade_v1_"))
                .flatMapIterable(j -> j.getJsonArray("data"), (o, v) -> new JsonData(o.getString("channel"), v))
                .filter(d -> d.value.getValueType().equals(JsonValue.ValueType.ARRAY))
                .map(j -> {
                    lastTrade = System.currentTimeMillis();

                    JsonArray a = (JsonArray) j.value;

                    Trade trade = new Trade();

                    trade.setTradeId(a.getString(0));
                    trade.setExchangeType(ExchangeType.OKCOIN_FUTURES);
                    trade.setSymbol(getSymbol(j.channel));
                    trade.setSymbolType(getSymbolType(j.channel));
                    trade.setPrice(new BigDecimal(a.getString(1)));
                    trade.setAmount(new BigDecimal(a.getString(2)));
                    trade.setDate(Date.from(LocalTime.parse(a.getString(3)).atDate(now()).toInstant(ofHours(8))));
                    trade.setOrderType(OrderType.valueOf(a.getString(4).toUpperCase()));

                    return trade;
                });
    }

    public Observable<Trade> createSpotTradeObservable() {
        return marketEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").contains("usd_trades_v1"))
                .flatMapIterable(j -> j.getJsonArray("data"), (o, v) -> new JsonData(o.getString("channel"), v))
                .filter(d -> d.value.getValueType().equals(JsonValue.ValueType.ARRAY))
                .map(j -> {
                    lastTrade = System.currentTimeMillis();

                    JsonArray a = (JsonArray) j.value;

                    Trade trade = new Trade();

                    trade.setTradeId(a.getString(0));
                    trade.setExchangeType(ExchangeType.OKCOIN_SPOT);
                    trade.setSymbol(getSymbol(j.channel));
                    trade.setPrice(new BigDecimal(a.getString(1)));
                    trade.setAmount(new BigDecimal(a.getString(2)));
                    trade.setDate(Date.from(LocalTime.parse(a.getString(3)).atDate(now()).toInstant(ofHours(8))));
                    trade.setOrderType(OrderType.valueOf(a.getString(4).toUpperCase()));

                    return trade;
                });
    }

    //DEPTH

    public Observable<Depth> createFutureDepthObservable() {
        return marketEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").contains("_future_depth_"))
                .map(j -> new JsonData(j.getString("channel"), j.getJsonObject("data")))
                .filter(j -> j != null && j.value != null)
                .map(j -> {
                    Depth depth = new Depth();

                    depth.setExchangeType(ExchangeType.OKCOIN_FUTURES);
                    depth.setSymbol(getSymbol(j.channel));
                    depth.setSymbolType(getSymbolType(j.channel));
                    depth.setDate(new Date(Long.parseLong(((JsonObject) j.value).getString("timestamp"))));
                    depth.setCreated(new Date());

                    ((JsonObject) j.value).getJsonArray("asks").forEach(ask ->
                            depth.getAskMap().put(((JsonArray) ask).getJsonNumber(0).bigDecimalValue(),
                                    ((JsonArray) ask).getJsonNumber(1).bigDecimalValue()));

                    ((JsonObject) j.value).getJsonArray("bids").forEach(bid ->
                            depth.getBidMap().put(((JsonArray) bid).getJsonNumber(0).bigDecimalValue(),
                                    ((JsonArray) bid).getJsonNumber(1).bigDecimalValue()));

                    return depth;
                });
    }

    public Observable<Depth> createSpotDepthObservable() {
        return marketEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").contains("usd_depth"))
                .map(j -> new JsonData(j.getString("channel"), j.getJsonObject("data")))
                .filter(j -> j != null && j.value != null)
                .map(j -> {
                    Depth depth = new Depth();

                    depth.setExchangeType(ExchangeType.OKCOIN_SPOT);
                    depth.setSymbol(getSymbol(j.channel));
                    depth.setDate(new Date(Long.parseLong(((JsonObject) j.value).getString("timestamp"))));
                    depth.setCreated(new Date());

                    ((JsonObject) j.value).getJsonArray("asks").forEach(ask ->
                            depth.getAskMap().put(((JsonArray) ask).getJsonNumber(0).bigDecimalValue(),
                                    ((JsonArray) ask).getJsonNumber(1).bigDecimalValue()));

                    ((JsonObject) j.value).getJsonArray("bids").forEach(bid ->
                            depth.getBidMap().put(((JsonArray) bid).getJsonNumber(0).bigDecimalValue(),
                                    ((JsonArray) bid).getJsonNumber(1).bigDecimalValue()));

                    return depth;
                });
    }

    //ORDER INFO

    public Observable<Order> createFutureOrderObservable() {
        return tradingEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").equals("ok_futureusd_order_info") && j.getJsonObject("data") != null)
                .map(j -> j.getJsonObject("data"))
                .filter(j -> j.getBoolean("result"))
                .flatMapIterable(j -> j.getJsonArray("orders"))
                .filter(j -> j.getValueType().equals(JsonValue.ValueType.OBJECT)).map(j -> (JsonObject) j)
                .map(j -> {
                    Order order = new Order();

                    order.setExchangeType(ExchangeType.OKCOIN_FUTURES);
                    order.setAmount(BigDecimal.valueOf(j.getJsonNumber("amount").doubleValue()));
                    order.setCreated(new Date(j.getJsonNumber("create_date").longValue()));
                    order.setFilledAmount(j.getJsonNumber("deal_amount").bigDecimalValue());
                    order.setFee(BigDecimal.valueOf(j.getJsonNumber("fee").doubleValue()));
                    order.setOrderId(j.getJsonNumber("order_id").toString());
                    order.setPrice(BigDecimal.valueOf(j.getJsonNumber("price").doubleValue()));
                    order.setAvgPrice(BigDecimal.valueOf(j.getJsonNumber("price_avg").doubleValue()));

                    String symbol = j.getString("symbol");
                    if (symbol.contains("ltc")) {
                        order.setSymbol("LTC/USD");
                    } else if (symbol.contains("btc")) {
                        order.setSymbol("BTC/USD");
                    }

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
                });
    }

    public Observable<Order> createSpotOrderObservable() {
        return tradingEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").equals("ok_spotusd_order_info") && j.getJsonObject("data") != null)
                .map(j -> j.getJsonObject("data"))
                .filter(j -> j.getBoolean("result"))
                .flatMapIterable(j -> j.getJsonArray("orders"))
                .filter(j -> j.getValueType().equals(JsonValue.ValueType.OBJECT)).map(j -> (JsonObject) j)
                .map(j -> {
                    Order order = new Order();

                    order.setExchangeType(ExchangeType.OKCOIN_SPOT);
                    order.setAmount(BigDecimal.valueOf(j.getJsonNumber("amount").doubleValue()));
                    order.setCreated(new Date(j.getJsonNumber("create_date").longValue()));
                    order.setFilledAmount(j.getJsonNumber("deal_amount").bigDecimalValue());
                    order.setOrderId(j.getJsonNumber("order_id").toString());
                    order.setPrice(BigDecimal.valueOf(j.getJsonNumber("price").doubleValue()));
                    order.setAvgPrice(BigDecimal.valueOf(j.getJsonNumber("avg_price").doubleValue()));

                    String symbol = j.getString("symbol");
                    if (symbol.contains("ltc")) {
                        order.setSymbol("LTC/USD");
                    } else if (symbol.contains("btc")) {
                        order.setSymbol("BTC/USD");
                    }

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

                    switch (j.getString("type")) {
                        case "buy":
                        case "buy_market ":
                            order.setType(OrderType.BID);
                            break;
                        case "sell":
                        case "sell_market":
                            order.setType(OrderType.ASK);
                            break;
                    }

                    return order;
                });
    }

    //REAL TRADES

    public Observable<Order> createFutureRealTrades(){
        return tradingEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").equals("ok_usd_future_realtrades"))
                .map(j -> j.getJsonObject("data"))
                .filter(j -> j != null)
                .map(j -> {
                    lastOrder = System.currentTimeMillis();

                    Order order = new Order();
                    order.setExchangeType(ExchangeType.OKCOIN_FUTURES);

                    order.setAmount(new BigDecimal(j.getString("amount")));
                    order.setOrderId(j.getJsonNumber("orderid").toString());
                    order.setPrice(new BigDecimal(j.getString("price")));
                    order.setAvgPrice(new BigDecimal(j.getString("price_avg")));
                    order.setFee(new BigDecimal(j.getString("fee")));
                    order.setFilledAmount(new BigDecimal(j.getString("deal_amount")));

                    String symbol = j.getString("contract_name");
                    if (symbol.contains("LTC")) {
                        order.setSymbol("LTC/USD");
                    } else if (symbol.contains("BTC")) {
                        order.setSymbol("BTC/USD");
                    }

                    switch (j.getString("status")) {
                        case "-1":
                        case "4":
                            order.setStatus(OrderStatus.CANCELED);
                            break;
                        case "2":
                            order.setStatus(OrderStatus.CLOSED);
                            break;
                        default:
                            order.setStatus(OrderStatus.OPEN);
                            break;
                    }

                    switch (j.getString("type")) {
                        case "1":
                            order.setType(OrderType.OPEN_LONG);
                            break;
                        case "2":
                            order.setType(OrderType.OPEN_SHORT);
                            break;
                        case "3":
                            order.setType(OrderType.CLOSE_LONG);
                            break;
                        case "4":
                            order.setType(OrderType.CLOSE_SHORT);
                            break;
                    }

                    switch (j.getString("contract_type")) {
                        case "this_week":
                            order.setSymbolType(SymbolType.THIS_WEEK);
                            break;
                        case "next_week":
                            order.setSymbolType(SymbolType.NEXT_WEEK);
                            break;
                        case "quarter":
                            order.setSymbolType(SymbolType.QUARTER);
                            break;
                    }

                    return order;
                });

    }

    public Observable<Order> createSpotRealTrades(){
        return tradingEndpoint.getJsonObservable()
                .filter(j -> j.getString("channel", "").equals("ok_usd_realtrades") && j.getJsonObject("data") != null)
                .map(j -> j.getJsonObject("data"))
                .map(j -> {
                    lastOrder = System.currentTimeMillis();

                    Order order = new Order();

                    order.setExchangeType(ExchangeType.OKCOIN_SPOT);
                    order.setAmount(new BigDecimal(j.getString("tradeAmount")));
                    order.setOrderId(j.getJsonNumber("orderId").toString());
                    order.setPrice(new BigDecimal(j.getString("tradePrice")));
                    order.setAvgPrice(new BigDecimal(j.getString("averagePrice")));
                    order.setFilledAmount(new BigDecimal(j.getString("completedTradeAmount")));

                    String symbol = j.getString("symbol");
                    if (symbol.contains("ltc")) {
                        order.setSymbol("LTC/USD");
                    } else if (symbol.contains("btc")) {
                        order.setSymbol("BTC/USD");
                    }

                    switch (j.getJsonNumber("status").intValue()) {
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

                    switch (j.getString("tradeType")) {
                        case "buy":
                            order.setType(OrderType.BID);
                            break;
                        case "sell":
                            order.setType(OrderType.ASK);
                            break;
                    }

                    return order;
                });

    }

    //REQUEST

    public void orderFutureInfo(String apiKey, String secretKey, Order order){
        orderFutureInfo(apiKey, secretKey, toSymbol(order.getSymbol()), order.getOrderId(),
                toContractName(order.getSymbolType()), "2", "1", "1");
    }

    public void orderFutureInfo(String apiKey, String secretKey, String symbol, String orderId, String contractType,
                                String status, String currentPage, String pageLength){
        try {
            JsonObject parameters = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .add("contract_type", contractType)
                    .add("current_page", currentPage)
                    .add("order_id", orderId)
                    .add("page_length", pageLength)
                    .add("status", status)
                    .add("symbol", symbol)
                    .build();

            JsonObject parametersSing = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .add("contract_type", contractType)
                    .add("current_page", currentPage)
                    .add("order_id", orderId)
                    .add("page_length", pageLength)
                    .add("status", status)
                    .add("symbol", symbol)
                    .add("sign", getMD5String(parameters, secretKey))
                    .build();

            tradingEndpoint.getSession().getBasicRemote().sendText(Json.createObjectBuilder()
                    .add("event", "addChannel")
                    .add("channel", "ok_futureusd_order_info")
                    .add("parameters", parametersSing).build().toString());
        } catch (Exception e) {
            log.error("order info error", e);
        }
    }

    public void orderSpotInfo(String apiKey, String secretKey, Order order){
        try {
            String orderId = order.getOrderId();
            String symbol = toSymbol(order.getSymbol());

            JsonObject parameters = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .add("order_id",orderId)
                    .add("symbol", symbol)
                    .build();

            JsonObject parametersSing = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .add("order_id", orderId)
                    .add("symbol", symbol)
                    .add("sign", getMD5String(parameters, secretKey))
                    .build();

            tradingEndpoint.getSession().getBasicRemote().sendText(Json.createObjectBuilder()
                    .add("event", "addChannel")
                    .add("channel", "ok_spotusd_order_info")
                    .add("parameters", parametersSing).build().toString());
        } catch (Exception e) {
            log.error("order info error", e);
        }
    }

    public void realFutureTrades(String apiKey, String secretKey){
        try {
            JsonObject parameters = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .build();

            JsonObject parametersSing = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .add("sign", getMD5String(parameters, secretKey))
                    .build();

            String channel = Json.createObjectBuilder()
                    .add("event", "addChannel")
                    .add("channel", "ok_usd_future_realtrades")
                    .add("parameters", parametersSing).build().toString();

            if (!tradesChannels.contains(channel)) {
                tradingEndpoint.getSession().getBasicRemote().sendText(channel);

                tradesChannels.add(channel);
            }
        } catch (Exception e) {
            log.error("real trades error", e);
        }
    }

    public void realSpotTrades(String apiKey, String secretKey){
        try {
            JsonObject parameters = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .build();

            JsonObject parametersSing = Json.createObjectBuilder()
                    .add("api_key", apiKey)
                    .add("sign", getMD5String(parameters, secretKey))
                    .build();

            String channel = Json.createObjectBuilder()
                    .add("event", "addChannel")
                    .add("channel", "ok_usd_realtrades")
                    .add("parameters", parametersSing).build().toString();

            if (!tradesChannels.contains(channel)) {
                tradingEndpoint.getSession().getBasicRemote().sendText(channel);

                tradesChannels.add(channel);
            }
        } catch (Exception e) {
            log.error("real trades error", e);
        }
    }

    //UTIL

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

    public String toSymbol(String symbol){
        if (symbol.equals("BTC/USD")){
            return "btc_usd";
        }else if (symbol.equals("LTC/USD")){
            return "ltc_usd";
        }

        return null;
    }

    public String toContractName(SymbolType symbolType){
        switch (symbolType){
            case THIS_WEEK: return "this_week";
            case NEXT_WEEK: return "next_week";
            case QUARTER: return "quarter";
        }

        return null;
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

    public void start(){

    }

    public void destroy(){
        destroy = true;
        try {
            marketEndpoint.getSession().close();
        } catch (IOException e) {
            log.error("error close connection -> ", e);
        }
    }
}
