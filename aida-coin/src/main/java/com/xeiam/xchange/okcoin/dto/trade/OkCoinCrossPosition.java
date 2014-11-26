package com.xeiam.xchange.okcoin.dto.trade;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.util.Date;

/**
 * inheaven on 26.11.2014 2:51.
 *
 * buy_amount(double): long position amount(usd)
 * buy_available: available long postions that can be closed
 * buy_price_avg(double): average open price
 * buy_profit_real(double): long position realized gains/losses
 * contract_id(long): contract ID
 * create_date(long): open date
 * sell_amount(double): short position amount(usd)
 * sell_available: available short positions that can be closed
 * sell_price_avg(double): average open price
 * sell_profit_real(double): short position realized gains/losses
 * symbol: btc_usd: BTC, ltc_usd: LTC
 * contract_type:contract type
 */
public class OkCoinCrossPosition {
    private BigDecimal buyAmount;
    private BigDecimal buyAvailable;
    private BigDecimal buyPriceAvg;
    private BigDecimal buyProfitReal;
    private Long contractId;
    private Date createDate;
    private BigDecimal sellAmount;
    private BigDecimal sellAvailable;
    private BigDecimal sellPriceAvg;
    private BigDecimal sellProfitReal;
    private String symbol;
    private String contractType;

    public OkCoinCrossPosition(@JsonProperty("buy_amount") BigDecimal buyAmount,
                               @JsonProperty("buy_available") BigDecimal buyAvailable,
                               @JsonProperty("buy_price_avg") BigDecimal buyPriceAvg,
                               @JsonProperty("buy_profit_real") BigDecimal buyProfitReal,
                               @JsonProperty("contract_id") Long contractId,
                               @JsonProperty("create_date") Date createDate,
                               @JsonProperty("sell_amount") BigDecimal sellAmount,
                               @JsonProperty("sell_available") BigDecimal sellAvailable,
                               @JsonProperty("sell_price_avg") BigDecimal sellPriceAvg,
                               @JsonProperty("sell_profit_real") BigDecimal sellProfitReal,
                               @JsonProperty("symbol") String symbol,
                               @JsonProperty("contract_type") String contractType) {
        this.buyAmount = buyAmount;
        this.buyAvailable = buyAvailable;
        this.buyPriceAvg = buyPriceAvg;
        this.buyProfitReal = buyProfitReal;
        this.contractId = contractId;
        this.createDate = createDate;
        this.sellAmount = sellAmount;
        this.sellAvailable = sellAvailable;
        this.sellPriceAvg = sellPriceAvg;
        this.sellProfitReal = sellProfitReal;
        this.symbol = symbol;
        this.contractType = contractType;
    }

    public BigDecimal getBuyAmount() {
        return buyAmount;
    }

    public BigDecimal getBuyAvailable() {
        return buyAvailable;
    }

    public BigDecimal getBuyPriceAvg() {
        return buyPriceAvg;
    }

    public BigDecimal getBuyProfitReal() {
        return buyProfitReal;
    }

    public Long getContractId() {
        return contractId;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public BigDecimal getSellAmount() {
        return sellAmount;
    }

    public BigDecimal getSellAvailable() {
        return sellAvailable;
    }

    public BigDecimal getSellPriceAvg() {
        return sellPriceAvg;
    }

    public BigDecimal getSellProfitReal() {
        return sellProfitReal;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getContractType() {
        return contractType;
    }
}
