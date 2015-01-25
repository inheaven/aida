package com.xeiam.xchange.okcoin.service.polling;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.Order.OrderType;
import com.xeiam.xchange.dto.trade.LimitOrder;
import com.xeiam.xchange.dto.trade.MarketOrder;
import com.xeiam.xchange.dto.trade.OpenOrders;
import com.xeiam.xchange.dto.trade.UserTrades;
import com.xeiam.xchange.exceptions.ExchangeException;
import com.xeiam.xchange.exceptions.NotAvailableFromExchangeException;
import com.xeiam.xchange.exceptions.NotYetImplementedForExchangeException;
import com.xeiam.xchange.okcoin.OkCoinAdapters;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinOrderResult;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinTradeResult;
import com.xeiam.xchange.service.polling.trade.PollingTradeService;
import com.xeiam.xchange.service.polling.trade.params.TradeHistoryParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class OkCoinTradeService extends OkCoinTradeServiceRaw implements PollingTradeService {
    private static final OpenOrders noOpenOrders = new OpenOrders(Collections.<LimitOrder>emptyList());

    private final Logger log = LoggerFactory.getLogger(OkCoinTradeService.class);
    private final List<CurrencyPair> exchangeSymbols = (List<CurrencyPair>) getExchangeSymbols();


    public OkCoinTradeService(ExchangeSpecification exchangeSpecification) {

        super(exchangeSpecification);

    }

    @Override
    public OpenOrders getOpenOrders() throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {

        List<OkCoinOrderResult> orderResults = new ArrayList<OkCoinOrderResult>(exchangeSymbols.size());

        for (CurrencyPair symbol : exchangeSymbols) {
            log.debug("Getting order: {}", symbol);

            OkCoinOrderResult orderResult = getOrder(-1, OkCoinAdapters.adaptSymbol(symbol), "this_week", 1);
            if (orderResult.getOrders().length > 0) {
                orderResults.add(orderResult);
            }

            orderResult = getOrder(-1, OkCoinAdapters.adaptSymbol(symbol), "this_week", 2);
            if (orderResult.getOrders().length > 0) {
                orderResults.add(orderResult);
            }
        }

        if(orderResults.size() <= 0) {
            return noOpenOrders;
        }

        return OkCoinAdapters.adaptOpenOrders(orderResults);
    }

    @Override
    public String placeMarketOrder(MarketOrder marketOrder) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {

        String marketOrderType = null;
        String rate = null;
        String amount = null;

        if (marketOrder.getType().equals(OrderType.BID)) {
            marketOrderType = "buy_market";
            rate = marketOrder.getTradableAmount().toPlainString();
            amount = "1";
        }
        else {
            marketOrderType = "sell_market";
            rate = "1";
            amount = marketOrder.getTradableAmount().toPlainString();
        }

        long orderId = trade(OkCoinAdapters.adaptSymbol(marketOrder.getCurrencyPair()), marketOrderType, rate, amount).getOrderId();
        return String.valueOf(orderId);
    }

    @Override
    public String placeLimitOrder(LimitOrder limitOrder) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
        // type 1: open long position    2: open short position    3:liquidate long position    4: liquidate short position
        long orderId;
        if ("SHORT".equals(limitOrder.getId())){
            orderId = trade(OkCoinAdapters.adaptSymbol(limitOrder.getCurrencyPair()), "this_week",
                    limitOrder.getType() == OrderType.BID ? "4" : "2", limitOrder.getLimitPrice().toPlainString(),
                    limitOrder.getTradableAmount().toPlainString()).getOrderId();
        }else{
             orderId = trade(OkCoinAdapters.adaptSymbol(limitOrder.getCurrencyPair()), "this_week",
                    limitOrder.getType() == OrderType.BID ? "1" : "3", limitOrder.getLimitPrice().toPlainString(),
                    limitOrder.getTradableAmount().toPlainString()).getOrderId();
        }

        return String.valueOf(orderId);
    }

    @Override
    public boolean cancelOrder(String orderId) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {

        boolean ret = false;
        long id = Long.valueOf(orderId);

        for (CurrencyPair symbol : exchangeSymbols) {
            try {
                OkCoinTradeResult cancelResult = cancelOrder(id, OkCoinAdapters.adaptSymbol(symbol), "this_week");

                if (id == cancelResult.getOrderId()) {
                    ret = true;
                }
                break;
            } catch (ExchangeException e) {
                // order not found.
            }
        }
        return ret;
    }

    @Override
    public UserTrades getTradeHistory(Object... arguments) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {

        CurrencyPair currencyPair = arguments.length > 0 ? (CurrencyPair) arguments[0] : (useIntl ? CurrencyPair.BTC_USD : CurrencyPair.BTC_CNY);
        Integer page = arguments.length > 1 ? (Integer) arguments[1] : 0;

        OkCoinOrderResult orderHistory = getOrderHistory(OkCoinAdapters.adaptSymbol(currencyPair), "1", page.toString(), "1000");
        return OkCoinAdapters.adaptTrades(orderHistory);
    }

    @Override
    public UserTrades getTradeHistory(TradeHistoryParams tradeHistoryParams) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {
        return null;
    }

    @Override
    public TradeHistoryParams createTradeHistoryParams() {
        return null;
    }


}
