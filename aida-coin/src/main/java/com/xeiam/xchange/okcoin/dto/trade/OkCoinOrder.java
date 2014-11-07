package com.xeiam.xchange.okcoin.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

public class OkCoinOrder {
    private final BigDecimal amount;
    private final String contractName;

    private final long orderId;

    private final int status;

    private final String symbol;

    private final String type;

    private final BigDecimal rate;

    private final BigDecimal dealAmount;

    private final BigDecimal fee;

    private final BigDecimal avgRate;

    private final Date createDate;

    private BigDecimal unitAmount;

    public OkCoinOrder(@JsonProperty("amount") final BigDecimal amount, @JsonProperty("contract_name") final String contractName,
                       @JsonProperty("order_id") final long orderId, @JsonProperty("status") final int status,
                       @JsonProperty("symbol") final String symbol, @JsonProperty("type") final String type,
                       @JsonProperty("price") final BigDecimal rate, @JsonProperty("deal_amount") final BigDecimal dealAmount,
                       @JsonProperty("fee") final BigDecimal fee, @JsonProperty("unit_amount") final BigDecimal unitAmount,
                       @JsonProperty("price_avg") final BigDecimal avgRate, @JsonProperty("created_date") final Date createDate) {
        this.orderId = orderId;
        this.status = status;
        this.symbol = symbol;
        this.type = type;
        this.rate = rate;
        this.amount = amount;
        this.dealAmount = dealAmount;
        this.fee = fee;
        this.avgRate = avgRate;
        this.createDate = createDate;
        this.contractName = contractName;
        this.unitAmount = unitAmount;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getContractName() {
        return contractName;
    }

    public long getOrderId() {
        return orderId;
    }

    public int getStatus() {
        return status;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getType() {
        return type;
    }

    public BigDecimal getRate() {
        return rate;
    }

    public BigDecimal getDealAmount() {
        return dealAmount;
    }

    public BigDecimal getFee() {
        return fee;
    }

    public BigDecimal getAvgRate() {
        return avgRate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public BigDecimal getUnitAmount() {
        return unitAmount;
    }
}
