package ru.inheaven.aida.happy.trading.entity;

import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

/**
 * @author inheaven on 17.06.2015 22:04.
 */
public class BroadcastPayload implements IWebSocketPushMessage {
    private Object payload;

    public BroadcastPayload(Object payload) {
        this.payload = payload;
    }

    public Object getPayload() {
        return payload;
    }
}
