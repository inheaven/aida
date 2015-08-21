package ru.inheaven.aida.happy.trading.fix;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.MsgType;
import quickfix.field.Password;
import quickfix.field.Username;
import quickfix.fix44.ExecutionReport;
import quickfix.fix44.MessageCracker;
import ru.inheaven.aida.happy.trading.fix.fix44.AccountInfoResponse;

import java.math.BigDecimal;

public class OKCoinApplication extends MessageCracker implements Application {

	private final Logger log = LoggerFactory.getLogger(OKCoinApplication.class);
	private final DataDictionary dataDictionary;
	private final MarketDataRequestCreator marketDataRequestCreator;
	private final TradeRequestCreator tradeRequestCreator;
	private final String apiKey;
	private final String secretKey;

	public OKCoinApplication(String apiKey, String secretKey) {
		this.apiKey = apiKey;
		this.secretKey = secretKey;
		this.marketDataRequestCreator = new MarketDataRequestCreator();
		this.tradeRequestCreator = new TradeRequestCreator(apiKey, secretKey);

		try {
			dataDictionary = new DataDictionary("FIX44.xml");
		} catch (ConfigError e) {
			throw new RuntimeException(e);
		}
	}

	public DataDictionary getDataDictionary() {
		return dataDictionary;
	}

	@Override
	public void onCreate(SessionID sessionId) {
	}

	@Override
	public void onLogon(SessionID sessionId) {
	}

	@Override
	public void onLogout(SessionID sessionId) {
	}

	@Override
	public void toAdmin(Message message, SessionID sessionId) {
		String msgType;
		try {
			msgType = message.getHeader().getString(MsgType.FIELD);
		} catch (FieldNotFound e) {
			throw new RuntimeException(e.getMessage(), e);
		}

		if (MsgType.LOGON.equals(msgType) || MsgType.HEARTBEAT.equals(msgType)) {
			message.setField(new Username(apiKey));
			message.setField(new Password(secretKey));
		}

		if (log.isTraceEnabled()) {
			log.trace("toAdmin: {}", message);
			log.trace("toAdmin: {}", message.toXML(dataDictionary));
		}
	}

	@Override
	public void fromAdmin(Message message, SessionID sessionId)
			throws FieldNotFound, IncorrectDataFormat, IncorrectTagValue,
			RejectLogon {
		if (log.isTraceEnabled()) {
			log.trace("fromAdmin: {}", message);
			log.trace("fromAdmin: {}", message.toXML(dataDictionary));
		}
	}

	@Override
	public void toApp(Message message, SessionID sessionId) throws DoNotSend {
		if (log.isTraceEnabled()) {
			log.trace("toApp: {}", message);
			log.trace("toApp: {}", message.toXML(dataDictionary));
		}
	}

	@Override
	public void fromApp(Message message, SessionID sessionId) throws FieldNotFound, IncorrectDataFormat,
			IncorrectTagValue, UnsupportedMessageType {
		if (log.isTraceEnabled()) {
			log.trace("fromApp: {}", message);
			log.trace("fromApp: {}", message.toXML(dataDictionary));
		}
		crack(message, sessionId);
	}

	@Override
	public void crack(quickfix.Message message, SessionID sessionId) throws UnsupportedMessageType, FieldNotFound,
            IncorrectTagValue {
		if (message instanceof AccountInfoResponse) {
			onMessage((AccountInfoResponse) message, sessionId);
		} else {
			super.crack(message, sessionId);
		}
	}

