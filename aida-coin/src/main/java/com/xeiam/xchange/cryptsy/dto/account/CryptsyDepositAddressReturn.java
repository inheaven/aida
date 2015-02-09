package com.xeiam.xchange.cryptsy.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.cryptsy.dto.CryptsyGenericReturn;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ObsessiveOrange
 */
public class CryptsyDepositAddressReturn extends CryptsyGenericReturn<Map<String, String>> {

  /**
   * Constructor
   * 
   * @param success True if successful
   * @param value The BTC-e account info
   * @param error Any error
   */
  public CryptsyDepositAddressReturn(@JsonProperty("success") int success, @JsonProperty("return") Map<String, String> value,
      @JsonProperty("error") String error) {

    super(success, (value == null ? new HashMap<String, String>() : value), error);
  }
}
