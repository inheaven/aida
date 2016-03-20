package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import javax.json.*;
import java.io.StringReader;
import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 02.05.2015 23:48.
 */
public class Strategy extends AbstractEntity {
    private Long accountId;
    private StrategyType type;
    private String name;
    private String symbol;
    private SymbolType symbolType;
    private String parameters;
    private boolean active;
    private Date sessionStart;

    private JsonObject parameterJsonObject;

    public String getString(Enum name){
        return parameterJsonObject.getString(name.name().toLowerCase());
    }

    public BigDecimal getBigDecimal(Enum name){
        return parameterJsonObject.getJsonNumber(name.name().toLowerCase()).bigDecimalValue();
    }

    public Integer getInteger(Enum name){
        return parameterJsonObject.getJsonNumber(name.name().toLowerCase()).intValue();
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public StrategyType getType() {
        return type;
    }

    public void setType(StrategyType type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getParameters() {
        return parameters;
    }

    public void setParameters(String parameters) {
        this.parameters = parameters;

        parameterJsonObject = Json.createReader(new StringReader(parameters)).readObject();
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public Date getSessionStart() {
        return sessionStart;
    }

    public void setSessionStart(Date sessionStart) {
        this.sessionStart = sessionStart;
    }
}
