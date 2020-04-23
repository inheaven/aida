package ru.inheaven.aida.zaif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public class TradeResponse extends Response{
    public static class Trade{
        private BigDecimal received;
        private BigDecimal remains;

        @JsonProperty("order_id")
        private Long orderId;

        private Map<String, BigDecimal> funds;

        public BigDecimal getReceived() {
            return received;
        }

        public void setReceived(BigDecimal received) {
            this.received = received;
        }

        public BigDecimal getRemains() {
            return remains;
        }

        public void setRemains(BigDecimal remains) {
            this.remains = remains;
        }

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Map<String, BigDecimal> getFunds() {
            return funds;
        }

        public void setFunds(Map<String, BigDecimal> funds) {
            this.funds = funds;
        }
    }

    @JsonProperty("return")
    private Trade trade;

    public Trade getTrade() {
        return trade;
    }

    public void setTrade(Trade trade) {
        this.trade = trade;
    }
}
