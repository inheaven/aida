package com.xeiam.xchange.cryptsy.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.cryptsy.dto.CryptsyGenericReturn;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ObsessiveOrange
 */
public class CryptsyTradeHistoryReturn extends CryptsyGenericReturn<List<CryptsyTradeHistory>> {

  /**
   * Constructor
   * 
   * @param success
   * @param value
   * @param error
   */
  public CryptsyTradeHistoryReturn(@JsonProperty("success") int success, @JsonProperty("return") List<CryptsyTradeHistory> value,
      @JsonProperty("error") String error) {

    super(success, (value == null ? new ArrayList<CryptsyTradeHistory>() : value), error);
  }
}
