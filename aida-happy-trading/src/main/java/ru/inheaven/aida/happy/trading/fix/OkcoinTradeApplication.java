package ru.inheaven.aida.happy.trading.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.OrdType;
import quickfix.field.Side;
import quickfix.fix44.ExecutionReport;
import ru.inheaven.aida.happy.trading.entity.Order;
import ru.inheaven.aida.happy.trading.entity.OrderStatus;
import ru.inheaven.aida.happy.trading.entity.OrderType;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * inheaven on 14.03.2016.
 */
public class OkcoinTradeApplication extends BaseApplication{
    private Logger log = LoggerFactory.getLogger(OkcoinTradeApplication.class);

    private TradeRequestCreator tradeRequestCreator;

    private Long accountId;

    private List<SessionID> sessionIds = new ArrayList<>();

    private AtomicLong index = new AtomicLong(0);

    public OkcoinTradeApplication(Long accountId, String apiKey, String secretKey) {
        super(apiKey, secretKey);

        this.accountId = accountId;

        tradeRequestCreator = new TradeRequestCreator(apiKey, secretKey);
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

    private void sendMessage(Message message) {
        sendMessage(sessionIds.get((int) (index.get() % sessionIds.size())), message);
    }

    public void placeOrder(Order order) {
        sendMessage(tradeRequestCreator.createNewOrderSingle(order.getInternalId(), getSide(order.getType()),
                OrdType.LIMIT, order.getAmount(), order.getPrice(), order.getSymbol()));
    }

    public void cancelOrder(Order order, SessionID sessionId) {
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
        sendMessage(tradeRequestCreator.createOrderCancelRequest(clOrdId != null ? clOrdId : System.nanoTime() + "",
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
        sendMessage(tradeRequestCreator.createOrderMassStatusRequest(massStatusReqId, massStatusReqType));
    }

    public void requestTradeCaptureReportRequest(String tradeRequestId, String symbol) {
        sendMessage(tradeRequestCreator.createTradeCaptureReportRequest(tradeRequestId, symbol));
    }

    public void requestAccountInfo(String accReqId) {
        sendMessage(tradeRequestCreator.createAccountInfoRequest(accReqId));
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
        sendMessage(tradeRequestCreator.createOrdersInfoAfterSomeIDRequest(tradeRequestId, symbol, orderId, ordStatus));
    }

    @Override
    public void onMessage(ExecutionReport message, SessionID sessionId) throws FieldNotFound, UnsupportedMessageType,
            IncorrectTagValue {
        Order order = new Order();

        order.setAccountId(accountId);
        order.setSymbol(message.getSymbol().getValue());
        order.setOrderId(message.getOrderID().getValue());

        if (message.isSetClOrdID()) {
            order.setInternalId(message.getClOrdID().getValue().replace("\\", ""));
        }

        order.setAvgPrice(message.getAvgPx().getValue());

        if (message.isSetPrice()) {
            order.setPrice(message.getPrice().getValue());
        }

        if (message.isSetText()){
            order.setText(message.getText().getValue() + " " + sessionId.getSenderCompID());
        }

        order.setAmount(message.getOrderQty().getValue());

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
    }

    protected void onOrder(Order order){
    }
}
