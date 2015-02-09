package com.xeiam.xchange.cryptsy.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.cryptsy.dto.CryptsyGenericReturn;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ObsessiveOrange
 */
public class CryptsyOpenOrdersReturn extends CryptsyGenericReturn<List<CryptsyOpenOrders>> {

  /**
   * Constructor
   * 
   * @param success
   * @param value
   * @param error
   */
  public CryptsyOpenOrdersReturn(@JsonProperty("success") int success, @JsonProperty("return") List<CryptsyOpenOrders> value,
      @JsonProperty("error") String error) {

    super(success, (value == null ? new ArrayList<CryptsyOpenOrders>() : value), error);
  }
}
