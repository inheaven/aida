package org.knowm.xchange.bittrex.dto.account;

import com.fasterxml.jackson.annotation.*;

import javax.annotation.Generated;
import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Generated("org.jsonschema2pojo")
@JsonPropertyOrder({"Currency", "Address"})
public class BittrexDepositAddress {

  @JsonProperty("Currency")
  private String currency;

  @JsonProperty("Address")
  private String address;

  @JsonIgnore private Map<String, Object> additionalProperties = new HashMap<String, Object>();

  @JsonProperty("Currency")
  public String getCurrency() {

    return currency;
  }

  @JsonProperty("Currency")
  public void setCurrency(String currency) {

    this.currency = currency;
  }

  @JsonProperty("Address")
  public String getAddress() {

    return address;
  }

  @JsonProperty("Address")
  public void setAddress(String address) {

    this.address = address;
  }

  @JsonAnyGetter
  public Map<String, Object> getAdditionalProperties() {

    return this.additionalProperties;
  }

  @JsonAnySetter
  public void setAdditionalProperty(String name, Object value) {

    this.additionalProperties.put(name, value);
  }
}
