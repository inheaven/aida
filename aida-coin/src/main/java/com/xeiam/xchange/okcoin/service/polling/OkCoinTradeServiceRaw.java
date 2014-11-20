package com.xeiam.xchange.okcoin.service.polling;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinOrder;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinOrderResult;
import com.xeiam.xchange.okcoin.dto.trade.OkCoinTradeResult;

import java.io.IOException;

public class OkCoinTradeServiceRaw extends OKCoinBaseTradePollingService {

    protected OkCoinTradeServiceRaw(ExchangeSpecification exchangeSpecification) {

        super(exchangeSpecification);
    }

    public OkCoinTradeResult trade(String symbol, String type, String rate, String amount) throws IOException {
        OkCoinTradeResult tradeResult = okCoin.trade(partner, symbol, type, rate, amount, signatureCreator);

        return returnOrThrow(tradeResult);
    }

    public OkCoinTradeResult trade(String symbol, String prompt, String type, String rate, String amount) throws IOException {
        OkCoinTradeResult tradeResult = okCoin.trade(partner, symbol, prompt, type, rate, amount, signatureCreator, 0);

        return returnOrThrow(tradeResult);
    }


    public OkCoinTradeResult cancelOrder(long orderId, String symbol) throws IOException {
        OkCoinTradeResult tradeResult = okCoin.cancelOrder(partner, orderId, symbol, signatureCreator);

        return returnOrThrow(tradeResult);
    }

    public OkCoinTradeResult cancelOrder(long orderId, String symbol, String prompt) throws IOException {
        OkCoinTradeResult tradeResult = okCoin.cancelOrder(partner, orderId, symbol, prompt, signatureCreator);

        return returnOrThrow(tradeResult);
    }

    public OkCoinOrderResult getOrder(long orderId, String symbol) throws IOException {
        OkCoinOrderResult orderResult = okCoin.getOrder(partner, orderId, symbol, signatureCreator);

        return returnOrThrow(orderResult);
    }

    public OkCoinOrderResult getOrder(long orderId, String symbol, String prompt) throws IOException {
        OkCoinOrderResult orderResult = okCoin.getOrder(partner, orderId, symbol, prompt, "1", 0, 50, signatureCreator);

        if (!orderResult.isResult() && orderResult.getErrorCode() == 20015){
            return new OkCoinOrderResult(true, 20015, new OkCoinOrder[]{});
        }

        return returnOrThrow(orderResult);
    }

    public OkCoinOrderResult getOrder(long orderId, String symbol, String prompt, Integer page) throws IOException {
        OkCoinOrderResult orderResult = okCoin.getOrder(partner, orderId, symbol, prompt, "1", page, 50, signatureCreator);

        if (!orderResult.isResult() && orderResult.getErrorCode() == 20015){
            return new OkCoinOrderResult(true, 20015, new OkCoinOrder[]{});
        }

        return returnOrThrow(orderResult);
    }

    public OkCoinOrderResult getOrderHistory(String symbol, String status, String currentPage, String pageLength) throws IOException {

        OkCoinOrderResult orderResult = okCoin.getOrderHistory(partner, symbol, status, currentPage, pageLength, signatureCreator);
        return returnOrThrow(orderResult);
    }
}
