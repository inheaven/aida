package ru.inheaven.aida.coin.entity;

import com.xeiam.xchange.currency.CurrencyPair;
import ru.inheaven.aida.coin.util.TraderUtil;

import java.io.Serializable;

/**
 * @author Anatoly Ivanov
 *         Date: 005 05.08.14 12:41
 */
public class ExchangePair implements Serializable{
    private ExchangeName exchange;
    private String pair;

    public ExchangePair(ExchangeName exchange, String pair) {
        this.exchange = exchange;
        this.pair = pair;
    }

    public static ExchangePair of(ExchangeName exchange, CurrencyPair currencyPair) {
        return new ExchangePair(exchange, currencyPair.baseSymbol + "/" + currencyPair.counterSymbol);
    }

    public String getCurrency(){
        return TraderUtil.getCurrency(pair);
    }

    public ExchangeName getExchange() {
        return exchange;
    }

    public void setExchange(ExchangeName exchange) {
        this.exchange = exchange;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExchangePair that = (ExchangePair) o;

        return exchange == that.exchange && !(pair != null ? !pair.equals(that.pair) : that.pair != null);
    }

    @Override
    public int hashCode() {
        return 31 * (exchange != null ? exchange.hashCode() : 0) + (pair != null ? pair.hashCode() : 0);
    }
}
