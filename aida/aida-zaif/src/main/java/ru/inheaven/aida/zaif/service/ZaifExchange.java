package ru.inheaven.aida.zaif.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.jersey.client.rx.rxjava2.RxFlowableInvoker;
import org.glassfish.jersey.client.rx.rxjava2.RxFlowableInvokerProvider;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.logging.LoggingFeature;
import org.glassfish.tyrus.client.ClientManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.zaif.model.*;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import javax.websocket.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URI;
import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Anatoly A. Ivanov
 * Date: 08.08.2017.
 */
public class ZaifExchange {
    private Logger log = LoggerFactory.getLogger(ZaifExchange.class);

    private final static String ws = "wss://ws.zaif.jp/stream";
    private final static String api = "https://api.zaif.jp/tapi";
    private final static String key = "425ad4bf-29ef-4c34-b30d-6b1cb3fce02f";
    private final static String secret = "d74638e3-aeba-4f3b-89b8-4dc6c6e9f18b";


    private AtomicLong nonce = new AtomicLong(makeAutoNonce());

    public ZaifExchange() throws URISyntaxException, IOException, DeploymentException {
//        trade("btc_jpy", "ask", new BigDecimal("376925"), new BigDecimal("0.0001"), null, "hello");
        cancel("123");

        try {
            new CountDownLatch(1).await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private Client getClient(){
        return ClientBuilder.newBuilder()
                .register(ObjectMapper.class)
                .register(JacksonFeature.class)
                .register(RxFlowableInvokerProvider.class)
                .register(LoggingFeature.class)
                .build();
    }

    private void cancel(String orderId){
        String message = String.format("nonce=%d&method=cancel_order&order_id=%s", nonce.incrementAndGet(), orderId);

        getClient().target(api)
                .request()
                .header("key", key)
                .header("sign", makeSignedHex(message))
                .rx(RxFlowableInvoker.class)
                .post(Entity.entity(message, MediaType.APPLICATION_FORM_URLENCODED_TYPE), CancelResponse.class)
                .subscribe(c -> {
                    if (c.getSuccess() == 1){
                        log.info(c.getCancel().getFunds().toString());
                    }else{
                        log.error(c.getError());
                    }
                });

    }

    private void trade(String currencyPair, String action, BigDecimal price, BigDecimal amount, BigDecimal limit, String comment){
        String message = String.format("nonce=%d&method=trade&currency_pair=%s&action=%s&price=%s&amount=%s",
                nonce.incrementAndGet(), currencyPair, action, price, amount);

        if (limit != null){
            message += String.format("&limit=%s",limit);
        }

        if (comment != null){
            message += "&comment=" + comment;
        }

        getClient().target(api)
                .request()
                .header("key", key)
                .header("sign", makeSignedHex(message))
                .rx(RxFlowableInvoker.class)
                .post(Entity.entity(message, MediaType.APPLICATION_FORM_URLENCODED_TYPE), TradeResponse.class)
                .subscribe(t -> {
                    if (t.getSuccess() == 1){
                        System.out.println(t.getTrade().getFunds());
                    }else{
                        System.err.println(t.getError());
                    }
                });

    }

    private void startInfo(){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()-> {
            String message = String.format("nonce=%d&method=get_info", nonce.incrementAndGet());

            getClient().target(api)
                    .request()
                    .header("key", key)
                    .header("sign", makeSignedHex(message))
                    .rx(RxFlowableInvoker.class)
                    .post(Entity.entity(message, MediaType.APPLICATION_FORM_URLENCODED_TYPE), InfoResponse.class)
                    .subscribe(i-> System.out.println(i.getInfo().getFunds()));
        },0, 1, TimeUnit.SECONDS);
    }

    private void startOrders(){
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(()-> {
            String message = String.format("nonce=%d&method=active_orders", nonce.incrementAndGet());

            getClient().target(api)
                    .request()
                    .header("key", key)
                    .header("sign", makeSignedHex(message))
                    .rx(RxFlowableInvoker.class)
                    .post(Entity.entity(message, MediaType.APPLICATION_FORM_URLENCODED_TYPE), OrderResponse.class)
                    .subscribe(o -> System.out.println(o.getOrders()));
        },0, 1, TimeUnit.SECONDS);
    }

    private static long makeAutoNonce(){
        try {
            long base_date = (new SimpleDateFormat("yyyy-MM-dd")).parse("2017-01-01").getTime();
            return (System.currentTimeMillis()-base_date)/1000;
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

//    ZAIF
//    key: d707744c-15a7-409e-854b-ab55f8f6c66a
//    secret: 6356cf76-7a1a-4190-9fbc-32c929fe5fc6

    private String makeSignedHex(String message){
        SecretKeySpec signingKey = new SecretKeySpec(secret.getBytes(),"HmacSHA512");
        Mac mac;
        try {
            mac = Mac.getInstance("HmacSHA512");
            mac.init(signingKey);
            StringBuilder sb = new StringBuilder();

            byte[] rawHmac = mac.doFinal(message.getBytes());

            for (byte b :rawHmac) {
                String hex = String.format("%02x", b);
                sb.append(hex);
            }

            return sb.toString();
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException(e);
        }
    }

    private void startStream(){
        ObjectMapper objectMapper = new ObjectMapper();

        try {
            ClientManager.createClient().connectToServer(new Endpoint() {

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    session.addMessageHandler(new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage(String message) {
                            try {
                                List<Stream> stream = objectMapper.readValue('[' + message + ']', new TypeReference<List<Stream>>() { });

                                System.out.println(stream);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            }, new URI(ws + "?currency_pair=btc_jpy"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws DeploymentException, IOException, URISyntaxException {
        new ZaifExchange();
    }
}
