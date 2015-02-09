package com.xeiam.xchange.okcoin.service.polling;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.dto.marketdata.OrderBook;
import com.xeiam.xchange.dto.marketdata.Ticker;
import com.xeiam.xchange.dto.marketdata.Trades;
import com.xeiam.xchange.okcoin.OkCoinAdapters;
import com.xeiam.xchange.service.polling.marketdata.PollingMarketDataService;

import java.io.IOException;

public class OkCoinMarketDataService extends OkCoinMarketDataServiceRaw implements PollingMarketDataService {


    public OkCoinMarketDataService(Exchange exchange) {
        super(exchange);
    }

    @Override
	public Ticker getTicker(CurrencyPair currencyPair, Object... args) throws IOException {
		return OkCoinAdapters.adaptTicker(getTicker(currencyPair, "this_week"), currencyPair);
	}

	@Override
	public OrderBook getOrderBook(CurrencyPair currencyPair, Object... args) throws IOException {
		return OkCoinAdapters.adaptOrderBook(getDepth(currencyPair, "this_week"), currencyPair);

	}

	@Override
	public Trades getTrades(CurrencyPair currencyPair, Object... args) throws IOException {
		return OkCoinAdapters.adaptTrades(getTrades(currencyPair, "this_week"), currencyPair);
	}
}
