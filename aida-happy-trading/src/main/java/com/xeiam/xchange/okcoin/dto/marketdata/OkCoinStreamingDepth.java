package com.xeiam.xchange.okcoin.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;


public class OkCoinStreamingDepth extends OkCoinDepth {

  public OkCoinStreamingDepth(@JsonProperty("asks") final BigDecimal[][] asks, @JsonProperty("bids") final BigDecimal[][] bids, 
      @JsonProperty(required=false, value="timestamp") Date timestamp, @JsonProperty(required=false, value="unit_amount") int unitAmount) {
    super(asks, bids, timestamp);
  }

}