	@Override
	public void onMessage(ExecutionReport message, SessionID sessionId)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
	}

	public void onMessage(AccountInfoResponse message, SessionID sessionId)
			throws FieldNotFound, UnsupportedMessageType, IncorrectTagValue {
	}

	public void sendMessage(final Message message, final SessionID sessionId) {
		log.trace("sending message: {}", message);
		Session.lookupSession(sessionId).send(message);
	}

	public void requestMarketData(String mdReqId, String symbol, char subscriptionRequestType, int marketDepth,
			int mdUpdateType, char[] mdEntryTypes, SessionID sessionId) {
		sendMessage(marketDataRequestCreator.createMarketDataRequest( mdReqId, symbol, subscriptionRequestType,
                marketDepth, mdUpdateType, mdEntryTypes), sessionId);
	}

	/**
	 *
	 * @param mdReqId Unique ID assigned to this request.
	 * @param symbol Symbol, BTC/CNY or LTC/CNY.
	 * @param subscriptionRequestType 0 = Snapshot, 1 = Snapshot + Subscribe,
	 * 2 = Unsubscribe.
	 * @param marketDepth Applicable only to order book snapshot requests.
	 * Should be ignored otherwise.
	 * 0 = Full Book
	 * @param mdUpdateType 0 = Full Refresh, 1 = Incremental Refresh.
	 * @param sessionId FIX session ID.
	 */
	public void requestOrderBook(String mdReqId, String symbol, char subscriptionRequestType, int marketDepth,
			int mdUpdateType, SessionID sessionId) {
		sendMessage(marketDataRequestCreator.createOrderBookRequest(mdReqId, symbol, subscriptionRequestType,
                marketDepth, mdUpdateType), sessionId);
	}

	public void requestLiveTrades(String mdReqId, String symbol, SessionID sessionId) {
		sendMessage(marketDataRequestCreator.createLiveTradesRequest(mdReqId, symbol), sessionId);
	}

	public void request24HTicker(String mdReqId, String symbol,SessionID sessionId) {
		sendMessage(marketDataRequestCreator.create24HTickerRequest(mdReqId, symbol), sessionId);
	}

	public void placeOrder(String clOrdId, char side, char ordType, BigDecimal orderQty, BigDecimal price, String symbol,
                           SessionID sessionId) {
		sendMessage(tradeRequestCreator.createNewOrderSingle(clOrdId, side, ordType, orderQty, price, symbol), sessionId);
	}

	public void cancelOrder(String clOrdId, String origClOrdId, char side, String symbol, SessionID sessionId) {
		sendMessage(tradeRequestCreator.createOrderCancelRequest(clOrdId, origClOrdId, side, symbol), sessionId);
	}

	/**
	 * Request order status.
	 *
	 * @param massStatusReqId Client-assigned unique ID of this request.(or ORDERID)
	 * @param massStatusReqType Specifies the scope of the mass status request.
	 * 1 = status of a specified order(Tag584 is ORDERID)
	 * 7 = Status for all orders
	 * @param sessionId the FIX session ID.
	 */
	public void requestOrderMassStatus(String massStatusReqId, int massStatusReqType, SessionID sessionId) {
		sendMessage(tradeRequestCreator.createOrderMassStatusRequest(massStatusReqId, massStatusReqType), sessionId);
	}

	public void requestTradeCaptureReportRequest(String tradeRequestId, String symbol, SessionID sessionId) {
		sendMessage(tradeRequestCreator.createTradeCaptureReportRequest(tradeRequestId, symbol), sessionId);
	}

	public void requestAccountInfo(String accReqId, SessionID sessionId) {
		sendMessage(tradeRequestCreator.createAccountInfoRequest(accReqId), sessionId);
	}

	/**
	 * Request history order information which order ID is after the specified
	 * {@code orderId}.
	 *
	 * @param tradeRequestId Client-assigned unique ID of this request.
	 * @param symbol Symbol. BTC/CNY or LTC/CNY.
	 * @param orderId Order ID. Return 10 records after this id.
	 * @param ordStatus Order status. 0 = Not filled 1 = Fully filled.
	 * @param sessionId the FIX session ID.
	 */
	public void requestOrdersInfoAfterSomeID(String tradeRequestId, String symbol, long orderId, char ordStatus,
			SessionID sessionId) {
		sendMessage(tradeRequestCreator.createOrdersInfoAfterSomeIDRequest(tradeRequestId, symbol, orderId, ordStatus),
                sessionId);
	}

}
