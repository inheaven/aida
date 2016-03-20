package ru.inheaven.aida.happy.trading.entity;

import java.util.Objects;

/**
 * inheaven on 20.03.2016.
 */
public class CacheKey {
    private String method;
    private ExchangeType exchangeType;
    private String symbol;
    private Integer points;

    public CacheKey(String method, ExchangeType exchangeType, String symbol, Integer points) {
        this.method = method;
        this.exchangeType = exchangeType;
        this.symbol = symbol;
        this.points = points;
    }

    public String getMethod() {
        return method;
    }

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public String getSymbol() {
        return symbol;
    }

    public Integer getPoints() {
        return points;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheKey cacheKey = (CacheKey) o;
        return Objects.equals(method, cacheKey.method) &&
                exchangeType == cacheKey.exchangeType &&
                Objects.equals(symbol, cacheKey.symbol) &&
                Objects.equals(points, cacheKey.points);
    }

    @Override
    public int hashCode() {
        return Objects.hash(method, exchangeType, symbol, points);
    }
}
