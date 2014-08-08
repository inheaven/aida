package ru.inheaven.aida.coin.entity;

/**
 * @author Anatoly Ivanov
 *         Date: 08.08.2014 12:50
 */
public class ExchangeCurrency {
    private ExchangeName exchangeName;
    private String currency;

    public ExchangeCurrency(ExchangeName exchangeName, String currency) {
        this.exchangeName = exchangeName;
        this.currency = currency;
    }

    public ExchangeName getExchangeName() {
        return exchangeName;
    }

    public void setExchangeName(ExchangeName exchangeName) {
        this.exchangeName = exchangeName;
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
        if (exchangeName != that.exchangeName) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = exchangeName != null ? exchangeName.hashCode() : 0;
        result = 31 * result + (currency != null ? currency.hashCode() : 0);
        return result;
    }
}
