package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 02.05.2015 23:48.
 */
public class Strategy extends AbstractEntity {
    private String name;
    private StrategyType type;

    private String symbol;
    private SymbolType symbolType;

    private BigDecimal levelLot;
    private BigDecimal levelSpread;
    private BigDecimal levelSideSpread;
    private BigDecimal levelSize;
    private boolean levelInverse;
    private boolean active;

    private Account account;

    private Date sessionStart;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public StrategyType getType() {
        return type;
    }

    public void setType(StrategyType type) {
        this.type = type;
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

    public BigDecimal getLevelSideSpread() {
        return levelSideSpread;
    }

    public void setLevelSideSpread(BigDecimal levelSideSpread) {
        this.levelSideSpread = levelSideSpread;
    }

    public BigDecimal getLevelSize() {
        return levelSize;
    }

    public void setLevelSize(BigDecimal levelSize) {
        this.levelSize = levelSize;
    }

    public boolean isLevelInverse() {
        return levelInverse;
    }

    public void setLevelInverse(boolean levelInverse) {
        this.levelInverse = levelInverse;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Date getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(Date sessionStart) {
        this.sessionStart = sessionStart;
    }
}
