package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

/**
 * @author inheaven on 01.05.2015 9:12.
 */
public class Account extends AbstractEntity {
    private Long clientId;
    private ExchangeType exchangeType;
    private String apiKey;
    private String secretKey;

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public Long getClientId() {
        return clientId;
    }

    public void setClientId(Long clientId) {
        this.clientId = clientId;
    }
}
