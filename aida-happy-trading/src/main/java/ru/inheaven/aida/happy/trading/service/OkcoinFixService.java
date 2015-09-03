package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import ru.inheaven.aida.happy.trading.entity.Account;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.fix.OKCoinApplication;
import ru.inheaven.aida.happy.trading.fix.fix44.OKCoinMessageFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.inject.Singleton;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author inheaven on 21.08.2015 21:19.
 */
@Singleton
public class OkcoinFixService {
    private Logger log = LoggerFactory.getLogger(getClass());

    private OKCoinApplication okCoinApplication;
    private SessionID sessionId;

    private PublishSubject<Trade> tradePublishSubject = PublishSubject.create();
    private PublishSubject<Order> orderPublishSubject = PublishSubject.create();

    public OkcoinFixService() {
        okCoinApplication = new OKCoinApplication("832a335b-e627-49ca-b95d-bceafe6c3815", "8FAF74E300D67DCFA080A6425182C8B7"){
            @Override
            public void onLogon(SessionID sessionId) {
                requestLiveTrades(UUID.randomUUID().toString(), "LTC/USD", sessionId);
                requestLiveTrades(UUID.randomUUID().toString(), "BTC/USD", sessionId);
            }

            @Override
            public void onCreate(SessionID sessionId) {
                requestLiveTrades(UUID.randomUUID().toString(), "LTC/USD", sessionId);
                requestLiveTrades(UUID.randomUUID().toString(), "BTC/USD", sessionId);
            }

            @Override
            protected void onTrade(Trade trade) {
                tradePublishSubject.onNext(trade);
            }

            @Override
            protected void onOrder(Order order) {
                orderPublishSubject.onNext(order);
            }
        };

        try {
            SessionSettings settings;
            try (InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("client.cfg")) {
                settings = new SessionSettings(inputStream);
            }

            MessageStoreFactory storeFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new FileLogFactory(settings);
            MessageFactory messageFactory = new OKCoinMessageFactory();
            SocketInitiator initiator = new SocketInitiator(okCoinApplication, storeFactory, settings, logFactory, messageFactory);
            initiator.start();

            while (!initiator.isLoggedOn()) {
                log.info("Waiting for logged on...");
                TimeUnit.SECONDS.sleep(1);
            }

            sessionId = initiator.getSessions().get(0);
        } catch (Exception e) {
            log.error("error init okcoin fix");
        }
    }

    public void placeLimitOrder(Account account, Order order){
        okCoinApplication.placeOrder(order, sessionId);
    }

    public Observable<Trade> getTradeObservable(){
        return tradePublishSubject.asObservable();
    }

    public Observable<Order> getOrderObservable(){
        return orderPublishSubject.asObservable();
    }
}
