package com.xeiam.xchange.cryptsy.dto.marketdata;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.cryptsy.dto.CryptsyGenericReturn;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ObsessiveOrange
 */
public class CryptsyGetMarketsReturn extends CryptsyGenericReturn<List<CryptsyMarketData>> {

  /**
   * Constructor
   * 
   * @param success
   * @param value
   * @param error
   */
  public CryptsyGetMarketsReturn(@JsonProperty("success") int success, @JsonProperty("return") List<CryptsyMarketData> value,
      @JsonProperty("error") String error) {

    super(success, (value == null ? new ArrayList<CryptsyMarketData>() : value), error);
  }
}
