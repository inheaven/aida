package ru.inheaven.aida.okex.storage.websocket;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.apache.commons.compress.compressors.deflate64.Deflate64CompressorInputStream;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import ru.inheaven.aida.okex.storage.service.ClusterService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.*;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Anatoly A. Ivanov
 * 09.12.2017 21:51
 */
@SuppressWarnings("Duplicates")
@Singleton
public class OkexWsExchangeStorageNew {
    @Inject
    private ClusterService clusterService;

    private AtomicLong future = new AtomicLong(System.currentTimeMillis());
    private AtomicLong futureIndex = new AtomicLong(0);

    @Inject
    public void init() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - future.get() > 1000*60*10){
                System.out.println("FUTURE WS ERROR " + new Date(future.get()));
            }

            System.out.println(new Date() + " index " + futureIndex.get());
        }, 1, 1, TimeUnit.MINUTES);

        try {
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

            ClientManager client = ClientManager.createClient();
            client.getProperties().put(ClientProperties.RETRY_AFTER_SERVICE_UNAVAILABLE, true);

            ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {

                private int counter = 0;

                @Override
                public boolean onDisconnect(CloseReason closeReason) {
                    System.out.println("### onDisconnect... (reconnect count: " + ++counter + ") " + closeReason.toString());
                    return true;
                }

                @Override
                public boolean onConnectFailure(Exception exception) {
                    System.out.println("### onConnectFailure... (reconnect count: " + ++counter + ") " + exception.getMessage());
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


            List<String> channels = new ArrayList<>();
            channels.add("ok_sub_futureusd_btc_trade_this_week");
            channels.add("ok_sub_futureusd_btc_trade_next_week");
            channels.add("ok_sub_futureusd_btc_trade_quarter");
            channels.add("ok_sub_futureusd_ltc_trade_this_week");
            channels.add("ok_sub_futureusd_ltc_trade_next_week");
            channels.add("ok_sub_futureusd_ltc_trade_quarter");

            FlowableProcessor<String> wsProcessor = PublishProcessor.create();

            client.connectToServer(new Endpoint() {
                private Session session;

                {
                    //noinspection Duplicates
                    Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                        if (session != null){
                            try {
                                session.getBasicRemote().sendText("{'event':'ping'}");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }                        }

                    }, 0, 60, TimeUnit.SECONDS);
                }

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    this.session = session;

                    session.addMessageHandler(new MessageHandler.Whole<ByteBuffer>() {

                        @Override
                        public void onMessage(ByteBuffer byteBuffer) {
                            try {
                                future.set(System.currentTimeMillis());
                                futureIndex.incrementAndGet();

                                wsProcessor.onNext(uncompress(getByteArrayFromByteBuffer(byteBuffer)));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    channels.forEach(c -> {
                        try {
                            session.getBasicRemote().sendText("{'event':'addChannel','channel':'"+ c +"'}");
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            }, config, new URI("wss://real.okex.com:10441/websocket/okexapi?compress=true"));

            //noinspection ResultOfMethodCallIgnored
            wsProcessor
                    .onBackpressureBuffer(100000)
                    .observeOn(Schedulers.single())
                    .filter(message -> !message.contains("{\"event\":\"pong\"}"))
                    .subscribe(message -> {
                        try {
                            clusterService.getSession().execute(
                                    QueryBuilder.insertInto("okex", "market_data_ws")
                                            .value("id", System.currentTimeMillis())
                                            .value("message", message));
                        } catch (Exception e) {
                            System.err.println(e.getMessage());
                        }
                    });


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static byte[] getByteArrayFromByteBuffer(ByteBuffer byteBuffer) {
        byte[] bytesArray = new byte[byteBuffer.remaining()];
        byteBuffer.get(bytesArray, 0, bytesArray.length);
        return bytesArray;
    }

    private static String uncompress(byte[] bytes) {
        try (final ByteArrayOutputStream out = new ByteArrayOutputStream();
             final ByteArrayInputStream in = new ByteArrayInputStream(bytes);
             final Deflate64CompressorInputStream zin = new Deflate64CompressorInputStream(in)) {
            final byte[] buffer = new byte[1024];
            int offset;
            while (-1 != (offset = zin.read(buffer))) {
                out.write(buffer, 0, offset);
            }
            return out.toString();
        } catch (final IOException e) {
            throw new RuntimeException(e);
        }
    }


}
