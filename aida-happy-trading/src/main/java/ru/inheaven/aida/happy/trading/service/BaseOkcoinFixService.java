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
import java.util.List;
import java.util.UUID;

/**
 * @author inheaven on 21.08.2015 21:19.
 */
public abstract class BaseOkcoinFixService {
    private Logger log = LoggerFactory.getLogger(getClass());
    private OKCoinApplication okCoinApplication;
    private SessionID sessionId;
    private PublishSubject<Trade> tradePublishSubject = PublishSubject.create();
    private PublishSubject<Order> orderPublishSubject = PublishSubject.create();
    private PublishSubject<Depth> depthPublishSubject = PublishSubject.create();

    public void placeLimitOrder(Account account, Order order){
        okCoinApplication.placeOrder(order, sessionId);
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

    public BaseOkcoinFixService(ExchangeType exchangeType, String apiKey, String secretKey, String config, List<String> symbols) {
        okCoinApplication = new OKCoinApplication(exchangeType, apiKey, secretKey){
            @Override
            public void onLogon(SessionID sessionId) {
                BaseOkcoinFixService.this.sessionId = sessionId;

                symbols.forEach(s -> {
                    requestLiveTrades(UUID.randomUUID().toString(), s, sessionId);
                    requestOrderBook(UUID.randomUUID().toString(), s, sessionId);
                });
            }

            @Override
            public void onCreate(SessionID sessionId) {
                BaseOkcoinFixService.this.sessionId = sessionId;

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
            protected void onOrder(Order order) {
                orderPublishSubject.onNext(order);
            }

            @Override
            protected void onDepth(Depth depth) {
                depthPublishSubject.onNext(depth);
            }
        };

        try {
            SessionSettings settings;
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(config)) {
                settings = new SessionSettings(inputStream);
            }

            MessageStoreFactory storeFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new FileLogFactory(settings);
            MessageFactory messageFactory = new OKCoinMessageFactory();
            SocketInitiator initiator = new SocketInitiator(okCoinApplication, storeFactory, settings, logFactory, messageFactory);
            initiator.start();
        } catch (Exception e) {
            log.error("error init okcoin fix");
        }
    }
}
