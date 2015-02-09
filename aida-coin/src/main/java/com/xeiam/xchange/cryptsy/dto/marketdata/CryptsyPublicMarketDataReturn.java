package com.xeiam.xchange.cryptsy.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.cryptsy.dto.CryptsyGenericReturn;

import java.util.Map;

public class CryptsyPublicMarketDataReturn extends CryptsyGenericReturn<Map<String, CryptsyPublicMarketData>> {

  public CryptsyPublicMarketDataReturn(@JsonProperty("success") int success,
      @JsonProperty("return") Map<String, Map<String, CryptsyPublicMarketData>> value, @JsonProperty("error") String error) {

    super(success, value.get("markets"), error);
  }
}
