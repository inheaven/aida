package  ru.inheaven.aida.happy.trading.fix;

import quickfix.field.*;
import quickfix.fix44.NewOrderSingle;
import quickfix.fix44.OrderCancelRequest;
import quickfix.fix44.OrderMassStatusRequest;
import quickfix.fix44.TradeCaptureReportRequest;
import ru.inheaven.aida.happy.trading.fix.field.AccReqID;
import ru.inheaven.aida.happy.trading.fix.fix44.AccountInfoRequest;
import ru.inheaven.aida.happy.trading.fix.fix44.OrdersInfoAfterSomeIDRequest;

import java.math.BigDecimal;
import java.util.Date;

/**
 * Utilities for creating trade request messages.
 */
public class TradeRequestCreator {

	private final String account;

	public TradeRequestCreator(String partner, String secretKey) {
		this.account = String.format("%s,%s", partner, secretKey);
	}

	public AccountInfoRequest createAccountInfoRequest(String accReqId) {
		AccountInfoRequest message = new AccountInfoRequest();
		message.set(new AccReqID(accReqId));
		message.set(new Account(account));
		return message;
	}

	public NewOrderSingle createNewOrderSingle(
			String clOrdId,
			char side,
			char ordType,
			BigDecimal orderQty,
			BigDecimal price,
			String symbol) {
		NewOrderSingle message = new NewOrderSingle(
				new ClOrdID(clOrdId),
				new Side(side),
				new TransactTime(new Date()),
				new OrdType(ordType));
		message.set(new Account(account));
		message.set(new OrderQty(orderQty.doubleValue()));
		message.set(new Price(price.doubleValue()));
		message.set(new Symbol(symbol));
		return message;
	}

	public OrderCancelRequest createOrderCancelRequest(
			String clOrdId,
			String origClOrdId,
			char side,
			String symbol) {
		OrderCancelRequest message = new OrderCancelRequest(
				new OrigClOrdID(origClOrdId),
				new ClOrdID(clOrdId),
				new Side(side),
				new TransactTime(new Date()));
		message.set(new Symbol(symbol));
		return message;
	}

	public OrderMassStatusRequest createOrderMassStatusRequest(
			String massStatusReqId,
			int massStatusReqType) {
		OrderMassStatusRequest message  = new OrderMassStatusRequest(
				new MassStatusReqID(massStatusReqId),
				new MassStatusReqType(massStatusReqType));
		return message;
	}

	public TradeCaptureReportRequest createTradeCaptureReportRequest(
			String tradeRequestId, String symbol) {
		TradeCaptureReportRequest message = new TradeCaptureReportRequest(
				new TradeRequestID(tradeRequestId),
				new TradeRequestType(
						TradeRequestType.MATCHED_TRADES_MATCHING_CRITERIA_PROVIDED_ON_REQUEST));
		message.set(new Symbol(symbol));
		return message;
	}

	public OrdersInfoAfterSomeIDRequest createOrdersInfoAfterSomeIDRequest(
			String tradeRequestId, String symbol, long orderId, char ordStatus) {
		OrdersInfoAfterSomeIDRequest message = new OrdersInfoAfterSomeIDRequest(
				new TradeRequestID(tradeRequestId),
				new Symbol(symbol),
				new OrderID(String.valueOf(orderId)),
				new OrdStatus(ordStatus));
		return message;
	}

}
