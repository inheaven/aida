package ru.inheaven.aida.coin.entity;

import com.xeiam.xchange.dto.account.AccountInfo;

import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 08.08.2014 12:40
 */
public class BalanceStat {
    private AccountInfo accountInfo;
    private Date date;

    public BalanceStat(AccountInfo accountInfo, Date date) {
        this.accountInfo = accountInfo;
        this.date = date;
    }

    public AccountInfo getAccountInfo() {
        return accountInfo;
    }

    public void setAccountInfo(AccountInfo accountInfo) {
        this.accountInfo = accountInfo;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
