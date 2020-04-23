package ru.inheaven.aida.okex.fix;

import io.reactivex.Flowable;
import io.reactivex.processors.FlowableProcessor;
import io.reactivex.processors.PublishProcessor;
import io.reactivex.schedulers.Schedulers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.Message;
import quickfix.field.*;
import quickfix.field.Currency;
import quickfix.fix44.*;
import quickfix.fix44.MessageFactory;
import ru.inheaven.aida.okex.fix.log.FileLogFactory;
import ru.inheaven.aida.okex.fix.log.MessagePrinter;
import ru.inheaven.aida.okex.model.*;
import ru.inheaven.aida.okex.model.Reject;

import javax.inject.Singleton;
import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

@Singleton
public class OkexExchange implements Application {
    public static final boolean DEV = false;

    private Logger log = LoggerFactory.getLogger(OkexExchange.class);

    private Map<String, String> KEYS = new HashMap<String, String>(){{
        put("fd079178-1b3b-44b3-8948-e11c29469218", "49FEEE45809B77FFFFA33044FBD39833");
        put("8ff1dce3-8cd5-4a3f-bd79-288fd0d665b1", "E6E419E1D9184ABC5184D14F600EF578");

        put("629524cb-7ae2-40f0-8627-547ad1eb71cb", "DFAC52FA65F09BEE7F8A8D7037BA0148");
        put("a07b2bd2-2b34-426f-a702-d9da203343ed", "37F874C6985E27D7CAC06004E14453D8");
    }};

    private List<SessionID> sessionIDs = new ArrayList<>();

    private FlowableProcessor<Trade> trades = PublishProcessor.create();
    private FlowableProcessor<Depth> depths = PublishProcessor.create();
    private FlowableProcessor<Order> orders = PublishProcessor.create();
    private FlowableProcessor<Position> positions = PublishProcessor.create();
    private FlowableProcessor<Info> infos = PublishProcessor.create();
    private FlowableProcessor<Reject> rejects = PublishProcessor.create();

    private Queue<Message> messageQueue = new ConcurrentLinkedQueue<>();
    private AtomicInteger messageCount = new AtomicInteger();

    private Queue<Message> cancelMessageQueue = new ConcurrentLinkedQueue<>();

    private AtomicLong index = new AtomicLong();

