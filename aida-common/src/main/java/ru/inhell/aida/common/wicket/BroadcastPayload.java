package ru.inhell.aida.common.wicket;

import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

import java.io.Serializable;

/**
 * @author inheaven on 17.06.2015 22:04.
 */
public class BroadcastPayload implements IWebSocketPushMessage, Serializable {
    private Class producer;
    private String key;
    private Object payload;

    public BroadcastPayload(Class producer, String key, Object payload) {
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

    public Object getPayload() {
        return payload;
    }
}
