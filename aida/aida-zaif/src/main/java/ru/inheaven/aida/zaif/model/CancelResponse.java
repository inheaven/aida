package ru.inheaven.aida.zaif.model;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Map;

public class CancelResponse extends Response {
    public static class Cancel{
        @JsonProperty("order_id")
        private Long orderId;

        private Map<String, BigDecimal> funds;

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
    private Cancel cancel;

    public Cancel getCancel() {
        return cancel;
    }

    public void setCancel(Cancel cancel) {
        this.cancel = cancel;
    }

}
