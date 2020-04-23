package ru.inheaven.aida.okex.ws;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.okex.model.*;

import javax.inject.Singleton;
import javax.websocket.*;
import java.math.BigDecimal;
import java.net.URI;
import java.security.SecureRandom;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

/**
 * @author Anatoly A. Ivanov
 * 09.12.2017 19:54
 */
@Singleton
public class OkexWsExchange {
    private Logger log = LoggerFactory.getLogger(OkexWsExchange.class);

    private String API = "8ff1dce3-8cd5-4a3f-bd79-288fd0d665b1";
    private String SECRET = "E6E419E1D9184ABC5184D14F600EF578";

    private FlowableProcessor<Trade> trades = PublishProcessor.create();
    private FlowableProcessor<Depth> depths = PublishProcessor.create();
    private FlowableProcessor<Order> orders = PublishProcessor.create();
    private FlowableProcessor<Position> positions = PublishProcessor.create();
    private FlowableProcessor<Info> infos = PublishProcessor.create();

    private Session session;

    private ObjectMapper objectMapper = new ObjectMapper();

    public OkexWsExchange() {
        ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();
        ClientManager client = ClientManager.createClient();

        client.getProperties().put(ClientProperties.RETRY_AFTER_SERVICE_UNAVAILABLE, true);

        ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {

            private int counter = 0;

            @Override
            public boolean onDisconnect(CloseReason closeReason) {
                System.out.println("### Reconnecting... (reconnect count: " + ++counter + ")");
                return true;
            }

            @Override
            public boolean onConnectFailure(Exception exception) {
                System.out.println("### Reconnecting... (reconnect count: " + ++counter + ") " + exception.getMessage());
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return true;
            }

            @Override
            public long getDelay() {
                return 1;
            }
        };

        client.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler);

