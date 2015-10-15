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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
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

    private List<SessionID> tradeSessions = Arrays.asList(tradeSessionId, tradeSessionId2, tradeSessionId3, tradeSessionId4);
    private AtomicLong index = new AtomicLong(4);

    public void placeLimitOrder(Account account, Order order){
        SessionID sessionID = tradeSessionId;

        switch ((int) index.incrementAndGet() % 4){
            case 0:
                sessionID = tradeSessionId;
                break;
            case 1:
                sessionID = tradeSessionId2;
                break;
            case 2:
                sessionID = tradeSessionId3;
                break;
            case 3:
                sessionID = tradeSessionId4;
                break;
        }

        okCoinApplicationTrade.placeOrder(order, sessionID);
    }

    public void cancelOrder(Account account, Order order){
        okCoinApplicationTrade.cancelOrder(order, tradeSessionId);
    }

    public void orderInfo(Order order){
        okCoinApplicationTrade.requestOrderMassStatus(order.getOrderId(), 1, tradeSessionId);
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

    public BaseOkcoinFixService(ExchangeType exchangeType, String apiKey, String secretKey, String marketConfig,
                                String tradeConfig, String tradeConfig2, String tradeConfig3, String tradeConfig4,
                                List<String> symbols) {
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
}
