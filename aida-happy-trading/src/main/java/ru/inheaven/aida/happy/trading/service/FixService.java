package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.MemoryStoreFactory;
import quickfix.SessionID;
import quickfix.SessionSettings;
import quickfix.ThreadedSocketInitiator;
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

    @Inject
    public FixService(AccountMapper accountMapper) {
        try {
            okcoinApplication = new OkcoinApplication() {

                @Override
                public void onLogon(SessionID sessionId) {
                    super.onLogon(sessionId);

                    if ("MARKET_DATA".equalsIgnoreCase(sessionId.getSessionQualifier())){
                        requestLiveTrades(sessionId, "BTC/CNY");
                        requestOrderBook(sessionId, "BTC/CNY");

                        requestLiveTrades(sessionId, "LTC/CNY");
                        requestOrderBook(sessionId, "LTC/CNY");
                    }
                }

                @Override
                protected void onDepth(Depth depth) {
                    depthPublishSubject.onNext(depth);
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

            SessionSettings settings = new SessionSettings("okcoin_cn.cfg");

            ThreadedSocketInitiator initiator = new ThreadedSocketInitiator(okcoinApplication,
                    new MemoryStoreFactory(),
                    settings,
                    null, //todo market
                    new OKCoinMessageFactory());
            initiator.start();
        } catch (Exception e) {
            log.error("error init okcoin fix");
        }
    }


    public void placeLimitOrder(Long accountId, Order order){
        internalPlaceOrder(accountId, order); //todo account session id map
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

    public Observable<Order> getOrderObservable(){
        return orderPublishSubject.asObservable();
    }

    public Observable<Depth> getDepthObservable(){
        return depthPublishSubject.asObservable();
    }
}