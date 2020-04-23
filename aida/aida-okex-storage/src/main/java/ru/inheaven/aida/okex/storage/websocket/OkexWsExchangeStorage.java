package ru.inheaven.aida.okex.storage.websocket;

import com.datastax.driver.core.querybuilder.QueryBuilder;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.glassfish.tyrus.client.ClientManager;
import org.glassfish.tyrus.client.ClientProperties;
import ru.inheaven.aida.okex.storage.service.ClusterService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author Anatoly A. Ivanov
 * 09.12.2017 21:51
 */
@SuppressWarnings("Duplicates")
@Singleton
public class OkexWsExchangeStorage {
    @Inject
    private ClusterService clusterService;

    private AtomicLong future = new AtomicLong(System.currentTimeMillis());
    private AtomicLong spot0 = new AtomicLong(System.currentTimeMillis());
    private AtomicLong spot1 = new AtomicLong(System.currentTimeMillis());
    private AtomicLong spot2 = new AtomicLong(System.currentTimeMillis());

    private AtomicLong futureIndex = new AtomicLong(0);
    private AtomicLong spotIndex0 = new AtomicLong(0);
    private AtomicLong spotIndex1 = new AtomicLong(0);
    private AtomicLong spotIndex2 = new AtomicLong(0);


    @Inject
    public void init() {
        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
            if (System.currentTimeMillis() - future.get() > 1000*60*10){
                System.out.println("FUTURE WS ERROR " + new Date(future.get()));
            }

            if (System.currentTimeMillis() - spot0.get() > 1000*60*10){
                System.out.println("SPOT 0 WS ERROR " + new Date(spot0.get()));
            }

            if (System.currentTimeMillis() - spot1.get() > 1000*60*10){
                System.out.println("SPOT 1 WS ERROR " + new Date(spot1.get()));
            }

            if (System.currentTimeMillis() - spot2.get() > 1000*60*10){
                System.out.println("SPOT 2 WS ERROR " + new Date(spot2.get()));
            }

            System.out.println(new Date() + " index " + futureIndex.get()
                    + ", " + spotIndex0.get()
                    + ", " + spotIndex1.get()
                    + ", " + spotIndex2.get());
        }, 1, 1, TimeUnit.MINUTES);

        try {
            ClientEndpointConfig config = ClientEndpointConfig.Builder.create().build();

            ClientManager client = ClientManager.createClient();
            client.getProperties().put(ClientProperties.RETRY_AFTER_SERVICE_UNAVAILABLE, true);

            ClientManager client1 = ClientManager.createClient();
            client1.getProperties().put(ClientProperties.RETRY_AFTER_SERVICE_UNAVAILABLE, true);

            ClientManager client2 = ClientManager.createClient();
            client2.getProperties().put(ClientProperties.RETRY_AFTER_SERVICE_UNAVAILABLE, true);

            ClientManager client3 = ClientManager.createClient();
            client2.getProperties().put(ClientProperties.RETRY_AFTER_SERVICE_UNAVAILABLE, true);

            ClientManager.ReconnectHandler reconnectHandler = new ClientManager.ReconnectHandler() {

                private int counter = 0;

                @Override
                public boolean onDisconnect(CloseReason closeReason) {
                    System.out.println("### Reconnecting... (reconnect count: " + ++counter + ") " + closeReason.toString());
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

            ClientManager.ReconnectHandler reconnectHandler1 = new ClientManager.ReconnectHandler() {

                private int counter = 0;

                @Override
                public boolean onDisconnect(CloseReason closeReason) {
                    System.out.println("###1 Reconnecting... (reconnect count: " + ++counter + ") " + closeReason.toString());
                    return true;
                }

                @Override
                public boolean onConnectFailure(Exception exception) {
                    System.out.println("###1 Reconnecting... (reconnect count: " + ++counter + ") " + exception.getMessage());
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

            ClientManager.ReconnectHandler reconnectHandler2 = new ClientManager.ReconnectHandler() {

                private int counter = 0;

                @Override
                public boolean onDisconnect(CloseReason closeReason) {
                    System.out.println("###2 Reconnecting... (reconnect count: " + ++counter + ") " + closeReason.toString());
                    return true;
                }

                @Override
                public boolean onConnectFailure(Exception exception) {
                    System.out.println("###2 Reconnecting... (reconnect count: " + ++counter + ") " + exception.getMessage());
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

            ClientManager.ReconnectHandler reconnectHandler3 = new ClientManager.ReconnectHandler() {

                private int counter = 0;

                @Override
                public boolean onDisconnect(CloseReason closeReason) {
                    System.out.println("###3 Reconnecting... (reconnect count: " + ++counter + ") " + closeReason.toString());
                    return true;
                }

                @Override
                public boolean onConnectFailure(Exception exception) {
                    System.out.println("###3 Reconnecting... (reconnect count: " + ++counter + ") " + exception.getMessage());
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
            client1.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler1);
            client2.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler2);
            client3.getProperties().put(ClientProperties.RECONNECT_HANDLER, reconnectHandler3);

            List<String> channels = new ArrayList<>();
            channels.add("ok_sub_futureusd_btc_trade_this_week");
            channels.add("ok_sub_futureusd_btc_trade_next_week");
            channels.add("ok_sub_futureusd_btc_trade_quarter");
            channels.add("ok_sub_futureusd_ltc_trade_this_week");
            channels.add("ok_sub_futureusd_ltc_trade_next_week");
            channels.add("ok_sub_futureusd_ltc_trade_quarter");
            channels.add("ok_sub_future_btc_depth_this_week_20");
            channels.add("ok_sub_future_btc_depth_next_week_20");
            channels.add("ok_sub_future_btc_depth_quarter_20");
            channels.add("ok_sub_future_ltc_depth_this_week_20");
            channels.add("ok_sub_future_ltc_depth_next_week_20");
            channels.add("ok_sub_future_ltc_depth_quarter_20");

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

                    }, 0, 1, TimeUnit.SECONDS);
                }

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    this.session = session;

                    session.addMessageHandler(new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage(String message) {
                            try {
                                future.set(System.currentTimeMillis());
                                futureIndex.incrementAndGet();

                                wsProcessor.onNext(message);
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
            }, config, new URI("wss://real.okex.com:10440/websocket/okexapi"));

            //noinspection ResultOfMethodCallIgnored
            wsProcessor
                    .onBackpressureBuffer(100000)
                    .observeOn(Schedulers.single())
                    .subscribe(message -> {
                        try {
                            clusterService.getSession().execute(
                                    QueryBuilder.insertInto("okex", "market_data_ws")
                                            .value("id", System.currentTimeMillis())
                                            .value("message", message));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

            FlowableProcessor<String> wsSpotProcessor = PublishProcessor.create();

            //noinspection ResultOfMethodCallIgnored
            wsSpotProcessor
                    .onBackpressureBuffer(100000)
                    .observeOn(Schedulers.single())
                    .subscribe(message -> {
                        try {
                            clusterService.getSession().execute(
                                    QueryBuilder.insertInto("okex", "market_data_ws_spot")
                                            .value("id", System.currentTimeMillis())
                                            .value("message", message));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });

            client1.connectToServer(new Endpoint() {
                private Session session;

                {
                    //noinspection Duplicates
                    Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                        if (session != null){
                            try {
                                session.getBasicRemote().sendText("{'event':'ping'}");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }                        }

                    }, 0, 1, TimeUnit.SECONDS);
                }

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    this.session = session;

                    session.addMessageHandler(new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage(String message) {
                            try {
                                spot0.set(System.currentTimeMillis());
                                spotIndex0.incrementAndGet();

                                wsSpotProcessor.onNext(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Executors.newSingleThreadExecutor().submit(()->{
                        Arrays.asList(
                                "ltc_btc", "eth_btc", "etc_btc", "bch_btc", "1st_btc", "aac_btc", "abt_btc", "ace_btc",
                                "act_btc", "aidoc_btc", "amm_btc", "ark_btc", "ast_btc", "auto_btc", "avt_btc", "bcd_btc",
                                "bkx_btc", "bnt_btc", "btg_btc", "btm_btc", "can_btc", "cbt_btc", "chat_btc", "cic_btc",
                                "cmt_btc", "cvc_btc", "dadi_btc", "dash_btc", "dent_btc", "dgb_btc", "dgd_btc", "dna_btc",
                                "dnt_btc", "dpy_btc", "edo_btc", "elf_btc", "eng_btc", "enj_btc", "eos_btc", "evx_btc",
                                "fun_btc", "gas_btc", "gnt_btc", "gnx_btc", "gtc_btc", "hot_btc", "hsr_btc", "icn_btc",
                                "icx_btc", "ins_btc", "insur_btc", "int_btc", "iost_btc", "iota_btc", "ipc_btc", "itc_btc",
                                "kcash_btc", "knc_btc", "light_btc", "link_btc", "lrc_btc", "mana_btc", "mco_btc",
                                "mda_btc", "mdt_btc", "mith_btc", "mkr_btc", "mof_btc","mth_btc", "mtl_btc", "nano_btc",
                                "nas_btc", "neo_btc", "nuls_btc", "oax_btc", "of_btc", "okb_btc", "omg_btc", "ont_btc",
                                "ost_btc", "pay_btc", "poe_btc", "ppt_btc", "pra_btc", "pst_btc", "qtum_btc", "qun_btc",
                                "r_btc", "rcn_btc", "rdn_btc", "ren_btc", "rfr_btc", "rnt_btc", "salt_btc", "san_btc",
                                "sbtc_btc", "smt_btc", "snc_btc", "sngls_btc", "snt_btc", "spf_btc", "ssc_btc", "storj_btc",
                                "sub_btc", "swftc_btc", "theta_btc", "tio_btc", "tnb_btc", "topc_btc", "tra_btc", "trio_btc",
                                "true_btc", "trx_btc", "uct_btc", "ugc_btc", "vee_btc", "vib_btc", "wbtc_btc", "wtc_btc",
                                "xem_btc", "xlm_btc", "xmr_btc", "xrp_btc", "xuc_btc", "yee_btc", "yoyo_btc", "zec_btc",
                                "zen_btc", "zip_btc", "zrx_btc")
                                .forEach(s -> addChannels(s, session));
                    });
                }
            }, config, new URI("wss://real.okex.com:10441/websocket"));

            client2.connectToServer(new Endpoint() {
                private Session session;

                {
                    //noinspection Duplicates
                    Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                        if (session != null){
                            try {
                                session.getBasicRemote().sendText("{'event':'ping'}");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }                        }

                    }, 0, 1, TimeUnit.SECONDS);
                }

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    this.session = session;

                    session.addMessageHandler(new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage(String message) {
                            try {
                                spot1.set(System.currentTimeMillis());
                                spotIndex1.incrementAndGet();

                                wsSpotProcessor.onNext(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Executors.newSingleThreadExecutor().submit(()->{
                        Arrays.asList(
                                "btc_usdt", "ltc_usdt", "eth_usdt", "etc_usdt", "bch_usdt", "1st_usdt", "aac_usdt",
                                "abt_usdt", "ace_usdt", "act_usdt", "aidoc_usdt", "amm_usdt", "ark_usdt", "ast_usdt",
                                "auto_usdt", "avt_usdt", "bcd_usdt", "bkx_usdt", "bnt_usdt", "btg_usdt", "btm_usdt",
                                "can_usdt", "cbt_usdt", "chat_usdt", "cic_usdt", "cmt_usdt", "cvc_usdt", "dadi_usdt",
                                "dash_usdt", "dent_usdt", "dgb_usdt", "dgd_usdt", "dna_usdt", "dnt_usdt", "dpy_usdt",
                                "edo_usdt", "elf_usdt", "eng_usdt", "enj_usdt", "eos_usdt", "evx_usdt", "fun_usdt",
                                "gas_usdt", "gnt_usdt", "gnx_usdt", "gtc_usdt", "hot_usdt", "hsr_usdt", "icn_usdt",
                                "icx_usdt", "ins_usdt", "insur_usdt", "int_usdt", "iost_usdt", "iota_usdt", "ipc_usdt",
                                "itc_usdt", "kcash_usdt", "knc_usdt", "light_usdt", "link_usdt", "lrc_usdt", "mana_usdt",
                                "mco_usdt", "mda_usdt", "mdt_usdt", "mith_usdt", "mkr_usdt", "mof_usdt", "mth_usdt",
                                "mtl_usdt", "nano_usdt", "nas_usdt", "neo_usdt", "nuls_usdt", "oax_usdt", "of_usdt",
                                "okb_usdt", "omg_usdt", "ont_usdt", "ost_usdt", "pay_usdt", "poe_usdt", "ppt_usdt",
                                "pra_usdt", "pst_usdt", "qtum_usdt", "qun_usdt", "r_usdt", "rcn_usdt", "rdn_usdt",
                                "ren_usdt", "rfr_usdt", "rnt_usdt", "salt_usdt", "san_usdt", "smt_usdt", "snc_usdt",
                                "sngls_usdt", "snt_usdt", "spf_usdt", "ssc_usdt", "storj_usdt", "sub_usdt", "swftc_usdt",
                                "theta_usdt", "tio_usdt", "tnb_usdt", "topc_usdt", "tra_usdt", "trio_usdt", "true_usdt",
                                "trx_usdt", "uct_usdt", "ugc_usdt", "vee_usdt", "vib_usdt", "wtc_usdt", "xem_usdt",
                                "xlm_usdt", "xmr_usdt", "xrp_usdt", "xuc_usdt","yee_usdt", "yoyo_usdt", "zec_usdt",
                                "zen_usdt", "zip_usdt", "zrx_usdt")
                                .forEach(s -> addChannels(s, session));
                    });
                }
            }, config, new URI("wss://real.okex.com:10441/websocket"));

            client3.connectToServer(new Endpoint() {
                private Session session;

                {
                    //noinspection Duplicates
                    Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                        if (session != null){
                            try {
                                session.getBasicRemote().sendText("{'event':'ping'}");
                            } catch (IOException e) {
                                e.printStackTrace();
                            }                        }

                    }, 0, 1, TimeUnit.SECONDS);
                }

                @Override
                public void onOpen(Session session, EndpointConfig config) {
                    this.session = session;

                    session.addMessageHandler(new MessageHandler.Whole<String>() {

                        @Override
                        public void onMessage(String message) {
                            try {
                                spot2.set(System.currentTimeMillis());
                                spotIndex2.incrementAndGet();

                                wsSpotProcessor.onNext(message);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    });
                    Executors.newSingleThreadExecutor().submit(()->{
                        Arrays.asList(
                                "ltc_eth", "etc_eth", "bch_eth", "1st_eth", "aac_eth", "abt_eth", "ace_eth", "act_eth",
                                "aidoc_eth", "amm_eth", "ark_eth", "ast_eth", "auto_eth", "avt_eth", "bkx_eth", "bnt_eth",
                                "btm_eth", "can_eth", "cbt_eth", "chat_eth", "cic_eth", "cmt_eth", "cvc_eth", "dadi_eth",
                                "dash_eth", "dent_eth", "dgb_eth", "dgd_eth", "dna_eth", "dnt_eth", "dpy_eth", "edo_eth",
                                "elf_eth", "eng_eth", "enj_eth", "eos_eth", "evx_eth", "fun_eth", "gas_eth", "gnt_eth",
                                "gnx_eth", "gtc_eth", "hot_eth", "hsr_eth", "icn_eth", "icx_eth", "ins_eth",
                                "insur_eth", "int_eth", "iost_eth", "iota_eth", "ipc_eth", "itc_eth", "kcash_eth",
                                "knc_eth", "light_eth", "link_eth", "lrc_eth", "mana_eth", "mco_eth", "mda_eth",
                                "mdt_eth", "mith_eth", "mkr_eth", "mof_eth", "mth_eth", "mtl_eth", "nano_eth", "nas_eth",
                                "neo_eth", "nuls_eth", "oax_eth", "of_eth", "okb_eth", "omg_eth", "ont_eth", "ost_eth",
                                "pay_eth", "poe_eth", "ppt_eth", "pra_eth", "pst_eth", "qtum_eth", "qun_eth", "r_eth",
                                "rcn_eth", "rdn_eth", "ren_eth", "rfr_eth", "rnt_eth", "salt_eth", "san_eth", "smt_eth",
                                "snc_eth", "sngls_eth", "snt_eth", "spf_eth", "ssc_eth", "storj_eth", "sub_eth", "swftc_eth",
                                "theta_eth", "tio_eth", "tnb_eth", "topc_eth", "tra_eth", "trio_eth", "true_eth", "trx_eth",
                                "uct_eth", "ugc_eth", "vee_eth", "vib_eth", "wtc_eth", "xem_eth", "xlm_eth", "xmr_eth",
                                "xrp_eth", "xuc_eth", "yee_eth", "yoyo_eth", "zec_eth", "zen_eth", "zip_eth", "zrx_eth")
                                .forEach(s -> addChannels(s, session));
                    });
                }
            }, config, new URI("wss://real.okex.com:10441/websocket"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void addChannels(String symbol, Session session) {
        session.getAsyncRemote().sendText("{'event':'addChannel','channel':'ok_sub_spot_" + symbol + "_deals'}");
        session.getAsyncRemote().sendText("{'event':'addChannel','channel':'ok_sub_spot_" + symbol + "_depth_20'}");
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new OkexWsExchangeStorage().init();

        new CountDownLatch(1).await();
    }
}
