package com.xeiam.xchange.okcoin.service.polling;

import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.okcoin.dto.account.OkCoinUserInfo;

import java.io.IOException;

public class OkCoinAccountServiceRaw extends OKCoinBaseTradePollingService {

  protected OkCoinAccountServiceRaw(ExchangeSpecification exchangeSpecification) {

    super(exchangeSpecification);
  }

  public OkCoinUserInfo getUserInfo() throws IOException {

    OkCoinUserInfo userInfo = okCoin.getUserInfo(partner, signatureCreator);

    return returnOrThrow(userInfo);
  }

}
