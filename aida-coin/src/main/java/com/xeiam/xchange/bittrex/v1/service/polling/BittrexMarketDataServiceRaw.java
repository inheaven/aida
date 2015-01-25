package com.xeiam.xchange.bittrex.v1.service.polling;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.bittrex.v1.Bittrex;
import com.xeiam.xchange.bittrex.v1.dto.marketdata.*;
import com.xeiam.xchange.exceptions.ExchangeException;

import java.io.IOException;
import java.util.ArrayList;

/**
 * <p>
 * Implementation of the market data service for Bittrex
 * </p>
 * <ul>
 * <li>Provides access to various market data values</li>
 * </ul>
 */
public class BittrexMarketDataServiceRaw extends BittrexBasePollingService<Bittrex> {

  /**
   * Constructor
   * 
   * @param exchangeSpecification The {@link ExchangeSpecification}
   */
  public BittrexMarketDataServiceRaw(ExchangeSpecification exchangeSpecification) {

    super(Bittrex.class, exchangeSpecification);
  }

  public BittrexCurrency[] getBittrexCurrencies() throws IOException {

    BittrexCurrenciesResponse response = bittrex.getCurrencies();

    if (response.isSuccess()) {
      return response.getCurrencies();
    }
    else {
      throw new ExchangeException("Bittrex returned an error: " + response.getMessage());
    }

  }
  
  public ArrayList<BittrexSymbol> getBittrexSymbols() throws IOException {

    BittrexSymbolsResponse response = bittrex.getSymbols();

    if (response.isSuccess()) {
      return response.getSymbols();
    }
    else {
      throw new ExchangeException("Bittrex returned an error: " + response.getMessage());
    }

  }
  
  public BittrexTicker getBittrexTicker(String pair) throws IOException {

    BittrexTickerResponse response = bittrex.getTicker(pair);

    if (response.getSuccess()) {
      return response.getTicker();
    }
    else {
      throw new ExchangeException("Bittrex returned an error: " + response.getMessage());
    }

  }
  
  public ArrayList<BittrexTicker> getBittrexTickers() throws IOException {

    BittrexTickersResponse response = bittrex.getTickers();

    if (response.isSuccess()) {
      return response.getTickers();
    }
    else {
      throw new ExchangeException("Bittrex returned an error: " + response.getMessage());
    }

  }

  public BittrexDepth getBittrexOrderBook(String pair, int depth) throws IOException {

    BittrexDepthResponse response = bittrex.getBook(pair, "both", depth);

    if (response.getSuccess()) {

      BittrexDepth bittrexDepth = response.getDepth();
      return bittrexDepth;
    }
    else {
      throw new ExchangeException("Bittrex returned an error: " + response.getMessage());
    }
  }

  public BittrexTrade[] getBittrexTrades(String pair, int count) throws IOException {

    BittrexTradesResponse response = bittrex.getTrades(pair, count);

    if (response.getSuccess()) {

      BittrexTrade[] bittrexTrades = response.getTrades();
      return bittrexTrades;
    }
    else {
      throw new ExchangeException("Bittrex returned an error: " + response.getMessage());
    }
  }
}
