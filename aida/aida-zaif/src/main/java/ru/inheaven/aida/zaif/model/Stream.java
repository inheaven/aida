package ru.inheaven.aida.zaif.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Anatoly A. Ivanov
 * Date: 09.08.2017.
 */
@JsonIgnoreProperties(ignoreUnknown=true)
public class Stream {
    public static class Price {
        private String action;
        private double price;

        public String getAction() {
            return action;
        }

        public void setAction(String action) {
            this.action = action;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }
    }

    @JsonFormat(shape = JsonFormat.Shape.ARRAY)
    public static class Order {
        private double price;
        private double value;

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public double getValue() {
            return value;
        }

        public void setValue(double value) {
            this.value = value;
        }
    }

    @JsonIgnoreProperties(ignoreUnknown=true)
    public static class Trade{
        @JsonProperty("currency_pair")
        private String currencyPair;

        @JsonProperty("trade_type")
        private String tradeType;

        private double price;
        private long tid;
        private double amount;
        private String date;

        public String getCurrencyPair() {
            return currencyPair;
        }

        public void setCurrencyPair(String currencyPair) {
            this.currencyPair = currencyPair;
        }

        public String getTradeType() {
            return tradeType;
        }

        public void setTradeType(String tradeType) {
            this.tradeType = tradeType;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public long getTid() {
            return tid;
        }

        public void setTid(long tid) {
            this.tid = tid;
        }

        public double getAmount() {
            return amount;
        }

        public void setAmount(double amount) {
            this.amount = amount;
        }

        public String getDate() {
            return date;
        }

        public void setDate(String date) {
            this.date = date;
        }
    }

    private List<Order> asks = new ArrayList<>();
    private List<Order> bids = new ArrayList<>();
    private List<Trade> trades;
    private String timestamp;

    @JsonProperty("last_price")
    private Price price;

    @JsonProperty("currency_pair")
    private String currencyPair;

    public List<Order> getAsks() {
        return asks;
    }

    public void setAsks(List<Order> asks) {
        this.asks = asks;
    }

    public List<Order> getBids() {
        return bids;
    }

    public void setBids(List<Order> bids) {
        this.bids = bids;
    }

    public List<Trade> getTrades() {
        return trades;
    }

    public void setTrades(List<Trade> trades) {
        this.trades = trades;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Price getLastPrice() {
        return price;
    }

    public void setLastPrice(Price price) {
        this.price = price;
    }

    public String getCurrencyPair() {
        return currencyPair;
    }

    public void setCurrencyPair(String currencyPair) {
        this.currencyPair = currencyPair;
    }
}
