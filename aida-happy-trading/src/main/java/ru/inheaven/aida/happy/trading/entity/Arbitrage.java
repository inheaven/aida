package ru.inheaven.aida.happy.trading.entity;

import ru.inhell.aida.common.entity.AbstractEntity;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @author inheaven on 07.08.2015 0:32.
 */
public class Arbitrage extends AbstractEntity{
    private Long strategyId;
    private BigDecimal delta;
    private Order openAsk;
    private Order openBid;
    private Order closeAsk;
    private Order closeBid;

    private Date created;
    private Date open;
    private Date closed;

    public Long getStrategyId() {
        return strategyId;
    }

    public void setStrategyId(Long strategyId) {
        this.strategyId = strategyId;
    }

    public BigDecimal getDelta() {
        return delta;
    }

    public void setDelta(BigDecimal delta) {
        this.delta = delta;
    }

    public Order getOpenAsk() {
        return openAsk;
    }

    public void setOpenAsk(Order openAsk) {
        this.openAsk = openAsk;
    }

    public Order getOpenBid() {
        return openBid;
    }

    public void setOpenBid(Order openBid) {
        this.openBid = openBid;
    }

    public Order getCloseAsk() {
        return closeAsk;
    }

    public void setCloseAsk(Order closeAsk) {
        this.closeAsk = closeAsk;
    }

    public Order getCloseBid() {
        return closeBid;
    }

    public void setCloseBid(Order closeBid) {
        this.closeBid = closeBid;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public Date getOpen() {
        return open;
    }

    public void setOpen(Date open) {
        this.open = open;
    }

    public Date getClosed() {
        return closed;
    }

    public void setClosed(Date closed) {
        this.closed = closed;
    }
}
