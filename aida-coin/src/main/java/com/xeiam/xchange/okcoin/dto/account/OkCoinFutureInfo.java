package com.xeiam.xchange.okcoin.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author Anatoly Ivanov
 *         Date: 06.11.2014 22:25
 */
public class OkCoinFutureInfo {
    private BigDecimal accountRights;
    private BigDecimal keepDeposit;
    private BigDecimal profitReal;
    private BigDecimal profitUnreal;
    private BigDecimal riskRate;

    public OkCoinFutureInfo(@JsonProperty("account_rights") BigDecimal accountRights, @JsonProperty("keep_deposit") BigDecimal keepDeposit,
                            @JsonProperty("profit_real") BigDecimal profitReal, @JsonProperty("profit_unreal") BigDecimal profitUnreal,
                            @JsonProperty("risk_rate") BigDecimal riskRate) {
        this.accountRights = accountRights;
        this.keepDeposit = keepDeposit;
        this.profitReal = profitReal;
        this.profitUnreal = profitUnreal;
        this.riskRate = riskRate;
    }

    public BigDecimal getAccountRights() {
        return accountRights;
    }

    public BigDecimal getKeepDeposit() {
        return keepDeposit;
    }

    public BigDecimal getProfitReal() {
        return profitReal;
    }

    public BigDecimal getProfitUnreal() {
        return profitUnreal;
    }

    public BigDecimal getRiskRate() {
        return riskRate;
    }
}
