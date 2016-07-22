package ru.inheaven.aida.happy.trading.fix;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import ru.inheaven.aida.happy.trading.entity.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * inheaven on 14.03.2016.
 */
public abstract class OkcoinApplication extends BaseApplication{
    private Logger log = LoggerFactory.getLogger(OkcoinApplication.class);

    private MarketDataRequestCreator marketDataRequestCreator = new MarketDataRequestCreator();

    private Map<SessionID, TradeRequestCreator> tradeRequestCreatorMap = new HashMap<>();

    private List<SessionID> sessionIds = new ArrayList<>();

    private AtomicLong index = new AtomicLong(0);

    protected ExchangeType getExchangeType(SessionID sessionID){
        return ExchangeType.OKCOIN_CN;
    }

    public void requestMarketData(SessionID sessionID, String mdReqId, String symbol, char subscriptionRequestType, int marketDepth,
                                  int mdUpdateType, char[] mdEntryTypes) {
        sendMessage(sessionID, marketDataRequestCreator.createMarketDataRequest( mdReqId, symbol, subscriptionRequestType,
                marketDepth, mdUpdateType, mdEntryTypes));
    }

    public void requestOrderBook(SessionID sessionID, String symbol) {
        sendMessage(sessionID, marketDataRequestCreator.createOrderBookRequest(UUID.randomUUID().toString(), symbol));
    }

    public void requestLiveTrades(SessionID sessionID, String symbol) {
        sendMessage(sessionID, marketDataRequestCreator.createLiveTradesRequest(UUID.randomUUID().toString(), symbol));
    }

    public void request24HTicker(SessionID sessionID, String mdReqId, String symbol) {
        sendMessage(sessionID, marketDataRequestCreator.create24HTickerRequest(mdReqId, symbol));
    }

    @Override
    public void onMessage(MarketDataSnapshotFullRefresh message, SessionID sessionID) throws FieldNotFound,
            UnsupportedMessageType, IncorrectTagValue {
        String symbol = message.getSymbol().getValue();
        List<PriceAmount> bids = null;
        List<PriceAmount> asks = null;

        for (int i = 1, l = message.getNoMDEntries().getValue(); i <= l; i++) {
            Group group = message.getGroup(i, NoMDEntries.FIELD);

            char type = group.getChar(MDEntryType.FIELD);
            BigDecimal price = new BigDecimal(group.getString(MDEntryPx.FIELD));
            BigDecimal amount = group.isSetField(MDEntrySize.FIELD) ? new BigDecimal(group.getString(MDEntrySize.FIELD)) : null;

            switch (type){
                case MDEntryType.TRADE:
                    Trade trade = new Trade();
                    trade.setTradeId(String.valueOf(System.nanoTime()));
                    trade.setExchangeType(getExchangeType(sessionID));
                    trade.setSymbol(symbol);
                    trade.setOrderType(group.getField(new Side()).getValue() == Side.SELL ? OrderType.BID : OrderType.ASK);
                    trade.setPrice(price);
                    trade.setAmount(amount);
                    trade.setTime(message.getString(OrigTime.FIELD));
                    trade.setCreated(new Date());
                    trade.setOrigTime(message.getField(new OrigTime()).getValue());

                    onTrade(trade);
                    break;
                case MDEntryType.BID:
                    if (bids == null){
                        bids = new ArrayList<>();
                    }

                    bids.add(new PriceAmount(price, amount));
                    break;
                case MDEntryType.OFFER:
                    if (asks == null){
                        asks = new ArrayList<>();
                    }

                    asks.add(new PriceAmount(price, amount));
                    break;
            }
        }


        if (asks != null && bids != null){
            Depth depth = new Depth();

            depth.setExchangeType(getExchangeType(sessionID));
            depth.setSymbol(symbol);

            depth.setAsk(asks.get(asks.size()-1).getPrice());
            depth.setBid(bids.get(0).getPrice());

            depth.setAskJson("[" + Joiner.on(",").join(asks) + "]");
            depth.setBidJson("[" + Joiner.on(",").join(bids) + "]");

            depth.setTime(message.getField(new OrigTime()).getValue());
            depth.setCreated(new Date());

            onDepth(depth);
        }
    }

    protected abstract void onDepth(Depth depth);

    protected abstract void onTrade(Trade trade);

    public TradeRequestCreator getTradeRequestCreator(SessionID sessionID){
        TradeRequestCreator tradeRequestCreator =  tradeRequestCreatorMap.get(sessionID);

        if (tradeRequestCreator == null){
            tradeRequestCreator = new TradeRequestCreator(sessionID.getSenderSubID(), OKCoinAPI.KEY.get(sessionID.getSenderSubID()));

            tradeRequestCreatorMap.put(sessionID, tradeRequestCreator);
        }

        return tradeRequestCreator;
    }

    private Long getAccountId(SessionID sessionID) {
        return OKCoinAPI.ACCOUNT_ID.get(sessionID.getSenderSubID());
    }

    @Override
    public void onCreate(SessionID sessionId) {
        super.onCreate(sessionId);

        if (!sessionIds.contains(sessionId)){
            sessionIds.add(sessionId);
        }
    }

    @Override
    public void onLogon(SessionID sessionId) {
        super.onLogon(sessionId);

        if (!sessionIds.contains(sessionId)){
            sessionIds.add(sessionId);
        }
    }

