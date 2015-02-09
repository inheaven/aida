package com.xeiam.xchange.cryptsy.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.cryptsy.dto.CryptsyGenericReturn;

import java.util.HashMap;

public class CryptsyCurrencyPairsReturn extends CryptsyGenericReturn<HashMap<String, CryptsyMarketId>> {

  /**
   * Constructor
   * 
   * @param success
   * @param value
   * @param error
   */
  public CryptsyCurrencyPairsReturn(@JsonProperty("success") int success, @JsonProperty("return") HashMap<String, CryptsyMarketId> value,
      @JsonProperty("error") String error) {

    super(success, (value == null ? new HashMap<String, CryptsyMarketId>() : value), error);
  }
}
