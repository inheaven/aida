package ru.inheaven.aida.zaif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public class InfoResponse extends Response{
    public static class Info{
        private Map<String, BigDecimal> funds;
        private Map<String, BigDecimal> deposit;
        private Map<String, BigDecimal> rights;

        @JsonProperty("trade_count")
        private long tradeCount;

        @JsonProperty("open_orders")
        private long openOrders;

        @JsonProperty("server_time")
        private String serverTime;

        public Map<String, BigDecimal> getFunds() {
            return funds;
        }

        public void setFunds(Map<String, BigDecimal> funds) {
            this.funds = funds;
        }

        public Map<String, BigDecimal> getDeposit() {
            return deposit;
        }

        public void setDeposit(Map<String, BigDecimal> deposit) {
            this.deposit = deposit;
        }

        public Map<String, BigDecimal> getRights() {
            return rights;
        }

        public void setRights(Map<String, BigDecimal> rights) {
            this.rights = rights;
        }

        public long getTradeCount() {
            return tradeCount;
        }

        public void setTradeCount(long tradeCount) {
            this.tradeCount = tradeCount;
        }

        public long getOpenOrders() {
            return openOrders;
        }

        public void setOpenOrders(long openOrders) {
            this.openOrders = openOrders;
        }

        public String getServerTime() {
            return serverTime;
        }

        public void setServerTime(String serverTime) {
            this.serverTime = serverTime;
        }
    }


    @JsonProperty("return")
    private Info info;

    public Info getInfo() {
        return info;
    }

    public void setInfo(Info info) {
        this.info = info;
    }
}
