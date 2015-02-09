package com.xeiam.xchange.bter.service.polling;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.bter.dto.account.BTERFunds;

import java.io.IOException;

public class BTERPollingAccountServiceRaw extends BTERBasePollingService {

  /**
   * Constructor
   *
   * @param exchange
   */
  public BTERPollingAccountServiceRaw(Exchange exchange) {

    super(exchange);
  }

  public BTERFunds getBTERAccountInfo() throws IOException {

    BTERFunds bterFunds = bter.getFunds(exchange.getExchangeSpecification().getApiKey(), signatureCreator, exchange.getNonceFactory());
    return handleResponse(bterFunds);
  }

}
