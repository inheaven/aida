package ru.inheaven.aida.happy.trading.entity;

import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

import java.io.Serializable;

/**
 * @author inheaven on 17.06.2015 22:04.
 */
public class BroadcastPayload implements IWebSocketPushMessage, Serializable {
    private Class producer;
    private Object payload;

    public BroadcastPayload(Class producer, Object payload) {
        this.producer = producer;
        this.payload = payload;
    }

    public Class getProducer() {
        return producer;
    }

    public Object getPayload() {
        return payload;
    }
}
