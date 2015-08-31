package com.xeiam.xchange.okcoin.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

public class OkCoinPositionResult extends OkCoinErrorResult {

    private final OkCoinPosition[] positions;
    private final BigDecimal forceLiquPrice;

    public OkCoinPositionResult(@JsonProperty("result") final boolean result, @JsonProperty("errorCode") final int errorCode,
                                @JsonProperty("force_liqu_price") final BigDecimal forceLiquPrice,
                                @JsonProperty("holding") final OkCoinPosition[] positions) {

        super(result, errorCode);
        this.positions = positions;
        this.forceLiquPrice = forceLiquPrice;
    }

    public OkCoinPosition[] getPositions() {
        return positions;
    }

    public BigDecimal getForceLiquPrice() {
        return forceLiquPrice;
    }
}
