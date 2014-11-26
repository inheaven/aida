package com.xeiam.xchange.okcoin.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * inheaven on 26.11.2014 2:50.
 */
public class OkCoinCrossPositionResult extends OkCoinErrorResult{
    private final OkCoinCrossPosition[] positions;

    public OkCoinCrossPositionResult(@JsonProperty("result") boolean result, @JsonProperty("error_code") int errorCode,
                                     @JsonProperty("holding") final OkCoinCrossPosition[] positions) {
        super(result, errorCode);

        this.positions = positions;
    }

    public OkCoinCrossPosition[] getPositions() {
        return positions;
    }
}
