package com.xeiam.xchange.cryptsy.dto.account;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.xeiam.xchange.cryptsy.CryptsyUtils;

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.Date;
import java.util.Map;

/**
 * @author ObsessiveOrange
 */

@JsonIgnoreProperties(ignoreUnknown = true)
public class CryptsyAccountInfo {

  private final int openOrders;
  private final Date timeStamp;
  private final Map<String, BigDecimal> availableFunds;
  private final Map<String, BigDecimal> balancesHold;

  @JsonCreator
  public CryptsyAccountInfo(@JsonProperty("openordercount") Integer openordercount, @JsonProperty("serverdatetime") String timeStamp,
      @JsonProperty("balances_available") Map<String, BigDecimal> availableFunds, @JsonProperty("balances_hold") Map<String, BigDecimal> balancesHold) throws ParseException {

    this.openOrders = openordercount;
    this.timeStamp = timeStamp == null ? null : CryptsyUtils.convertDateTime(timeStamp);
    this.availableFunds = availableFunds;
    this.balancesHold = balancesHold;
  }

  public int getOpenOrders() {

    return openOrders;
  }

  public Date getTimeStamp() {

    return timeStamp;
  }

  public Map<String, BigDecimal> getAvailableFunds() {

    return availableFunds;
  }

    public Map<String, BigDecimal> getBalancesHold() {
        return balancesHold;
    }

    @Override
  public String toString() {

    return "CryptsyAccountInfo[" + "availableFunds='" + availableFunds + "', Balance Hold='" + balancesHold + "',Open Orders='" + openOrders + "',TimeStamp='" + timeStamp + "']";
  }
}
