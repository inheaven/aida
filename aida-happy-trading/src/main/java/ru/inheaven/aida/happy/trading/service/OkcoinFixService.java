package ru.inheaven.aida.happy.trading.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import ru.inheaven.aida.happy.trading.entity.ExchangeType;
import ru.inheaven.aida.happy.trading.entity.OrderType;
import ru.inheaven.aida.happy.trading.entity.Trade;
import ru.inheaven.aida.happy.trading.fix.OKCoinApplication;
import ru.inheaven.aida.happy.trading.fix.fix44.OKCoinMessageFactory;
import rx.Observable;
import rx.subjects.PublishSubject;

import javax.inject.Singleton;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.Date;
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

    private long lastTrade = System.currentTimeMillis();

    public OkcoinFixService() {
        okCoinApplication = new OKCoinApplication("00dff9d7-7d99-45f9-bd41-23d08d4665ce", "CB58C8091A0605AAD1F5815F215BB93B"){
            @Override
            public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound,
                    UnsupportedMessageType, IncorrectTagValue {
                String symbol = message.getSymbol().getValue();

                for (int i = 1, l = message.getNoMDEntries().getValue(); i <= l; i++) {
                    Group group = message.getGroup(i, NoMDEntries.FIELD);

                    BigDecimal price = new BigDecimal(group.getString(MDEntryPx.FIELD));
                    BigDecimal amount = group.isSetField(MDEntrySize.FIELD) ? new BigDecimal(group.getString(MDEntrySize.FIELD)) : null;
                    char type = group.getChar(MDEntryType.FIELD);

                    if (type == MDEntryType.TRADE){
                        Trade trade = new Trade();
                        trade.setTradeId(String.valueOf(System.nanoTime()));
                        trade.setExchangeType(ExchangeType.OKCOIN);
                        trade.setSymbol(symbol);
                        trade.setOrderType(group.getField(new Side()).getValue() == Side.BUY ? OrderType.BID : OrderType.ASK);
                        trade.setPrice(price);
                        trade.setAmount(amount);
                        trade.setTime(message.getString(OrigTime.FIELD));
                        trade.setCreated(new Date());

                        tradePublishSubject.onNext(trade);

                        lastTrade = System.currentTimeMillis();
                    }
                }
            }

            @Override
            public void onLogon(SessionID sessionId) {
                requestLiveTrades(UUID.randomUUID().toString(), "LTC/USD", sessionId);
                requestLiveTrades(UUID.randomUUID().toString(), "BTC/USD", sessionId);
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

    public Observable<Trade> getTradeObservable(){
        return tradePublishSubject.asObservable();
    }
}
