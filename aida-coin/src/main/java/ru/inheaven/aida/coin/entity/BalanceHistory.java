package ru.inheaven.aida.coin.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 17.08.2014 19:18
 */
@Entity
@Table(name = "balance_history")
public class BalanceHistory extends AbstractEntity{
    @Column(nullable = false) @Enumerated(EnumType.STRING)
    private ExchangeType exchangeType;

    @Column(nullable = false)
    private String pair;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal balance;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal askAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal bidAmount;

    @Column(nullable = false, precision = 19, scale = 8)
    private BigDecimal price;

    @Column(nullable = false) @Temporal(TemporalType.TIMESTAMP)
    private Date date = new Date();

    public ExchangeType getExchangeType() {
        return exchangeType;
    }

    public void setExchangeType(ExchangeType exchangeType) {
        this.exchangeType = exchangeType;
    }

    public String getPair() {
        return pair;
    }

    public void setPair(String pair) {
        this.pair = pair;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public BigDecimal getAskAmount() {
        return askAmount;
    }

    public void setAskAmount(BigDecimal askAmount) {
        this.askAmount = askAmount;
    }

    public BigDecimal getBidAmount() {
        return bidAmount;
    }

    public void setBidAmount(BigDecimal bidAmount) {
        this.bidAmount = bidAmount;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BalanceHistory)) return false;

        BalanceHistory that = (BalanceHistory) o;

        if (askAmount != null ? !askAmount.equals(that.askAmount) : that.askAmount != null) return false;
        if (balance != null ? !balance.equals(that.balance) : that.balance != null) return false;
        if (bidAmount != null ? !bidAmount.equals(that.bidAmount) : that.bidAmount != null) return false;
        if (exchangeType != that.exchangeType) return false;
        if (pair != null ? !pair.equals(that.pair) : that.pair != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = exchangeType != null ? exchangeType.hashCode() : 0;
        result = 31 * result + (pair != null ? pair.hashCode() : 0);
        result = 31 * result + (balance != null ? balance.hashCode() : 0);
        result = 31 * result + (askAmount != null ? askAmount.hashCode() : 0);
        result = 31 * result + (bidAmount != null ? bidAmount.hashCode() : 0);
        return result;
    }
}
