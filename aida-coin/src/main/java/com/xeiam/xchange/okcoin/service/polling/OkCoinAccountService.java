package com.xeiam.xchange.okcoin.service.polling;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.dto.account.AccountInfo;
import com.xeiam.xchange.exceptions.ExchangeException;
import com.xeiam.xchange.exceptions.NotAvailableFromExchangeException;
import com.xeiam.xchange.exceptions.NotYetImplementedForExchangeException;
import com.xeiam.xchange.okcoin.OkCoinAdapters;
import com.xeiam.xchange.service.polling.account.PollingAccountService;

import java.io.IOException;
import java.math.BigDecimal;

public class OkCoinAccountService extends OkCoinAccountServiceRaw implements PollingAccountService {


    /**
     * Constructor
     *
     * @param exchange
     */
    public OkCoinAccountService(Exchange exchange) {
        super(exchange);
    }

    /**
   * {@inheritDoc}
   */
  @Override
  public AccountInfo getAccountInfo() throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {

    return OkCoinAdapters.adaptAccountInfo(getFutureUserInfo());
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String withdrawFunds(String currency, BigDecimal amount, String address) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {

    throw new NotAvailableFromExchangeException();
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String requestDepositAddress(String currency, String... args) throws ExchangeException, NotAvailableFromExchangeException, NotYetImplementedForExchangeException, IOException {

    throw new NotAvailableFromExchangeException();
  }

}