    @Override
    public void onLogout(SessionID sessionId) {
        super.onLogout(sessionId);

        sessionIds.remove(sessionId);
    }

    private SessionID nextSessionID(){
        return sessionIds.get((int) (index.get() % sessionIds.size()));
    }

    public void placeOrder(Order order) {
        SessionID sessionID = nextSessionID();

        sendMessage(sessionID, getTradeRequestCreator(sessionID).createNewOrderSingle(order.getInternalId(), getSide(order.getType()),
                OrdType.LIMIT, order.getAmount(), order.getPrice(), order.getSymbol()));
    }

    public void cancelOrder(Order order) {
        cancelOrder(order.getInternalId(), order.getOrderId(), getSide(order.getType()), order.getSymbol());
    }

    private char getSide(OrderType orderType){
        switch (orderType){
            case ASK: return Side.SELL;
            case BID: return Side.BUY;
            default: return  '0';
        }
    }

    public void cancelOrder(String clOrdId, String origClOrdId, char side, String symbol) {
        SessionID sessionID = nextSessionID();

        sendMessage(sessionID, getTradeRequestCreator(sessionID).createOrderCancelRequest(clOrdId != null ? clOrdId : System.nanoTime() + "",
                origClOrdId, side, symbol));
    }

    /**
     * Request order status.
     *
     * @param massStatusReqId Client-assigned unique ID of this request.(or ORDERID)
     * @param massStatusReqType Specifies the scope of the mass status request.
     * 1 = status of a specified order(Tag584 is ORDERID)
     * 7 = Status for all orders
     */
    public void requestOrderMassStatus(String massStatusReqId, int massStatusReqType) {
        SessionID sessionID = nextSessionID();

        sendMessage(sessionID, getTradeRequestCreator(sessionID).createOrderMassStatusRequest(massStatusReqId, massStatusReqType));
    }

    public void requestTradeCaptureReportRequest(String tradeRequestId, String symbol) {
        SessionID sessionID = nextSessionID();

        sendMessage(sessionID, getTradeRequestCreator(sessionID).createTradeCaptureReportRequest(tradeRequestId, symbol));
    }

    public void requestAccountInfo(String accReqId) {
        SessionID sessionID = nextSessionID();

        sendMessage(sessionID, getTradeRequestCreator(sessionID).createAccountInfoRequest(accReqId));
    }

    /**
     * Request history order information which order ID is after the specified
     * {@code orderId}.
     *
     * @param tradeRequestId Client-assigned unique ID of this request.
     * @param symbol Symbol. BTC/CNY or LTC/CNY.
     * @param orderId Order ID. Return 10 records after this id.
     * @param ordStatus Order status. 0 = Not filled 1 = Fully filled.
     */
    public void requestOrdersInfoAfterSomeID(String tradeRequestId, String symbol, long orderId, char ordStatus) {
        SessionID sessionID = nextSessionID();

        sendMessage(sessionID, getTradeRequestCreator(sessionID).createOrdersInfoAfterSomeIDRequest(tradeRequestId, symbol, orderId, ordStatus));
    }

    @Override
    public void onMessage(ExecutionReport message, SessionID sessionId) throws FieldNotFound, UnsupportedMessageType,
            IncorrectTagValue {
        try {
            Order order = new Order();

            order.setAccountId(getAccountId(sessionId));
            order.setSymbol(message.getSymbol().getValue());
            order.setOrderId(message.getOrderID().getValue());

            if (message.isSetClOrdID()) {
                order.setInternalId(message.getClOrdID().getValue().replace("\\", ""));
            }

            order.setAvgPrice(BigDecimal.valueOf(message.getAvgPx().getValue()));

            if (message.isSetPrice()) {
                order.setPrice(BigDecimal.valueOf(message.getPrice().getValue()));
            }

            if (message.isSetText()){
                order.setText(message.getText().getValue() + " " + sessionId.getSenderCompID());
            }

            order.setAmount(BigDecimal.valueOf(message.getOrderQty().getValue()));

            order.setType(message.getSide().getValue() == Side.BUY ? OrderType.BID : OrderType.ASK);

            Date time = message.isSetTransactTime() ? message.getTransactTime().getValue() : new Date();

            switch (message.getOrdStatus().getValue()){
                case '0':
                case '1':
                    order.setStatus(OrderStatus.OPEN);
                    order.setOpen(time);
                    break;
                case '2':
                    order.setStatus(OrderStatus.CLOSED);
                    order.setClosed(time);
                    break;
                case '4':
                case '8':
                    order.setStatus(OrderStatus.CANCELED);
                    order.setClosed(time);
                    break;
                default:
                    order.setStatus(OrderStatus.CANCELED);
                    log.error("unknow order type - > {}", message.toString());
            }

            onOrder(order);
        } catch (Exception e) {
            log.error("error onMessage ", e);
        }
    }

    @Override
    protected String getApiKey(SessionID sessionID) {
        return sessionID.getSenderSubID();
    }

    @Override
    protected String getSecretKey(SessionID sessionID) {
        return OKCoinAPI.KEY.get(sessionID.getSenderSubID());
    }

    protected abstract void onOrder(Order order);
}
