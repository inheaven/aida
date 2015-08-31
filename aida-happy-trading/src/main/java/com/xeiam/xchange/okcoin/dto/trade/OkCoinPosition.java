package com.xeiam.xchange.okcoin.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

/** Instances of this class represent all holdings related to a specific contract, e.g. `this_week`. If you have 2 open positions for
 * this week then all fields (buy/sell) will be fully populated. If you have multiple contracts open (`this_week`, `next_week`, ..) then each
 * contract will be its own instance.
 */
public class OkCoinPosition {
    private final long contractId;
    private final String symbol;
    private final String type;
    private final BigDecimal rate;
    private final Date createDate;

    private BigDecimal buyAmount;
    private BigDecimal buyAmountAvailable;
    private BigDecimal buyProfitReal;
    private BigDecimal buyPriceAvg;
    private BigDecimal buyPriceCost;

    private BigDecimal sellAmount;
    private BigDecimal sellAmountAvailable;
    private BigDecimal sellProfitReal;
    private BigDecimal sellPriceAvg;
    private BigDecimal sellPriceCost;

    public OkCoinPosition(@JsonProperty("contract_id") final long orderId,
                          @JsonProperty("symbol") final String symbol,
                          @JsonProperty("contract_type") final String type,
                          @JsonProperty("lever_rate") final BigDecimal rate,

                          @JsonProperty("buy_amount") final BigDecimal buyAmount,
                          @JsonProperty("buy_available") final BigDecimal buyAmountAvailable,
                          @JsonProperty("buy_profit_real") final BigDecimal buyProfitReal,
                          @JsonProperty("buy_price_avg") final BigDecimal buyPriceAvg,
                          @JsonProperty("buy_price_cost") final BigDecimal buyPriceCost,

                          @JsonProperty("sell_amount") final BigDecimal sellAmount,
                          @JsonProperty("sell_available") final BigDecimal sellAmountAvailable,
                          @JsonProperty("sell_profit_real") final BigDecimal sellProfitReal,
                          @JsonProperty("sell_price_avg") final BigDecimal sellPriceAvg,
                          @JsonProperty("sell_price_cost") final BigDecimal sellPriceCost,

                          @JsonProperty("create_date") final Date createDate) {

        this.contractId = orderId;
        this.symbol = symbol;
        this.type = type;
        this.rate = rate;

        this.buyAmount = buyAmount;
        this.buyAmountAvailable = buyAmountAvailable;
        this.buyProfitReal = buyProfitReal;
        this.buyPriceAvg = buyPriceAvg;
        this.buyPriceCost = buyPriceCost;

        this.sellAmount = sellAmount;
        this.sellAmountAvailable = sellAmountAvailable;
        this.sellProfitReal = sellProfitReal;
        this.sellPriceAvg = sellPriceAvg;
        this.sellPriceCost = sellPriceCost;

        this.createDate = createDate;
    }


    public long getContractId() {

        return contractId;
    }


    public String getSymbol() {

        return symbol;
    }


    public String getType() {

        return type;
    }


    public BigDecimal getRate() {

        return rate;
    }


    public Date getCreateDate() {

        return createDate;
    }


    public BigDecimal getBuyAmount() {

        return buyAmount;
    }


    public BigDecimal getBuyAmountAvailable() {

        return buyAmountAvailable;
    }


    public BigDecimal getBuyProfitReal() {

        return buyProfitReal;
    }


    public BigDecimal getBuyPriceAvg() {

        return buyPriceAvg;
    }


    public BigDecimal getSellAmount() {

        return sellAmount;
    }


    public BigDecimal getSellAmountAvailable() {

        return sellAmountAvailable;
    }


    public BigDecimal getSellProfitReal() {

        return sellProfitReal;
    }


    public BigDecimal getSellPriceAvg() {

        return sellPriceAvg;
    }

    public BigDecimal getBuyPriceCost() {
        return buyPriceCost;
    }

    public BigDecimal getSellPriceCost() {
        return sellPriceCost;
    }
}
