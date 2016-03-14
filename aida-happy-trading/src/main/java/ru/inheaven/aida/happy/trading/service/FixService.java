package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import ru.inheaven.aida.happy.trading.entity.*;
import ru.inheaven.aida.happy.trading.fix.OKCoinApplication;
import ru.inheaven.aida.happy.trading.fix.OkcoinMarketApplication;
import ru.inheaven.aida.happy.trading.fix.fix44.OKCoinMessageFactory;
import ru.inheaven.aida.happy.trading.mapper.AccountMapper;
import rx.subjects.PublishSubject;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.List;

/**
 * @author inheaven on 04.09.2015 2:46.
 */
@Singleton
public class FixService {
    private Logger log = LoggerFactory.getLogger(FixService.class);

    private PublishSubject<Trade> tradePublishSubject = PublishSubject.create();
    private PublishSubject<Order> orderPublishSubject = PublishSubject.create();
    private PublishSubject<Depth> depthPublishSubject = PublishSubject.create();

    @Inject
    public FixService(AccountMapper accountMapper) {
        //Okcoin Market
        OkcoinMarketApplication okcoinMarketApplication = new OkcoinMarketApplication(ExchangeType.OKCOIN,
                "8b8620cf-83ed-46d8-91e6-41e5eb65f44f", "DBB5E3FAA26238E9613BD73A3D4ECEDC"){
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
        };


        List<Account> accounts = accountMapper.getAccounts(ExchangeType.OKCOIN);


    }

    private void init(OKCoinApplication application, String config, boolean logging){
        try {
            SessionSettings settings = new SessionSettings(config);

            ThreadedSocketInitiator initiator = new ThreadedSocketInitiator(application,
                    new MemoryStoreFactory(),
                    settings,
                    logging ? new FileLogFactory(settings) : null,
                    new OKCoinMessageFactory());
            initiator.start();
        } catch (Exception e) {
            log.error("error init okcoin fix");
        }
    }

    //add create order switcher
}
