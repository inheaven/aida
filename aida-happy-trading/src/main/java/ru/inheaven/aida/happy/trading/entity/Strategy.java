package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;

/**
 * @author inheaven on 02.05.2015 23:48.
 */
public class Strategy extends AbstractEntity {
    private StrategyType type;
    private Account account;
    private BigDecimal levelLot;
    private BigDecimal levelSpread;
    private BigDecimal levelSize;

    public StrategyType getType() {
        return type;
    }

    public void setType(StrategyType type) {
        this.type = type;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public BigDecimal getLevelLot() {
        return levelLot;
    }

    public void setLevelLot(BigDecimal levelLot) {
        this.levelLot = levelLot;
    }

    public BigDecimal getLevelSpread() {
        return levelSpread;
    }

    public void setLevelSpread(BigDecimal levelSpread) {
        this.levelSpread = levelSpread;
    }

    public BigDecimal getLevelSize() {
        return levelSize;
    }

    public void setLevelSize(BigDecimal levelSize) {
        this.levelSize = levelSize;
    }
}
