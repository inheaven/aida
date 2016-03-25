package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.fix.OKCoinApplication;
import ru.inheaven.aida.happy.trading.fix.fix44.OKCoinMessageFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import java.io.InputStream;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author inheaven on 21.08.2015 21:19.
 */
public abstract class BaseOkcoinFixService {
    private Logger log = LoggerFactory.getLogger(getClass());
    private OKCoinApplication okCoinApplicationMarket;
    private OKCoinApplication okCoinApplicationTrade;
    private OKCoinApplication okCoinApplicationTrade2;
    private OKCoinApplication okCoinApplicationTrade3;
    private OKCoinApplication okCoinApplicationTrade4;
    private SessionID marketSessionId;
    private SessionID tradeSessionId;
    private SessionID tradeSessionId2;
    private SessionID tradeSessionId3;
    private SessionID tradeSessionId4;
    private PublishSubject<Trade> tradePublishSubject = PublishSubject.create();
    private PublishSubject<Order> orderPublishSubject = PublishSubject.create();
    private PublishSubject<Depth> depthPublishSubject = PublishSubject.create();

    private AtomicLong index = new AtomicLong(4);

    private Deque<Order> orderQueue = new ConcurrentLinkedDeque<>();

    public BaseOkcoinFixService(ExchangeType exchangeType, String apiKey, String secretKey, String marketConfig,
                                String tradeConfig, String tradeConfig2, String tradeConfig3, String tradeConfig4,
                                List<String> symbols) {
        //push order
//        Executors.newScheduledThreadPool(4)
//                .scheduleWithFixedDelay(() -> {
//                    try {
//                        if (!orderQueue.isEmpty()){
//                            internalPlaceLimitOrder(orderQueue.poll());
//                        }
//                    } catch (Exception e) {
//                        log.error("error push order", e);
//                    }
//                }, 0, 150, TimeUnit.MILLISECONDS);

        //MARKET
        okCoinApplicationMarket = new OKCoinApplication(exchangeType, apiKey, secretKey){
            @Override
            public void onLogon(SessionID sessionId) {
                BaseOkcoinFixService.this.marketSessionId = sessionId;

                symbols.forEach(s -> {
                    requestLiveTrades(UUID.randomUUID().toString(), s, sessionId);
                    requestOrderBook(UUID.randomUUID().toString(), s, sessionId);
                });
            }

            @Override
            public void onCreate(SessionID sessionId) {
                BaseOkcoinFixService.this.marketSessionId = sessionId;

                symbols.forEach(s -> {
                    requestLiveTrades(UUID.randomUUID().toString(), s, sessionId);
                    requestOrderBook(UUID.randomUUID().toString(), s, sessionId);
                });
            }

            @Override
            protected void onTrade(Trade trade) {
                tradePublishSubject.onNext(trade);
            }

            @Override
            protected void onDepth(Depth depth) {
                depthPublishSubject.onNext(depth);
            }

            @Override
            protected void onOrder(Order order) {
                if (tradeSessionId == null){
                    orderPublishSubject.onNext(order);
                }
            }
        };
        init(okCoinApplicationMarket, marketConfig);

        //TRADE
        okCoinApplicationTrade = new OKCoinApplication(exchangeType, apiKey, secretKey){
            @Override
            public void onLogon(SessionID sessionId) {
                BaseOkcoinFixService.this.tradeSessionId = sessionId;
            }

            @Override
            public void onCreate(SessionID sessionId) {
                BaseOkcoinFixService.this.tradeSessionId = sessionId;
            }

            @Override
            protected void onOrder(Order order) {
                orderPublishSubject.onNext(order);
            }
        };
        init(okCoinApplicationTrade, tradeConfig);

        if (tradeConfig2 != null){
            okCoinApplicationTrade2 = new OKCoinApplication(exchangeType, apiKey, secretKey){
                @Override
                public void onLogon(SessionID sessionId) {
                    BaseOkcoinFixService.this.tradeSessionId2 = sessionId;
                }

                @Override
                public void onCreate(SessionID sessionId) {
                    BaseOkcoinFixService.this.tradeSessionId2 = sessionId;
                }

                @Override
                protected void onOrder(Order order) {
                    orderPublishSubject.onNext(order);
                }
            };
            init(okCoinApplicationTrade2, tradeConfig2);
        }

        if (tradeConfig3 != null){
            okCoinApplicationTrade3 = new OKCoinApplication(exchangeType, apiKey, secretKey){
                @Override
                public void onLogon(SessionID sessionId) {
                    BaseOkcoinFixService.this.tradeSessionId3 = sessionId;
                }

                @Override
                public void onCreate(SessionID sessionId) {
                    BaseOkcoinFixService.this.tradeSessionId3 = sessionId;
                }

                @Override
                protected void onOrder(Order order) {
                    orderPublishSubject.onNext(order);
                }
            };
            init(okCoinApplicationTrade3, tradeConfig3);
        }

        if (tradeConfig4 != null){
            okCoinApplicationTrade4 = new OKCoinApplication(exchangeType, apiKey, secretKey){
                @Override
                public void onLogon(SessionID sessionId) {
                    BaseOkcoinFixService.this.tradeSessionId4 = sessionId;
                }

                @Override
                public void onCreate(SessionID sessionId) {
                    BaseOkcoinFixService.this.tradeSessionId4 = sessionId;
                }

                @Override
                protected void onOrder(Order order) {
                    orderPublishSubject.onNext(order);
                }
            };
            init(okCoinApplicationTrade4, tradeConfig4);
        }
    }

