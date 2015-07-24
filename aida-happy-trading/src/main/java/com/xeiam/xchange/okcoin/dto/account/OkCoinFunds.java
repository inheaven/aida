package com.xeiam.xchange.okcoin.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

public class OkCoinFunds {

    private final Map<String, BigDecimal> free;
    private final Map<String, BigDecimal> freezed;
    private final Map<String, BigDecimal> asset;
    private final Map<String, BigDecimal> borrow;
    private final Map<String, BigDecimal> unionFund;


    public OkCoinFunds(@JsonProperty("free") final Map<String, BigDecimal> free,
                       @JsonProperty("asset") final Map<String, BigDecimal> asset,
                       @JsonProperty("freezed") final Map<String, BigDecimal> freezed,
                       @JsonProperty(value = "borrow", required = false) final Map<String, BigDecimal> borrow,
                       @JsonProperty(value = "union_fund", required = false) final Map<String, BigDecimal> unionFund) {

        this.free = free;
        this.freezed = freezed;
        this.asset = asset;
        this.borrow = borrow == null ? Collections.<String, BigDecimal> emptyMap() : borrow;
        this.unionFund = unionFund == null ? Collections.<String, BigDecimal> emptyMap() : unionFund;
    }

    public Map<String, BigDecimal> getFree() {

        return free;
    }

    public Map<String, BigDecimal> getFreezed() {

        return freezed;
    }

    public Map<String, BigDecimal> getAsset() {
        return asset;
    }

    public Map<String, BigDecimal> getBorrow() {

        return borrow;
    }

    public Map<String, BigDecimal> getUnionFund() {
        return unionFund;
    }
}
