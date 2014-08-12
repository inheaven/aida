package ru.inheaven.aida.coin.entity;

/**
 * @author Anatoly Ivanov
 *         Date: 08.08.2014 12:50
 */
public class ExchangeCurrency {
    private ExchangeType exchangeType;
    private String currency;

    public ExchangeCurrency(ExchangeType exchangeType, String currency) {
        this.exchangeType = exchangeType;
        this.currency = currency;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExchangeCurrency that = (ExchangeCurrency) o;

        if (currency != null ? !currency.equals(that.currency) : that.currency != null) return false;
        if (exchangeType != that.exchangeType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = exchangeType != null ? exchangeType.hashCode() : 0;
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        return result;
    }
}
