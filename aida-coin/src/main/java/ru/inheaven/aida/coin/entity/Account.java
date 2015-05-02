package ru.inheaven.aida.coin.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;

/**
 * @author inheaven on 01.05.2015 9:12.
 */
@Entity
public class Account extends AbstractEntity{
    @Column(nullable = false)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String apiKey;

    @Column(nullable = false)
    private String secretKey;

    @ManyToOne
    private Client client;


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

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
