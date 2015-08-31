package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 01.09.2015 1:44.
 */
public class FuturesPosition extends AbstractEntity{
    private Long accountId;
    private String symbol;
    private SymbolType symbolType;

    private BigDecimal buyAmount;
    private BigDecimal buyAvailable;
    private BigDecimal buyPriceAvg;
    private BigDecimal buyPriceCost;
    private BigDecimal buyProfitReal;

    private BigDecimal sellAmount;
    private BigDecimal sellAvailable;
    private BigDecimal sellPriceAvg;
    private BigDecimal sellPriceCost;
    private BigDecimal sellProfitReal;

    private BigDecimal leverRate;
    private BigDecimal forceLiquPrice;
    private Long contractId;
    private Date contractDate;
    private Date created;

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public SymbolType getSymbolType() {
        return symbolType;
    }

    public void setSymbolType(SymbolType symbolType) {
        this.symbolType = symbolType;
    }

    public BigDecimal getBuyAmount() {
        return buyAmount;
    }

    public void setBuyAmount(BigDecimal buyAmount) {
        this.buyAmount = buyAmount;
    }

    public BigDecimal getBuyAvailable() {
        return buyAvailable;
    }

    public void setBuyAvailable(BigDecimal buyAvailable) {
        this.buyAvailable = buyAvailable;
    }

    public BigDecimal getBuyPriceAvg() {
        return buyPriceAvg;
    }

    public void setBuyPriceAvg(BigDecimal buyPriceAvg) {
        this.buyPriceAvg = buyPriceAvg;
    }

    public BigDecimal getBuyPriceCost() {
        return buyPriceCost;
    }

    public void setBuyPriceCost(BigDecimal buyPriceCost) {
        this.buyPriceCost = buyPriceCost;
    }

    public BigDecimal getBuyProfitReal() {
        return buyProfitReal;
    }

    public void setBuyProfitReal(BigDecimal buyProfitReal) {
        this.buyProfitReal = buyProfitReal;
    }

    public BigDecimal getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(BigDecimal sellAmount) {
        this.sellAmount = sellAmount;
    }

    public BigDecimal getSellAvailable() {
        return sellAvailable;
    }

    public void setSellAvailable(BigDecimal sellAvailable) {
        this.sellAvailable = sellAvailable;
    }

    public BigDecimal getSellPriceAvg() {
        return sellPriceAvg;
    }

    public void setSellPriceAvg(BigDecimal sellPriceAvg) {
        this.sellPriceAvg = sellPriceAvg;
    }

    public BigDecimal getSellPriceCost() {
        return sellPriceCost;
    }

    public void setSellPriceCost(BigDecimal sellPriceCost) {
        this.sellPriceCost = sellPriceCost;
    }

    public BigDecimal getSellProfitReal() {
        return sellProfitReal;
    }

    public void setSellProfitReal(BigDecimal sellProfitReal) {
        this.sellProfitReal = sellProfitReal;
    }

    public Long getContractId() {
        return contractId;
    }

    public void setContractId(Long contractId) {
        this.contractId = contractId;
    }

    public BigDecimal getLeverRate() {
        return leverRate;
    }

    public void setLeverRate(BigDecimal leverRate) {
        this.leverRate = leverRate;
    }

    public BigDecimal getForceLiquPrice() {
        return forceLiquPrice;
    }

    public void setForceLiquPrice(BigDecimal forceLiquPrice) {
        this.forceLiquPrice = forceLiquPrice;
    }

    public Date getContractDate() {
        return contractDate;
    }

    public void setContractDate(Date contractDate) {
        this.contractDate = contractDate;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FuturesPosition that = (FuturesPosition) o;

        if (accountId != null ? !accountId.equals(that.accountId) : that.accountId != null) return false;
        if (symbol != null ? !symbol.equals(that.symbol) : that.symbol != null) return false;
        if (symbolType != that.symbolType) return false;
        if (buyAmount != null ? !buyAmount.equals(that.buyAmount) : that.buyAmount != null) return false;
        if (buyAvailable != null ? !buyAvailable.equals(that.buyAvailable) : that.buyAvailable != null) return false;
        if (buyPriceAvg != null ? !buyPriceAvg.equals(that.buyPriceAvg) : that.buyPriceAvg != null) return false;
        if (buyPriceCost != null ? !buyPriceCost.equals(that.buyPriceCost) : that.buyPriceCost != null) return false;
        if (buyProfitReal != null ? !buyProfitReal.equals(that.buyProfitReal) : that.buyProfitReal != null)
            return false;
        if (sellAmount != null ? !sellAmount.equals(that.sellAmount) : that.sellAmount != null) return false;
        if (sellAvailable != null ? !sellAvailable.equals(that.sellAvailable) : that.sellAvailable != null)
            return false;
        if (sellPriceAvg != null ? !sellPriceAvg.equals(that.sellPriceAvg) : that.sellPriceAvg != null) return false;
        if (sellPriceCost != null ? !sellPriceCost.equals(that.sellPriceCost) : that.sellPriceCost != null)
            return false;
        if (sellProfitReal != null ? !sellProfitReal.equals(that.sellProfitReal) : that.sellProfitReal != null)
            return false;
        if (leverRate != null ? !leverRate.equals(that.leverRate) : that.leverRate != null) return false;
        if (forceLiquPrice != null ? !forceLiquPrice.equals(that.forceLiquPrice) : that.forceLiquPrice != null)
            return false;
        if (contractId != null ? !contractId.equals(that.contractId) : that.contractId != null) return false;
        if (contractDate != null ? !contractDate.equals(that.contractDate) : that.contractDate != null) return false;
        return !(created != null ? !created.equals(that.created) : that.created != null);

    }
}
