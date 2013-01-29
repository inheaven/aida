package ru.inhell.aida.entity;

import ru.inhell.aida.common.entity.OrderType;

import java.util.Date;

/**
 * User: Anatoly A. Ivanov java@inhell.ru
 * Date: 03.01.11 14:39
 */
public class AlphaTraderData {
    private Long id;
    private Long alphaTraderId;
    private Date date;
    private float price;
    private long quantity;
    private OrderType order;
    private Long orderNum;
    private Integer result;
    private Integer replyCode;
    private String resultMessage;
    private String errorMessage;

    public AlphaTraderData() {
    }

    public AlphaTraderData(Long alphaTraderId, Date date, float price, long quantity, OrderType order) {
        this.alphaTraderId = alphaTraderId;
        this.date = date;
        this.price = price;
        this.quantity = quantity;
        this.order = order;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAlphaTraderId() {
        return alphaTraderId;
    }

    public void setAlphaTraderId(Long alphaTraderId) {
        this.alphaTraderId = alphaTraderId;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public long getQuantity() {
        return quantity;
    }

    public void setQuantity(long quantity) {
        this.quantity = quantity;
    }

    public OrderType getOrder() {
        return order;
    }

    public void setOrder(OrderType order) {
        this.order = order;
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public Integer getReplyCode() {
        return replyCode;
    }

    public void setReplyCode(Integer replyCode) {
        this.replyCode = replyCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }
}
