package com.xeiam.xchange.okcoin.service.polling;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.okcoin.OkCoin;
import com.xeiam.xchange.okcoin.OkCoinAdapters;
import com.xeiam.xchange.okcoin.dto.marketdata.OkCoinDepth;
import com.xeiam.xchange.okcoin.dto.marketdata.OkCoinTickerResponse;
import com.xeiam.xchange.okcoin.dto.marketdata.OkCoinTrade;
import si.mazi.rescu.RestProxyFactory;

import java.io.IOException;
import java.util.Map;

public class OkCoinMarketDataServiceRaw extends OkCoinBasePollingService {

	private final OkCoin okCoin;

    public OkCoinMarketDataServiceRaw(Exchange exchange) {

        super(exchange);

        Map<String, Object> specific = exchange.getExchangeSpecification().getExchangeSpecificParameters();
        okCoin = RestProxyFactory.createProxy(OkCoin.class, exchange.getExchangeSpecification().getSslUri());
    }

	public OkCoinTickerResponse getTicker(CurrencyPair currencyPair) throws IOException {

		return okCoin.getTicker("1", OkCoinAdapters.adaptSymbol(currencyPair));
	}

    public OkCoinTickerResponse getTicker(CurrencyPair currencyPair, String prompt) throws IOException {

        return okCoin.getTicker("1", OkCoinAdapters.adaptSymbol(currencyPair), prompt.toLowerCase());
    }

	public OkCoinDepth getDepth(CurrencyPair currencyPair) throws IOException {

		return okCoin.getDepth("1", OkCoinAdapters.adaptSymbol(currencyPair));
	}

	public OkCoinDepth getDepth(CurrencyPair currencyPair, String prompt) throws IOException {

		return okCoin.getDepth("1", OkCoinAdapters.adaptSymbol(currencyPair), prompt.toLowerCase());
	}

	public OkCoinTrade[] getTrades(CurrencyPair currencyPair) throws IOException {

		return okCoin.getTrades("1", OkCoinAdapters.adaptSymbol(currencyPair));
	}

	public OkCoinTrade[] getTrades(CurrencyPair currencyPair, long since) throws IOException {

		return okCoin.getTrades("1", OkCoinAdapters.adaptSymbol(currencyPair), since);
	}

	public OkCoinTrade[] getTrades(CurrencyPair currencyPair, String prompt) throws IOException {

		return okCoin.getTrades("1", OkCoinAdapters.adaptSymbol(currencyPair), prompt.toLowerCase());
	}

}