    private void init(OKCoinApplication application, String config){
        if(config == null){
            return;
        }

        try {
            SessionSettings settings;
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(config)) {
                settings = new SessionSettings(inputStream);
            }

            MessageStoreFactory storeFactory = new MemoryStoreFactory();
            LogFactory logFactory = !config.contains("market") ? new FileLogFactory(settings) : null;
            MessageFactory messageFactory = new OKCoinMessageFactory();
            ThreadedSocketInitiator initiator = new ThreadedSocketInitiator(application, storeFactory, settings, logFactory, messageFactory);
            initiator.start();
        } catch (Exception e) {
            log.error("error init okcoin fix");
        }
    }

    public void placeLimitOrder(Account account, Order order){
//        orderQueue.add(order);
        internalPlaceLimitOrder(order);
    }

    private void internalPlaceLimitOrder(Order order){
        SessionID sessionID = getTradeSessionId();
        OKCoinApplication application = okCoinApplicationTrade;

        if (tradeSessionId2 != null && tradeSessionId3 != null && tradeSessionId4 != null) {
            switch ((int) index.incrementAndGet() % 4){
                case 0:
                    sessionID = tradeSessionId;
                    application = okCoinApplicationTrade;
                    break;
                case 1:
                    sessionID = tradeSessionId2;
                    application = okCoinApplicationTrade2;
                    break;
                case 2:
                    sessionID = tradeSessionId3;
                    application = okCoinApplicationTrade3;
                    break;
                case 3:
                    sessionID = tradeSessionId4;
                    application = okCoinApplicationTrade4;
                    break;
            }
        }

        application.placeOrder(order, sessionID);
    }

    private SessionID getTradeSessionId(){
        return tradeSessionId != null ? tradeSessionId : marketSessionId;
    }

    public void cancelOrder(Account account, Order order){
        okCoinApplicationTrade.cancelOrder(order, getTradeSessionId());
    }

    public void orderInfo(Order order){
        okCoinApplicationTrade.requestOrderMassStatus(order.getOrderId(), 1, getTradeSessionId());
    }

    public Observable<Trade> getTradeObservable(){
        return tradePublishSubject.asObservable();
    }

    public Observable<Order> getOrderObservable(){
        return orderPublishSubject.asObservable();
    }

    public Observable<Depth> getDepthObservable(){
        return depthPublishSubject.asObservable();
    }
}
