package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import ru.inheaven.aida.happy.trading.entity.Depth;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.fix.OkcoinApplication;
import ru.inheaven.aida.happy.trading.fix.fix44.OKCoinMessageFactory;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * @author inheaven on 04.09.2015 2:46.
 */
@Singleton
public class FixService {
    private Logger log = LoggerFactory.getLogger(FixService.class);

    private PublishSubject<Trade> tradePublishSubject = PublishSubject.create();
    private PublishSubject<Depth> depthPublishSubject = PublishSubject.create();
    private PublishSubject<Order> orderPublishSubject = PublishSubject.create();

    private OkcoinApplication okcoinApplication;

    private Connector connector;

    private Queue<Order> orderQueue = new ConcurrentLinkedQueue<>();

    @Inject
    public FixService(AccountMapper accountMapper) {
        try {
            okcoinApplication = new OkcoinApplication() {
                private SessionID marketSessionId;
                private long time = System.currentTimeMillis();

                {
                    Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                        if (System.currentTimeMillis() - time > 600000){
                            connector.stop();

                            try {
                                connector.start();
                            } catch (Exception e) {
                                log.error("error connector restart", e);
                            }

                            log.error("fix service trade delay");
                        }
                    }, 0, 1, TimeUnit.MINUTES);
                }

                @Override
                public void onLogon(SessionID sessionId) {
                    super.onLogon(sessionId);

                    request(sessionId);
                }

                @Override
                protected void onDepth(Depth depth) {
                    depthPublishSubject.onNext(depth);
                }

                @Override
                protected void onTrade(Trade trade) {
                    time = System.currentTimeMillis();

                    tradePublishSubject.onNext(trade);
                }

                //Fut
                // 8ff1dce3-8cd5-4a3f-bd79-288fd0d665b1
                // E6E419E1D9184ABC5184D14F600EF578

                @Override
                protected void onOrder(Order order) {
                    orderPublishSubject.onNext(order);
                }

                private void request(SessionID sessionId){
                    if ("433cd272-ea87-4a1b-abca-86a0dcf9d8c4".equalsIgnoreCase(sessionId.getSessionQualifier())){
                        marketSessionId = sessionId;

                        requestLiveTrades(sessionId, "BTC/USD");
                        requestOrderBook(sessionId, "BTC/USD");
//
//                        requestLiveTrades(sessionId, "LTC/CNY");
//                        requestOrderBook(sessionId, "LTC/CNY");
                    }
                }
            };

            SessionSettings settings = new SessionSettings("okcoin_cn.cfg");

            connector = new ThreadedSocketInitiator(okcoinApplication,
                    new MemoryStoreFactory(),
                    settings,
                    new FileLogFactory(settings){
                        @Override
                        public Log create(SessionID sessionId) {
//                            if ("8b8620cf-83ed-46d8-91e6-41e5eb65f44f".equalsIgnoreCase(sessionId.getSessionQualifier())){
//                                return null;
//                            }

                            return super.create(sessionId);
                        }
                    },
                    new OKCoinMessageFactory());
            connector.start();
        } catch (Exception e) {
            log.error("error init okcoin fix");
        }

        /*
        Access Keyc6446eee-e820690a-3224e7e8-7f0f4
        Secret Keyde1ab7d9-c34894b7-2e6931fa-60880
        IP地址 : 45.115.36.120
        */

//        Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
//            try {
//                Order order = orderQueue.poll();
//
//                if (order != null){
//                    internalPlaceOrder(order.getAccountId(), order);
//                }
//            } catch (Exception e) {
//                log.error("error queue place order", e);
//            }
//        },0, 127, TimeUnit.MILLISECONDS);
    }


    public void placeLimitOrder(Long accountId, Order order){
//        orderQueue.add(order);
        internalPlaceOrder(accountId, order);
    }

    private void internalPlaceOrder(Long accountId, Order order){
        okcoinApplication.placeOrder(order);
    }

    public void cancelOrder(Long accountId, Order order){
        okcoinApplication.cancelOrder(order);
    }

    public void orderInfo(Long accountId, Order order){
        okcoinApplication.requestOrderMassStatus(order.getOrderId(), 1);
    }

    public Observable<Trade> getTradeObservable(){
        return tradePublishSubject.asObservable();
    }

    public Observable<Depth> getDepthObservable(){
        return depthPublishSubject.asObservable();
    }

    public Observable<Order> getOrderObservable(){
        return orderPublishSubject.asObservable();
    }
}
