package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import ru.inheaven.aida.happy.trading.entity.Depth;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.fix.OkcoinMarketApplication;
import ru.inheaven.aida.happy.trading.fix.OkcoinTradeApplication;
import ru.inheaven.aida.happy.trading.fix.fix44.OKCoinMessageFactory;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author inheaven on 04.09.2015 2:46.
 */
@Singleton
public class FixService {
    private Logger log = LoggerFactory.getLogger(FixService.class);

    private PublishSubject<Trade> tradePublishSubject = PublishSubject.create();
    private PublishSubject<Depth> depthPublishSubject = PublishSubject.create();
    private PublishSubject<Order> orderPublishSubject = PublishSubject.create();

    private Map<Long, OkcoinTradeApplication> tradeApplicationMap = new ConcurrentHashMap<>();

    @Inject
    public FixService(AccountMapper accountMapper) {
        //Okcoin Market
        start(new OkcoinMarketApplication(ExchangeType.OKCOIN_CN, "8b8620cf-83ed-46d8-91e6-41e5eb65f44f", "DBB5E3FAA26238E9613BD73A3D4ECEDC"){
            private void request(){
                requestLiveTrades("BTC/CNY");
                requestOrderBook("BTC/CNY");

                requestLiveTrades("LTC/CNY");
                requestOrderBook("LTC/CNY");
            }

            @Override
            public void onLogon(SessionID sessionId) {
                super.onLogon(sessionId);
                request();
            }

            @Override
            public void onCreate(SessionID sessionId) {
                super.onCreate(sessionId);
                request();
            }

            @Override
            protected void onDepth(Depth depth) {
                depthPublishSubject.onNext(depth);
            }

            @Override
            protected void onTrade(Trade trade) {
                tradePublishSubject.onNext(trade);
            }
        }, "okcoin_market_cn.cfg", false);

        //Accounts
        accountMapper.getAccounts(ExchangeType.OKCOIN).forEach(account ->
                start(new OkcoinTradeApplication(account.getId(), account.getApiKey(), account.getSecretKey()){
                    @Override
                    protected void onOrder(Order order) {
                        orderPublishSubject.onNext(order);
                    }
                }, "okcoin_trade_cn_" + account.getId(), true));
    }

    private void start(Application application, String config, boolean logging){
        try {
            SessionSettings settings = new SessionSettings(config);

            ThreadedSocketInitiator initiator = new ThreadedSocketInitiator(application,
                    new MemoryStoreFactory(),
                    settings,
                    logging ? new FileLogFactory(settings) : null,
                    new OKCoinMessageFactory());
            initiator.start();

            if (application instanceof OkcoinTradeApplication){
                OkcoinTradeApplication tradeApplication = (OkcoinTradeApplication) application;
                tradeApplicationMap.put(tradeApplication.getAccountId(), tradeApplication);
            }
        } catch (Exception e) {
            log.error("error init okcoin fix");
        }
    }

    public void placeOrder(Long accountId, Order order){
        internalPlaceOrder(accountId, order);
    }

    private void internalPlaceOrder(Long accountId, Order order){
        tradeApplicationMap.get(accountId).placeOrder(order);
    }

    public void cancelOrder(Long accountId, Order order){
        tradeApplicationMap.get(accountId).cancelOrder(order);
    }

    public void orderInfo(Long accountId, Order order){
        tradeApplicationMap.get(accountId).requestOrderMassStatus(order.getOrderId(), 1);
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
