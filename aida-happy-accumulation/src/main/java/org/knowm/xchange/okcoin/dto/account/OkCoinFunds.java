package org.knowm.xchange.okcoin.dto.account;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OkCoinFunds {

  private final Map<String, BigDecimal> free;
  private final Map<String, BigDecimal> freezed;
  private final Map<String, BigDecimal> borrow;
  private final Map<String, BigDecimal> asset;

  public OkCoinFunds(@JsonProperty(value = "asset") final Map<String, BigDecimal> asset,
                     @JsonProperty("free") final Map<String, BigDecimal> free,
                     @JsonProperty("freezed") final Map<String, BigDecimal> freezed,
                     @JsonProperty(value = "borrow", required = false) final Map<String, BigDecimal> borrow) {

    this.free = free;
    this.freezed = freezed;
    this.borrow = borrow == null ? Collections.<String, BigDecimal> emptyMap() : borrow;
    this.asset = asset;
  }

  public Map<String, BigDecimal> getFree() {

    return free;
  }

  public Map<String, BigDecimal> getFreezed() {

    return freezed;
  }

  public Map<String, BigDecimal> getBorrow() {

    return borrow;
  }

  public Map<String, BigDecimal> getAsset() {
    return asset;
  }
}
