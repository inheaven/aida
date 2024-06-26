package org.knowm.xchange.bittrex.service;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.bittrex.BittrexUtils;
import org.knowm.xchange.bittrex.dto.marketdata.*;
import org.knowm.xchange.currency.CurrencyPair;

import java.io.IOException;
import java.util.List;

public class BittrexMarketDataServiceRaw extends BittrexBaseService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public BittrexMarketDataServiceRaw(Exchange exchange) {

    super(exchange);
  }

  public List<BittrexCurrency> getBittrexCurrencies() throws IOException {

    return bittrexAuthenticated.getCurrencies().getResult();
  }

  public BittrexTicker getBittrexTicker(CurrencyPair currencyPair) throws IOException {
    return bittrexAuthenticated.getTicker(BittrexUtils.toPairString(currencyPair)).getResult();
  }

  public List<BittrexSymbol> getBittrexSymbols() throws IOException {

    return bittrexAuthenticated.getSymbols().getResult();
  }

  public BittrexMarketSummary getBittrexMarketSummary(String pair) throws IOException {

    List<BittrexMarketSummary> result = bittrexAuthenticated.getMarketSummary(pair).getResult();
    if (result == null || result.isEmpty()) {
      return null;
    }
    return result.get(0);
  }

  public List<BittrexMarketSummary> getBittrexMarketSummaries() throws IOException {

    return bittrexAuthenticated.getMarketSummaries().getResult();
  }

  public BittrexDepth getBittrexOrderBook(String pair, int depth) throws IOException {

    return bittrexAuthenticated.getBook(pair, "both", depth).getResult();
  }

  public List<BittrexTrade> getBittrexTrades(String pair) throws IOException {

    return bittrexAuthenticated.getTrades(pair).getResult();
  }

  public List<BittrexChartData> getBittrexChartData(
      CurrencyPair currencyPair, BittrexChartDataPeriodType periodType) throws IOException {

    return bittrexV2
        .getChartData(BittrexUtils.toPairString(currencyPair), periodType.getPeriod())
        .getResult();
  }

  public List<BittrexChartData> getBittrexLatestTick(
      CurrencyPair currencyPair, BittrexChartDataPeriodType periodType, Long timeStamp)
      throws IOException {
    return bittrexV2
        .getLatestTick(BittrexUtils.toPairString(currencyPair), periodType.getPeriod(), timeStamp)
        .getResult();
  }

  public List<BittrexV2MarketSummary> getBittrexV2MarketSummaries() {
    return bittrexV2.getMarketSummaries().getResult();
  }
}
