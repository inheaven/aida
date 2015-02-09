package com.xeiam.xchange.okcoin;

import com.xeiam.xchange.BaseExchange;
import com.xeiam.xchange.ExchangeSpecification;
import com.xeiam.xchange.okcoin.service.polling.OkCoinAccountService;
import com.xeiam.xchange.okcoin.service.polling.OkCoinMarketDataService;
import com.xeiam.xchange.okcoin.service.polling.OkCoinTradeService;
import si.mazi.rescu.SynchronizedValueFactory;

public class OkCoinExchange extends BaseExchange {
    @Override
    public void applySpecification(ExchangeSpecification exchangeSpecification) {
        super.applySpecification(exchangeSpecification);

        this.pollingMarketDataService = new OkCoinMarketDataService(this);
        if (exchangeSpecification.getApiKey() != null) {
            this.pollingAccountService = new OkCoinAccountService(this);
            this.pollingTradeService = new OkCoinTradeService(this);
        }

        if (exchangeSpecification.getExchangeSpecificParametersItem("Use_Intl").equals(true)) {
            exchangeSpecification.setSslUri("https://www.okcoin.com/api");
            exchangeSpecification.setHost("www.okcoin.com");
        }
    }

    @Override
    public SynchronizedValueFactory<Long> getNonceFactory() {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ExchangeSpecification getDefaultExchangeSpecification() {

        ExchangeSpecification exchangeSpecification = new ExchangeSpecification(this.getClass().getCanonicalName());
        exchangeSpecification.setSslUri("https://www.okcoin.com/api");
        exchangeSpecification.setHost("www.okcoin.com");
        exchangeSpecification.setExchangeName("OKCoin");
        exchangeSpecification.setExchangeDescription("OKCoin is a globally oriented crypto-currency trading platform.");

        exchangeSpecification.setExchangeSpecificParametersItem("Intl_SslUri", "https://www.okcoin.com/api");
        exchangeSpecification.setExchangeSpecificParametersItem("Intl_Host", "www.okcoin.com");

        // set to true to automatically use the Intl_ parameters for ssluri and host
        exchangeSpecification.setExchangeSpecificParametersItem("Use_Intl", true);

        return exchangeSpecification;
    }

    @Override
    public String getMetaDataFileName(ExchangeSpecification exchangeSpecification) {

        if (exchangeSpecification.getExchangeSpecificParametersItem("Use_Intl").equals(false)) {
            return exchangeSpecification.getExchangeName().toLowerCase().replace(" ", "").replace("-", "").replace(".", "") + "_china";
        } else {
            return exchangeSpecification.getExchangeName().toLowerCase().replace(" ", "").replace("-", "").replace(".", "") + "_intl";

        }
    }

}