    public OkexExchange() {
        try {
            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    Message message = messageQueue.poll();

                    if (message != null){
                        SessionID sessionID = getSessionID();

                        String key = sessionID.getSessionQualifier();

                        if(message instanceof NewOrderSingle){
                            ((NewOrderSingle)message).set(new Account(key + "," + KEYS.get(key)));
                        }

                        Session.lookupSession(sessionID).send(message);
                    }

                    if (messageCount.get() > 0){
                        messageCount.decrementAndGet();
                    }
                } catch (Exception e) {
                    log.error("error send message scheduler ", e);
                }
            }, 0, 250, TimeUnit.MILLISECONDS);

            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    Message message = cancelMessageQueue.poll();

                    if (message != null){
                        SessionID sessionID = getSessionID();

                        Session.lookupSession(sessionID).send(message);
                    }
                } catch (Exception e) {
                    log.error("error cancel send message scheduler ", e);
                }
            }, 0, 1, TimeUnit.SECONDS);

            Executors.newSingleThreadScheduledExecutor().scheduleWithFixedDelay(() -> {
                try {
                    ordersInfo(sessionIDs.get(0),"this_week", "btc_usd");
                    ordersInfo(sessionIDs.get(0),"quarter", "btc_usd");
                } catch (Exception e) {
                    log.error("error order info scheduler ", e);
                }
            }, 1, 60, TimeUnit.MINUTES);

            SessionSettings settings = new SessionSettings("okex.cfg");

            Initiator initiator = new ThreadedSocketInitiator(this, new MemoryStoreFactory(),
                    settings, new FileLogFactory(settings), new MessageFactory());

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                sessionIDs.forEach(s-> Session.lookupSession(s).logout());

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

    private SessionID getSessionID() {
        return sessionIDs.get((int) (index.incrementAndGet()%sessionIDs.size()));
    }

    private void send(Message message){
        if (messageCount.get() < 20) {
            messageCount.incrementAndGet();

            SessionID sessionID = getSessionID();

            String key = sessionID.getSessionQualifier();

            if(message instanceof NewOrderSingle){
                ((NewOrderSingle)message).set(new Account(key + "," + KEYS.get(key)));
            }

            Session.lookupSession(sessionID).send(message);
        } else {
            messageQueue.add(message);
        }
    }

    public Flowable<Trade> getTrades(){
        return trades.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Depth> getDepths() {
        return depths.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Order> getOrders() {
        return orders.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Position> getPositions() {
        return positions.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Info> getInfos() {
        return infos.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    public Flowable<Reject> getRejects() {
        return rejects.onBackpressureBuffer(10000)
                .observeOn(Schedulers.computation());
    }

    @Override
    public void onCreate(SessionID sessionId) {

    }

    @Override
    public void onLogon(SessionID sessionID) {
        log.info("onLogon: " + sessionID.toString());

        sessionIDs.add(sessionID);

        if (sessionID.getSenderCompID().contains("market")) {
            subscribeMarketData(sessionID,"this_week", "btc_usd", MDEntryType.TRADE);
            subscribeMarketData(sessionID,"next_week", "btc_usd", MDEntryType.TRADE);
            subscribeMarketData(sessionID,"quarter", "btc_usd", MDEntryType.TRADE);


            subscribeMarketData(sessionID,"this_week", "btc_usd", MDEntryType.BID, MDEntryType.OFFER);
            subscribeMarketData(sessionID,"next_week", "btc_usd", MDEntryType.BID, MDEntryType.OFFER);
            subscribeMarketData(sessionID,"quarter", "btc_usd", MDEntryType.BID, MDEntryType.OFFER);
        }
    }

    @Override
    public void onLogout(SessionID sessionId) {
        log.info("onLogout: " + sessionId.toString());

        sessionIDs.remove(sessionId);
    }

    private void ordersInfo(SessionID sessionID, String symbol, String currency){
        ordersInfo(sessionID, symbol, currency, 0);
        ordersInfo(sessionID, symbol, currency, 1);
        ordersInfo(sessionID, symbol, currency, 2);
        ordersInfo(sessionID, symbol, currency, 3);
        ordersInfo(sessionID, symbol, currency, 4);
    }

    private void ordersInfo(SessionID sessionID, String symbol, String currency, int page){
        Message message = new Message();
        message.getHeader().setField(new MsgType("Z2000"));

        message.setField(new IntField(8214, page));
        message.setField(new OrdStatus(OrdStatus.PARTIALLY_FILLED));
        message.setField(new Symbol(symbol));
        message.setField(new StrikeCurrency(currency));
        message.setField(new SecurityType(SecurityType.FUTURE));

        Session.lookupSession(sessionID).send(message);
    }

    /*
      77=0 54=1 Open Long 77=0 54=2 Open Short
      77=C 54=1 Close Long 77=C 54=2  Close Short
     */
    public void createOrder(int amount, double price, char side, char positionEffect, String symbol, String currency, String clOrdID){
        NewOrderSingle message = new NewOrderSingle();

        message.set(new ClOrdID(clOrdID));
        message.set(new OrderQty(amount));
        message.set(new OrdType(OrdType.LIMIT));
        message.set(new Price(price));
        message.set(new Side(side));
        message.set(new PositionEffect(positionEffect));
        message.set(new Symbol(symbol));
        message.set(new Currency(currency));
        message.set(new SecurityType(SecurityType.FUTURE));
        message.set(new MarginRatio(10));
        message.set(new TransactTime());

        send(message);
    }

    public void createMarketOrder(int amount, char side, char positionEffect, String symbol, String currency, String clOrdID){
        NewOrderSingle message = new NewOrderSingle();

        message.set(new ClOrdID(clOrdID));
        message.set(new OrderQty(amount));
        message.set(new OrdType(OrdType.MARKET));
        message.set(new Price(0));
        message.set(new Side(side));
        message.set(new PositionEffect(positionEffect));
        message.set(new Symbol(symbol));
        message.set(new Currency(currency));
        message.set(new SecurityType(SecurityType.FUTURE));
        message.set(new MarginRatio(10));
        message.set(new TransactTime());

        send(message);
    }

    public void cancelOrder(String orderId, String clOrderId, String symbol, String currency, char side){
        OrderCancelRequest message = new OrderCancelRequest();

        message.set(new ClOrdID(clOrderId));
        message.set(new OrigClOrdID(orderId));
        message.set(new Symbol(symbol));
        message.set(new StrikeCurrency(currency));
        message.set(new Side(side));
        message.set(new SecurityType(SecurityType.FUTURE));
        message.set(new TransactTime());

        cancelMessageQueue.add(message);
    }

    @SuppressWarnings("Duplicates")
    private void subscribeMarketData(SessionID sessionID, String symbol, String currency, char... mdEntryTypes) {
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
    public void toAdmin(Message message, SessionID sessionID) {
        try {
            String msgType = message.getHeader().getString(MsgType.FIELD);

            if (MsgType.LOGON.equals(msgType) || MsgType.HEARTBEAT.equals(msgType)) {
                message.setField(new Username(sessionID.getSessionQualifier()));
                message.setField(new Password(KEYS.get(sessionID.getSessionQualifier())));
            }
        } catch (FieldNotFound e) {
            log.error("toAdmin error", e);
        }
    }

    @Override
    public void fromAdmin(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, RejectLogon {

    }

    @Override
    public void toApp(Message message, SessionID sessionId) throws DoNotSend {

    }

    private MessagePrinter messagePrinter = new MessagePrinter();

    @Override
    public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue, UnsupportedMessageType {
        String type = message.getHeader().getString(MsgType.FIELD);

        switch (type) {
            case "Z3001":
                try {
                    int count = message.getGroupCount(8303);

                    for (int i = 1; i <= count; ++i){
                        Group group = message.getGroup(i, 8303);

                        Position position = new Position();

                        position.setCurrency(message.getString(Currency.FIELD));

                        position.setAvgPrice(group.getDecimal(AvgPx.FIELD));
                        position.setQty(group.getInt(OrderQty.FIELD));
                        position.setPrice(group.getDecimal(Price.FIELD));
                        position.setSymbol(group.getString(Symbol.FIELD));
                        position.setProfit(group.getDecimal(8203));
                        position.setFrozen(group.getDecimal(8207));
                        position.setMarginCash(group.getDecimal(8208));
                        position.setPositionId(group.getString(8209));

                        switch (group.getChar(8210)){
                            case '1':
                                position.setType("long");
                                break;
                            case '2':
                                position.setType("short");
                                break;
                        }

                        position.setEveningUp(group.getDecimal(8215));

                        positions.onNext(position);

                        log.info(position.toString());
                    }
                } catch (Exception e) {
                    log.error("error Z3001 ", e);

                    messagePrinter.print(message);
                }
                break;
            case "Z3003":
                try {
                    Info info = new Info();

                    info.setCurrency(message.getString(Currency.FIELD));
                    info.setBalance(message.getDecimal(8001));
                    info.setProfit(message.getDecimal(8203));
                    info.setMargin(message.getDecimal(8208));

                    infos.onNext(info);

                    log.info(info.toString());
                } catch (Exception e) {
                    log.error("error Z3003 ", e);

                    messagePrinter.print(message);
                }
                break;
            case ExecutionReport.MSGTYPE:
                try {
                    Order order = new Order();

                    order.setAvgPrice(message.getDecimal(AvgPx.FIELD));
                    order.setClOrderId(message.getString(ClOrdID.FIELD));

                    if (message.isSetField(Commission.FIELD)) {
                        order.setCommission(message.getDecimal(Commission.FIELD));
                    }

                    order.setTotalQty(message.getInt(CumQty.FIELD));
                    order.setCurrency(message.getString(Currency.FIELD));
                    order.setExecId(message.getString(ExecID.FIELD));
                    order.setOrderId(message.getString(OrderID.FIELD));
                    order.setQty(message.getInt(OrderQty.FIELD));

                    switch (message.getChar(OrdStatus.FIELD)) {
                        case OrdStatus.NEW:
                            order.setStatus("new");
                            break;
                        case OrdStatus.PARTIALLY_FILLED:
                            order.setStatus("partially_filled");
                            break;
                        case OrdStatus.FILLED:
                            order.setStatus("filled");
                            break;
                        case OrdStatus.CANCELED:
                            order.setStatus("canceled");
                            break;
                        case OrdStatus.PENDING_CANCEL:
                            order.setStatus("pending_cancel");
                            break;
                        case OrdStatus.REJECTED:
                            order.setStatus("rejected");
                            break;
                        default:
                            order.setStatus(message.getChar(OrdStatus.FIELD) + "");
                    }

                    if (message.isSetField(OrdType.FIELD)) {
                        switch (message.getChar(OrdType.FIELD)) {
                            case OrdType.MARKET:
                                order.setType("market");
                                break;
                            case OrdType.STOP:
                                order.setType("stop");
                                break;
                            case OrdType.LIMIT:
                                order.setType("limit");
                                break;
                            case OrdType.STOP_LIMIT:
                                order.setType("stop_limit");
                                break;
                            default:
                                order.setType(message.getString(OrdType.FIELD));
                        }
                    }

                    if (message.isSetField(Price.FIELD)) {
                        order.setPrice(message.getDecimal(Price.FIELD));
                    }

                    char side = message.getField(new Side()).getValue();
                    order.setSide(side == Side.BUY ? "buy" : side == Side.SELL ? "sell" : "unknown: " + side);
                    order.setSymbol(message.getString(Symbol.FIELD));

                    if (message.isSetField(Text.FIELD)) {
                        order.setText(message.getString(Text.FIELD));
                    }

                    if (message.isSetField(TransactTime.FIELD)) {
                        order.setTxTime(message.getField(new TransactTime()).getValue());
                    }

                    if (message.isSetField(ExecType.FIELD)) {
                        switch (message.getChar(ExecType.FIELD)) {
                            case ExecType.NEW:
                                order.setExecType("new");
                                break;
                            case ExecType.PARTIAL_FILL:
                                order.setExecType("partial_fill");
                                break;
                            case ExecType.FILL:
                                order.setExecType("fill");
                                break;
                            case ExecType.CANCELED:
                                order.setExecType("canceled");
                                break;
                            case ExecType.PENDING_CANCEL:
                                order.setExecType("pending_cancel");
                                break;
                            case ExecType.REJECTED:
                                order.setExecType("rejected");
                                break;
                            default:
                                order.setExecType(message.getString(ExecType.FIELD));
                        }
                    }

                    order.setLeavesQty(message.getInt(LeavesQty.FIELD));

                    if (message.isSetField(MarginRatio.FIELD)) {
                        order.setMarginRatio(message.getInt(MarginRatio.FIELD));
                    }

                    orders.onNext(order);

                    messagePrinter.printSimple(message);
                } catch (Exception e) {
                    log.error("error ExecutionReport ", e);

                    messagePrinter.print(message);
                }
                break;
            case MarketDataSnapshotFullRefresh.MSGTYPE:
                try {
                    BigDecimal bid = null;
                    BigDecimal ask = null;

                    String currency = message.getString(Currency.FIELD);
                    String symbol = message.getString(Symbol.FIELD);

                    int count = message.getGroupCount(NoMDEntries.FIELD);

                    for (int i = 1; i <= count; i++) {
                        Group group = message.getGroup(i, NoMDEntries.FIELD);

                        BigDecimal price = group.getDecimal(MDEntryPx.FIELD);
                        Integer qty = group.getInt(MDEntrySize.FIELD);

                        switch (group.getChar(MDEntryType.FIELD)) {
                            case MDEntryType.TRADE:
                                Trade trade = new Trade();

                                trade.setCurrency(currency);
                                trade.setOrigTime(message.getField(new OrigTime()).getValue());
                                trade.setSymbol(symbol);

                                trade.setOrderId(Long.valueOf(group.getString(OrderID.FIELD)));
                                char side = group.getField(new Side()).getValue();
                                trade.setSide(side == Side.BUY ? "buy" : side == Side.SELL ? "sell" : "unknown: " + side);
                                trade.setPrice(price);
                                trade.setQty(qty);

                                trades.onNext(trade);

                                break;
                            case MDEntryType.BID:
                                if (bid == null || bid.compareTo(price) < 0){
                                    bid = price;
                                }

                                break;
                            case MDEntryType.OFFER:
                                if (ask == null || ask.compareTo(price) > 0){
                                    ask = price;
                                }

                                break;
                        }

                        if (bid != null && ask != null){
                            Depth depth = new Depth();

                            depth.setCurrency(currency);
                            depth.setSymbol(symbol);
                            depth.setBid(bid);
                            depth.setAsk(ask);

                            depths.onNext(depth);
                        }
                    }
                } catch (Exception e) {
                    log.error("error MarketDataSnapshotFullRefresh ", e);

                    messagePrinter.print(message);
                }

                break;
            case MarketDataIncrementalRefresh.MSGTYPE:
                try {
                    BigDecimal bid = null;
                    BigDecimal ask = null;

                    String symbol = message.getString(Symbol.FIELD);

                    int count = message.getGroupCount(NoMDEntries.FIELD);

                    for (int i = 1; i <= count; i++) {
                        Group group = message.getGroup(i, NoMDEntries.FIELD);

                        BigDecimal price = group.getDecimal(MDEntryPx.FIELD);

                        switch (group.getChar(MDEntryType.FIELD)) {
                            case MDEntryType.BID:
                                if (bid == null || price.compareTo(bid) > 0){
                                    bid = price;
                                }

                                break;
                            case MDEntryType.OFFER:
                                if (ask == null || price.compareTo(ask) < 0){
                                    ask = price;
                                }

                                break;
                        }

                        if (bid != null && ask != null){
                            String currency = group.getString(Currency.FIELD);

                            Depth depth = new Depth();

                            depth.setCurrency(currency);
                            depth.setSymbol(symbol);
                            depth.setBid(bid);
                            depth.setAsk(ask);

                            depths.onNext(depth);
                        }
                    }
                } catch (Exception e) {
                    log.error("error MarketDataIncrementalRefresh ", e);

                    messagePrinter.print(message);
                }
                break;
            case OrderCancelReject.MSGTYPE:
                try {
                    Reject reject = new Reject();

                    reject.setClOrderId(message.getString(ClOrdID.FIELD));
                    reject.setOrderId(message.getString(OrderID.FIELD));

                    char status = message.getChar(OrdStatus.FIELD);
                    reject.setStatus(OrdStatus.REJECTED == status ? "rejected" : status + "");

                    reject.setOrigClOrderId(message.getString(OrigClOrdID.FIELD));
                    reject.setText(message.getString(Text.FIELD));
                    reject.setReason(message.getString(CxlRejReason.FIELD));
                    reject.setResponseTo(message.getString(CxlRejResponseTo.FIELD));

                    rejects.onNext(reject);
                } catch (Exception e) {
                    log.error("error OrderCancelReject ", e);

                    messagePrinter.print(message);
                }
                break;
                default:
                    messagePrinter.print(message);

        }
    }
}
