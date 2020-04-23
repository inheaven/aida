package ru.inheaven.aida.okex.model;

import com.google.common.base.MoreObjects;

/**
 * @author Anatoly A. Ivanov
 * 17.08.2017 23:24
 */
public class Reject {
    private String clOrderId;
    private String orderId;
    private String status;
    private String origClOrderId;
    private String text;
    private String reason;
    private String responseTo;

    public String getClOrderId() {
        return clOrderId;
    }

    public void setClOrderId(String clOrderId) {
        this.clOrderId = clOrderId;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOrigClOrderId() {
        return origClOrderId;
    }

    public void setOrigClOrderId(String origClOrderId) {
        this.origClOrderId = origClOrderId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getResponseTo() {
        return responseTo;
    }

    public void setResponseTo(String responseTo) {
        this.responseTo = responseTo;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .omitNullValues()
                .add("clOrderId", clOrderId)
                .add("orderId", orderId)
                .add("status", status)
                .add("origClOrderId", origClOrderId)
                .add("text", text)
                .add("reason", reason)
                .add("responseTo", responseTo)
                .toString();
    }
}
