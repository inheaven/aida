package ru.inheaven.aida.common.web.wicket;

import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

import java.io.Serializable;

/**
 * inheaven on 20.04.2016.
 */
public class BroadcastPayload<T> implements IWebSocketPushMessage, Serializable {
    private Class producer;
    private String key;
    private T payload;

    public BroadcastPayload(Class producer, String key, T payload) {
        this.producer = producer;
        this.key = key;
        this.payload = payload;
    }

    public Class getProducer() {
        return producer;
    }

    public String getKey() {
        return key;
    }

    public T getPayload() {
        return payload;
    }
}
