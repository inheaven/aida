package com.xeiam.xchange.cexio.service.polling;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.currency.CurrencyPair;
import com.xeiam.xchange.service.BaseExchangeService;
import com.xeiam.xchange.service.polling.BasePollingService;

import java.io.IOException;
import java.util.List;

/**
 * @author timmolter
 */
public class CexIOBasePollingService extends BaseExchangeService implements BasePollingService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public CexIOBasePollingService(Exchange exchange) {

    super(exchange);
  }

  @Override
  public List<CurrencyPair> getExchangeSymbols() throws IOException {

    // TODO call the public API and parse out the symbols.
    return exchange.getMetaData().getCurrencyPairs();
  }

}