        try {
            client.connectToServer(new Endpoint() {
                private AtomicLong ping = new AtomicLong(System.currentTimeMillis());

                {
                    //noinspection Duplicates
                    Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                        if (session != null){
                            try {
                                session.getBasicRemote().sendText("{'event':'ping'}");

                                if (System.currentTimeMillis() - ping.get() > 30000){
                                    init();

                                    ping.set(System.currentTimeMillis());

                                    log.warn("ping > 30s");
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }                        }

                    }, 0, 1, TimeUnit.SECONDS);
                }

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    OkexWsExchange.this.session = session;

                    session.addMessageHandler(new MessageHandler.Whole<String>() {
                        @Override
                        public void onMessage(String message) {
                            try {
                                if (message.length() <= 16 && message.contains("pong")){
                                    ping.set(System.currentTimeMillis());

                                    return;
                                }

                                objectMapper.readTree(message).forEach(n -> {
                                    JsonNode channelNode = n.get("channel");

                                    if (channelNode != null){
                                        String channel = channelNode.textValue();

                                        if (channel.contains("ok_sub_futureusd_btc_trade")){
                                            String symbol = getSymbol(channel);
                                            String currency = getCurrency(channel);

                                            n.get("data").forEach(d -> {
                                                Trade trade = new Trade();

                                                trade.setSymbol(symbol);
                                                trade.setCurrency(currency);

                                                trade.setOrderId(d.get(0).asLong());
                                                trade.setPrice(new BigDecimal(d.get(1).asText()));
                                                trade.setQty(d.get(2).asInt());
                                                trade.setOrigTime(getDate(d.get(3).asText()));
                                                trade.setSide(d.get(4).asText());
                                                trade.setCreated(new Date());

                                                trades.onNext(trade);
                                            });
                                        }else if (channel.contains("ok_sub_futureusd_btc_depth")){
                                            Depth depth = new Depth();

                                            depth.setCurrency(getCurrency(channel));
                                            depth.setSymbol(getSymbol(channel));

                                            ArrayNode asks = (ArrayNode) n.get("data").get("asks");
                                            ArrayNode bids = (ArrayNode) n.get("data").get("bids");

                                            depth.setAsk(new BigDecimal(asks.get(asks.size()-1).get(0).asText()));
                                            depth.setBid(new BigDecimal(bids.get(0).get(0).asText()));

                                            depths.onNext(depth);
                                        }else if (channel.equals("ok_sub_futureusd_userinfo")){
                                            JsonNode data = n.get("data");

                                            Info info = new Info();

                                            info.setCurrency(data.get("symbol").asText());
                                            info.setBalance(new BigDecimal(data.get("balance").asText()));
                                            info.setProfit(new BigDecimal(data.get("profit_real").asText()));
                                            info.setMargin(new BigDecimal(data.get("keep_deposit").asText()));

                                            infos.onNext(info);

                                            log.info(info.toSimpleString());
                                        }else if (channel.equals("ok_sub_futureusd_positions")){
                                            JsonNode data = n.get("data");

                                            data.get("positions").forEach(d -> {
                                                Position position = new Position();

                                                position.setCurrency(data.get("symbol").asText());

                                                position.setSymbol(getContractType(d.get("contract_name").asText()));
                                                position.setPositionId(d.get("position_id").asText());
                                                position.setAvgPrice(new BigDecimal(d.get("avgprice").asText()));
                                                position.setEveningUp(new BigDecimal(d.get("eveningup").asText()));
                                                position.setQty(d.get("hold_amount").asInt());
                                                position.setProfit(new BigDecimal(d.get("realized").asText()));
                                                position.setMarginCash(new BigDecimal(d.get("margin").asText()));

                                                //position(string): position 1 long 2 short
                                                switch (d.get("position").asInt()){
                                                    case 1:
                                                        position.setType("long");
                                                        break;
                                                    case 2:
                                                        position.setType("short");
                                                        break;
                                                    default:
                                                        position.setType(d.get("position").asText());
                                                }

                                                positions.onNext(position);

                                                log.info(position.toSimpleString());
                                            });
                                        }else if (channel.equals("ok_sub_futureusd_trades")){
                                            Order order = getOrder(n.get("data"));

                                            orders.onNext(order);

                                            log.info(order.toSimpleString());
                                        }else if (channel.equals("ok_futureusd_orderinfo")){
                                            n.get("data").get("orders").forEach(o -> {
                                                Order order = getOrder(o);

                                                orders.onNext(order);

                                                log.info(order.toSimpleString());
                                            });
                                        }else{
                                            log.info(message);
                                        }
                                    }else{
                                        if (n.get("base") == null) {
                                            log.error(message);
                                        }
                                    }
                                });
                            } catch (Exception e) {
                                log.error("error json {}", message, e);
                            }

                        }
                    });

                    init();

                }

                private void init(){
                    try {
                        login();
                        session.getBasicRemote().sendText("{'event':'addChannel','channel':'ok_sub_futureusd_btc_trade_this_week'}");
                        session.getBasicRemote().sendText("{'event':'addChannel','channel':'ok_sub_futureusd_btc_trade_next_week'}");
                        session.getBasicRemote().sendText("{'event':'addChannel','channel':'ok_sub_futureusd_btc_trade_quarter'}");

                        session.getBasicRemote().sendText("{'event':'addChannel','channel':'ok_sub_futureusd_btc_depth_this_week_5'}");
                        session.getBasicRemote().sendText("{'event':'addChannel','channel':'ok_sub_futureusd_btc_depth_next_week_5'}");
                        session.getBasicRemote().sendText("{'event':'addChannel','channel':'ok_sub_futureusd_btc_depth_quarter_5'}");

                        getOrderInfo("this_week", "btc_usd", 1);
                        getOrderInfo("next_week", "btc_usd", 1);
                        getOrderInfo("quarter", "btc_usd", 1);


                    } catch (Exception e) {
                        log.error("error sent text", e);
                    }
                }
            }, config, new URI("wss://real.okex.com:10440/websocket/okexapi"));
        } catch (Exception e) {
            log.error("error client", e);
        }

        Executors.newSingleThreadScheduledExecutor().schedule(() -> {
            Stream.of("this_week", "next_week", "quarter").forEach(symbol -> {
                try {
                    futureTrade("btc_usd", symbol, null, 1, 1, 1, 20);
                    futureTrade("btc_usd", symbol, null, 1, 2, 1, 20);
                    Thread.sleep(5000);
                    futureTrade("btc_usd", symbol, null, 1, 3, 1, 20);
                    futureTrade("btc_usd", symbol, null, 1, 4, 1, 20);
                } catch (Exception e) {
                    log.error("aida-init create order", e);
                }
            });
        }, 60, TimeUnit.SECONDS);
    }

    private Order getOrder(JsonNode data) {
        Order order = new Order();

        if (data.get("orderid") != null) {
            order.setOrderId(data.get("orderid").asText());
        }
        if (data.get("order_id") != null) {
            order.setOrderId(data.get("order_id").asText());
        }

        order.setQty(data.get("amount").asInt());
        order.setTotalQty(data.get("deal_amount").asInt());
        order.setCommission(new BigDecimal(data.get("fee").asText()));
        if (data.get("contract_type") != null) {
            order.setSymbol(data.get("contract_type").asText());
        }else{
            order.setSymbol(getContractType(data.get("contract_name").asText()));
        }
        order.setCurrency(getCurrency(data.get("contract_name").asText()));
        order.setPrice(new BigDecimal(data.get("price").asText()));
        order.setAvgPrice(new BigDecimal(data.get("price_avg").asText()));

        //-1 = cancelled, 0 = unfilled, 1 = partially filled, 2 = fully filled,
        // 4 = cancel request in process
        switch (data.get("status").asInt()) {
            case 0:
                order.setStatus("new");
                break;
            case 1:
                order.setStatus("partially_filled");
                break;
            case 2:
                order.setStatus("filled");
                break;
            case -1:
                order.setStatus("canceled");
                break;
            case 4:
                order.setStatus("pending_cancel");
                break;
            default:
                order.setStatus(data.get("status").asText());
        }

        //order type 1: open long, 2: open short, 3: close long, 4: close short
        switch (data.get("type").asInt()) {
            case 1:
                order.setType("market");
                break;
            case 3:
                order.setType("stop");
                break;
            case 2:
                order.setType("limit");
                break;
            case 4:
                order.setType("stop_limit");
                break;
            default:
                order.setType(data.get("type").asText());
        }
        return order;
    }

    private String getCurrency(String channel){
        if (channel.toLowerCase().contains("btc")){
            return "btc_usd";
        }

        throw new IllegalArgumentException(channel);
    }

    private String getSymbol(String channel){
        if (channel.contains("this_week")){
            return "this_week";
        }else if (channel.contains("next_week")){
            return "next_week";
        }else if (channel.contains("quarter")){
            return "quarter";
        }

        throw new IllegalArgumentException(channel);
    }

    private String thisWeekContractName;
    private String nextWeekContractName;

    {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            try {
                LocalDateTime now = LocalDateTime.now();

                LocalDateTime thisWeek, nextWeek;

                if (now.getDayOfWeek().compareTo(DayOfWeek.FRIDAY) > 0 ||
                        (now.getDayOfWeek().compareTo(DayOfWeek.FRIDAY) ==  0 && now.getHour() >= 8)){
                    thisWeek = now.plusDays(5 - now.getDayOfWeek().getValue()).plusWeeks(1);
                    nextWeek = thisWeek.plusWeeks(1);
                }else{
                    thisWeek = now.plusDays(5 - now.getDayOfWeek().getValue());
                    nextWeek = thisWeek.plusWeeks(1);
                }

                String thisWeekContractName = "BTC" + thisWeek.format(DateTimeFormatter.ofPattern("MMdd"));
                String nextWeekContractName = "BTC" + nextWeek.format(DateTimeFormatter.ofPattern("MMdd"));

                if (!thisWeekContractName.equals(this.thisWeekContractName)){
                    this.thisWeekContractName = thisWeekContractName;
                    this.nextWeekContractName = nextWeekContractName;

                    log.info("UPDATE WEEK {} {}", thisWeekContractName, nextWeekContractName);
                }
            } catch (Exception e) {
                log.error("error update week", e);
            }
        }, 0, 1, TimeUnit.MINUTES);
    }

    private String getContractType(String contractName){
        if (contractName.equals(thisWeekContractName)){
            return "this_week";
        }else if (contractName.equals(nextWeekContractName)){
            return "next_week";
        }else if (contractName.equals("BTC0330")){
            return "quarter";
        }

        throw new IllegalArgumentException(contractName);
    }

    private Date getDate(String time){
        return Date.from(LocalTime.parse(time).atDate(LocalDate.now()).toInstant(ZoneOffset.ofHours(8))); //todo
    }

    private void sendMessage(String message){
        try {
            session.getBasicRemote().sendText(message);
        } catch (Exception e) {
            log.error("error send message", e);
        }
    }

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(8);

    private void sendMessage(String message, int delay, TimeUnit timeUnit){
        scheduledExecutorService.schedule(() -> {
            try{
                session.getAsyncRemote().sendText(message);
            } catch (Exception e) {
                log.error("error send message", e);
            }
        }, delay, timeUnit);
    }

    private void login(){
        Map<String, String> preMap = new HashMap<>();

        preMap.put("api_key", API);
        String preStr = MD5Util.createLinkString(preMap);
        preStr = preStr + "&secret_key=" + SECRET;
        String signStr = MD5Util.getMD5String(preStr);
        preMap.put("sign", signStr);

        sendMessage("{'event':'login', 'parameters': " + MD5Util.getParams(preMap) + "}");
    }

    public void futureCancel(String currency, String contractType, String orderId) {

        Map<String, String> preMap = new HashMap<>();

        preMap.put("api_key", API);
        preMap.put("symbol", currency);
        preMap.put("order_id", orderId);
        preMap.put("contract_type", contractType);
        String preStr = MD5Util.createLinkString(preMap);
        preStr = preStr + "&secret_key=" + SECRET;
        String signStr = MD5Util.getMD5String(preStr);
        preMap.put("sign", signStr);

        sendMessage("{'event': 'addChannel','channel': 'ok_futureusd_cancel_order','parameters': " + MD5Util.getParams(preMap) + "}");
    }


    public void futureRealtrades(String apiKey, String secretKey) {
        String signStr = MD5Util.getMD5String("api_key=" + apiKey + "&secret_key=" + secretKey);

        sendMessage("{'event':'addChannel','channel':'ok_sub_futureusd_trades','parameters':{'api_key':'" +
                apiKey + "','sign':'" + signStr +
                "'},'binary':'true'}");
    }

    private SecureRandom secureRandom = new SecureRandom();

    /**
     *
     * @param type  1: open long position 2: open short position 3: liquidate long position 4: liquidate short position
     */
    public void futureTrade(String currency, String contractType, String price, int amount,
                            int type, int matchPrice, int leverRate) {
        Map<String, String> preMap = new HashMap<>();

        preMap.put("api_key", API);
        preMap.put("symbol", currency);
        preMap.put("contract_type", contractType);
        preMap.put("price", price);
        preMap.put("amount", String.valueOf(amount));
        preMap.put("type", String.valueOf(type));
        preMap.put("match_price", String.valueOf(matchPrice));
        preMap.put("lever_rate", String.valueOf(leverRate));
        String preStr = MD5Util.createLinkString(preMap);
        preStr = preStr + "&secret_key=" + SECRET;

        String signStr = MD5Util.getMD5String(preStr);

        preMap.put("sign", signStr);
        String params = MD5Util.getParams(preMap);

        sendMessage("{'event': 'addChannel','channel':'ok_futureusd_trade','parameters':" + params + "}");
    }

    public void getUserInfo() {
        String signStr = MD5Util.getMD5String("api_key=" + API + "&secret_key=" + SECRET);

        sendMessage("{'event':'addChannel','channel':'ok_futureusd_userinfo','parameters':{'api_key':'" + API +
                "','sign':'" + signStr + "'},'binary':'0'}");
    }

    public void getPositions0() {
        String signStr = MD5Util.getMD5String("api_key=" + API + "&secret_key=" + SECRET);

        sendMessage("{'event':'addChannel','channel':'ok_sub_futureusd_positions','parameters':{'api_key':'" + API +
                "','sign':'" + signStr + "'},'binary':'0'}");
    }

    public void getOrderInfo(String contractType, String currency, int page){
        Map<String, String> preMap = new HashMap<>();

        preMap.put("api_key", API);
        preMap.put("symbol", currency);
        preMap.put("contract_type", contractType);
        preMap.put("order_id", "-1");
        preMap.put("status", "1");
        preMap.put("current_page", String.valueOf(page));
        preMap.put("page_length", "50");
        String preStr = MD5Util.createLinkString(preMap);
        preStr = preStr + "&secret_key=" + SECRET;

        String signStr = MD5Util.getMD5String(preStr);

        preMap.put("sign", signStr);
        String params = MD5Util.getParams(preMap);

        sendMessage("{'event': 'addChannel','channel':'ok_futureusd_orderinfo','parameters':" + params + "}");
    }

    public Flowable<Trade> getTrades() {
        return trades.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Depth> getDepths() {
        return depths.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Order> getOrders() {
        return orders.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Info> getInfos() {
        return infos.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Position> getPositions() {
        return positions.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public static void main(String [] args) throws InterruptedException {
        OkexWsExchange okexWsExchange = new OkexWsExchange();
        new CountDownLatch(1).await();

//        System.out.println(LocalDateTime.now().getHour());


    }
}

