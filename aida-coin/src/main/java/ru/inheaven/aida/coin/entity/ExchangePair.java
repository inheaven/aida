package ru.inheaven.aida.coin.entity;

import com.xeiam.xchange.currency.CurrencyPair;
import ru.inheaven.aida.coin.util.TraderUtil;

import java.io.Serializable;

/**
 * @author Anatoly Ivanov
 *         Date: 005 05.08.14 12:41
 */
public class ExchangePair implements Serializable{
    private ExchangeType exchangeType;
    private String pair;
    private TraderType traderType = TraderType.LONG;

    public ExchangePair(ExchangeType exchangeType, String pair) {
        this.exchangeType = exchangeType;
        this.pair = pair;
    }

    public ExchangePair(ExchangeType exchangeType, String pair, TraderType traderType) {
        this.exchangeType = exchangeType;
        this.pair = pair;
        this.traderType = traderType;
    }

    public static ExchangePair of(ExchangeType exchange, CurrencyPair currencyPair) {
        return new ExchangePair(exchange, currencyPair.baseSymbol + "/" + currencyPair.counterSymbol);
    }

    public static ExchangePair of(ExchangeType exchange, String pair) {
        return new ExchangePair(exchange, pair);
    }

    public String getCurrency(){
        return TraderUtil.getCurrency(pair);
    }

    public String getCounterSymbol(){
        return TraderUtil.getCounterSymbol(pair);
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public TraderType getTraderType() {
        return traderType;
    }

    public void setTraderType(TraderType traderType) {
        this.traderType = traderType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExchangePair that = (ExchangePair) o;

        if (exchangeType != that.exchangeType) return false;
        if (pair != null ? !pair.equals(that.pair) : that.pair != null) return false;
        if (traderType != that.traderType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = exchangeType != null ? exchangeType.hashCode() : 0;
        result = 31 * result + (pair != null ? pair.hashCode() : 0);
        result = 31 * result + (traderType != null ? traderType.hashCode() : 0);
        return result;
    }
}
