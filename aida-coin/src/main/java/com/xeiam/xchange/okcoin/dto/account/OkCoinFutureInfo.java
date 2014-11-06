package com.xeiam.xchange.okcoin.dto.account;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * @author Anatoly Ivanov
 *         Date: 06.11.2014 22:25
 */
public class OkCoinFutureInfo {
    private BigDecimal balance;
    private BigDecimal rights;

    public OkCoinFutureInfo(@JsonProperty("balance") BigDecimal balance, @JsonProperty("rights") BigDecimal rights) {
        this.balance = balance;
        this.rights = rights;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getRights() {
        return rights;
    }

    public void setRights(BigDecimal rights) {
        this.rights = rights;
    }
}
