package com.xeiam.xchange.okcoin.service.polling;

import com.xeiam.xchange.Exchange;
import com.xeiam.xchange.okcoin.dto.account.OkCoinFutureUserInfo;
import com.xeiam.xchange.okcoin.dto.account.OkCoinUserInfo;

import java.io.IOException;

public class OkCoinAccountServiceRaw extends OKCoinBaseTradePollingService {


    /**
     * Constructor
     *
     * @param exchange
     */
    protected OkCoinAccountServiceRaw(Exchange exchange) {
        super(exchange);
    }

    public OkCoinUserInfo getUserInfo() throws IOException {
        return returnOrThrow(okCoin.getUserInfo(apikey, signatureCreator));
    }

    public OkCoinFutureUserInfo getFutureUserInfo() throws IOException {
        return returnOrThrow(okCoin.getUserFutureInfo(apikey, signatureCreator));
    }

}
