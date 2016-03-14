package ru.inheaven.aida.happy.trading.fix;

import com.google.common.base.Joiner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import quickfix.*;
import quickfix.field.*;
import quickfix.fix44.MarketDataSnapshotFullRefresh;
import ru.inheaven.aida.happy.trading.entity.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * inheaven on 14.03.2016.
 */
public abstract class OkcoinMarketApplication extends BaseApplication{
    private Logger log = LoggerFactory.getLogger(OkcoinMarketApplication.class);

    private MarketDataRequestCreator marketDataRequestCreator;

    private SessionID sessionId;

    private ExchangeType exchangeType;

    public OkcoinMarketApplication(ExchangeType exchangeType, String apiKey, String secretKey) {
        super(apiKey, secretKey);

        this.exchangeType = exchangeType;

        marketDataRequestCreator = new MarketDataRequestCreator();
    }

    @Override
    public void onCreate(SessionID sessionId) {
        super.onCreate(sessionId);

        this.sessionId = sessionId;
    }

    @Override
    public void onLogon(SessionID sessionId) {
        super.onLogon(sessionId);

        this.sessionId = sessionId;
    }

    @Override
    public void onLogout(SessionID sessionId) {
        super.onLogout(sessionId);
    }

    private void sendMessage(Message message) {
        sendMessage(sessionId, message);
    }

    public void requestMarketData(String mdReqId, String symbol, char subscriptionRequestType, int marketDepth,
                                  int mdUpdateType, char[] mdEntryTypes) {
        sendMessage(marketDataRequestCreator.createMarketDataRequest( mdReqId, symbol, subscriptionRequestType,
                marketDepth, mdUpdateType, mdEntryTypes));
    }

    public void requestOrderBook(String symbol) {
        sendMessage(marketDataRequestCreator.createOrderBookRequest(UUID.randomUUID().toString(), symbol));
    }

    public void requestLiveTrades(String symbol) {
        sendMessage(marketDataRequestCreator.createLiveTradesRequest(UUID.randomUUID().toString(), symbol));
    }

    public void request24HTicker(String mdReqId, String symbol) {
        sendMessage(marketDataRequestCreator.create24HTickerRequest(mdReqId, symbol));
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
                    trade.setExchangeType(exchangeType);
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

            depth.setExchangeType(exchangeType);
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
}
