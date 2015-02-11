package ru.inheaven.aida.coin.service;

import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Trade;
import com.xeiam.xchange.dto.trade.LimitOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.MDUpdateType;
import quickfix.field.SubscriptionRequestType;
import ru.inheaven.aida.fix.OKCoinApplication;
import ru.inheaven.aida.fix.OKCoinXChangeApplication;
import ru.inheaven.aida.fix.fix44.OKCoinMessageFactory;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import java.io.InputStream;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * @author inheaven on 12.02.2015 1:19.
 */
@Singleton
@Startup
public class OkcoinFixService {
    private static final Logger log = LoggerFactory.getLogger(OkcoinFixService.class);

    private OKCoinApplication application;
    private SessionID sessionId;
    private Initiator initiator;

    @PostConstruct
    public void start(){
        try {
            application = new OKCoinXChangeApplication("5e94fb8e-73dc-11e4-8382-d8490bd27a4b", "F41C04C8917B62967D12030DA66DF202") {

                @Override
                public void onOrderBook(OrderBook orderBook, SessionID sessionId) {
                    log.info("asks: {}, bids: {}", orderBook.getAsks().size(), orderBook.getBids().size());

                    // bids should be sorted by limit price descending
                    LimitOrder preOrder = null;
                    for (LimitOrder order : orderBook.getBids()) {
                        log.info("Bid: {}, {}", order.getLimitPrice(), order.getTradableAmount());

                        if (preOrder != null && preOrder.compareTo(order) >= 0) {
                            log.error("bids should be sorted by limit price descending");
                        }
                        preOrder = order;
                    }

                    // asks should be sorted by limit price ascending
                    preOrder = null;
                    for (LimitOrder order : orderBook.getAsks()) {
                        log.info("Ask: {}, {}", order.getLimitPrice(), order.getTradableAmount());

                        if (preOrder != null && preOrder.compareTo(order) >= 0) {
                            log.error("asks should be sorted by limit price ascending");
                        }
                        preOrder = order;
                    }

                    LimitOrder ask = orderBook.getAsks().get(0);
                    LimitOrder bid = orderBook.getBids().get(0);
                    log.info("lowest  ask: {}, {}", ask.getLimitPrice(), ask.getTradableAmount());
                    log.info("highest bid: {}, {}", bid.getLimitPrice(), bid.getTradableAmount());

                    if (ask.getLimitPrice().compareTo(bid.getLimitPrice()) <= 0) {
                        throw new IllegalStateException(String.format("Lowest ask %s is not higher than the highest bid %s.",
                                ask.getLimitPrice(), bid.getLimitPrice()));
                    }
                }

                @Override
                public void onTrades(List<Trade> trades, SessionID sessionId) {
                    for (Trade trade : trades) {
                        log.info("{}", trade);
                    }
                }

                @Override
                public void onAccountInfo(AccountInfo accountInfo, SessionID sessionId) {
                    log.info("AccountInfo: {}", accountInfo);
                }
            };

            SessionSettings settings;
            try (InputStream inputStream = getClass().getResourceAsStream("client.cfg")) {
                settings = new SessionSettings(inputStream);
            }

            MessageStoreFactory storeFactory = new FileStoreFactory(settings);
            LogFactory logFactory = new FileLogFactory(settings);
            MessageFactory messageFactory = new OKCoinMessageFactory();
            initiator = new SocketInitiator(application, storeFactory, settings, logFactory, messageFactory);
            initiator.start();

            while (!initiator.isLoggedOn()) {
                log.info("Waiting for logged on...");
                TimeUnit.SECONDS.sleep(1);
            }

            sessionId = initiator.getSessions().get(0);

            //request
            String mdReqId = UUID.randomUUID().toString();
            String symbol = "LTC/USD";
            char subscriptionRequestType = SubscriptionRequestType.SNAPSHOT;
            int marketDepth = 0;
            int mdUpdateType = MDUpdateType.FULL_REFRESH;

            application.requestOrderBook(mdReqId, symbol, subscriptionRequestType, marketDepth, mdUpdateType, sessionId);

            mdReqId = UUID.randomUUID().toString();
            application.requestLiveTrades(mdReqId, symbol, sessionId);

            mdReqId = UUID.randomUUID().toString();
            application.request24HTicker(mdReqId, symbol, sessionId);

            String accReqId = UUID.randomUUID().toString();
            application.requestAccountInfo(accReqId, sessionId);
        } catch (Exception e) {
            log.error("Error okcoin fix start", e);
        }
    }
}
