package ru.inheaven.aida.okex.storage.fix;

import com.datastax.driver.core.querybuilder.Insert;
import com.datastax.driver.core.querybuilder.QueryBuilder;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataRequest;
import ru.inheaven.aida.okex.storage.service.ClusterService;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Anatoly A. Ivanov
 * 31.08.2017 20:30
 */
@Singleton
public class OkexExchangeStorage implements Application{
    private Map<String, String> KEYS = new HashMap<String, String>(){{
        put("fd079178-1b3b-44b3-8948-e11c29469218", "49FEEE45809B77FFFFA33044FBD39833");

        put("629524cb-7ae2-40f0-8627-547ad1eb71cb", "DFAC52FA65F09BEE7F8A8D7037BA0148");
    }};

    @Inject
    private ClusterService clusterService;

    private SessionID sessionID;

    public OkexExchangeStorage() {
        try {
            SessionSettings settings = new SessionSettings("okex.cfg");

            Initiator initiator = new ThreadedSocketInitiator(this, new MemoryStoreFactory(),
                    settings, new FileLogFactory(settings), new quickfix.fix44.MessageFactory());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                Session.lookupSession(sessionID).logout();

                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }));

            initiator.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onCreate(SessionID sessionId) {

    }

    private void subscribeMarketData(String symbol, String currency, char... mdEntryTypes) {
        MarketDataRequest message = new MarketDataRequest();

        MarketDataRequest.NoRelatedSym symGroup = new MarketDataRequest.NoRelatedSym();
        symGroup.set(new Symbol(symbol));
        symGroup.set(new SecurityType(SecurityType.FUTURE));
        symGroup.set(new StrikeCurrency(currency));
        message.addGroup(symGroup);

        message.set(new MDReqID(UUID.randomUUID().toString()));
        message.set(new SubscriptionRequestType(SubscriptionRequestType.SNAPSHOT_PLUS_UPDATES));
        message.set(new MDUpdateType(MDUpdateType.FULL_REFRESH));
        message.set(new MarketDepth(0));

        for (char mdEntryType : mdEntryTypes) {
            MarketDataRequest.NoMDEntryTypes entryTypesGroup = new MarketDataRequest.NoMDEntryTypes();
            entryTypesGroup.set(new MDEntryType(mdEntryType));
            message.addGroup(entryTypesGroup);
        }

        Session.lookupSession(sessionID).send(message);
    }

    @Override
    public void onLogon(SessionID sessionId) {
        this.sessionID = sessionId;

        if (sessionId.getSenderCompID().contains("market")) {
            subscribeMarketData("this_week", "btc_usd", MDEntryType.TRADE);
            subscribeMarketData("this_week", "btc_usd", MDEntryType.BID, MDEntryType.OFFER);

            subscribeMarketData("next_week", "btc_usd", MDEntryType.TRADE);
            subscribeMarketData("next_week", "btc_usd", MDEntryType.BID, MDEntryType.OFFER);

            subscribeMarketData("quarter", "btc_usd", MDEntryType.TRADE);
            subscribeMarketData("quarter", "btc_usd", MDEntryType.BID, MDEntryType.OFFER);

            subscribeMarketData("this_week", "ltc_usd", MDEntryType.TRADE);
            subscribeMarketData("this_week", "ltc_usd", MDEntryType.BID, MDEntryType.OFFER);

            subscribeMarketData("next_week", "ltc_usd", MDEntryType.TRADE);
            subscribeMarketData("next_week", "ltc_usd", MDEntryType.BID, MDEntryType.OFFER);

            subscribeMarketData("quarter", "ltc_usd", MDEntryType.TRADE);
            subscribeMarketData("quarter", "ltc_usd", MDEntryType.BID, MDEntryType.OFFER);
        }

        System.out.println(sessionId.toString());
    }

    @Override
    public void onLogout(SessionID sessionId) {

    }

    @Override
    public void toAdmin(Message message, SessionID sessionId) {
        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);

            if (MsgType.LOGON.equals(msgType) || MsgType.HEARTBEAT.equals(msgType)) {
                message.setField(new Username(sessionId.getSessionQualifier()));
                message.setField(new Password(KEYS.get(sessionId.getSessionQualifier())));
            }
        } catch (FieldNotFound e) {
            e.printStackTrace();
        }
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

    }

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        try {
            Insert insert = QueryBuilder.insertInto("okex", "market_data")
                    .value("id", System.currentTimeMillis())
                    .value("session_qualifier", sessionId.getSessionQualifier())
                    .value("message", message.toString());
            clusterService.getSession().execute(insert);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
