package ru.inhell.aida.common.wicket;

import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

/**
 * @author inheaven on 18.06.2015 13:32.
 */
public abstract class BroadcastBehavior extends WebSocketBehavior {
    private Class producer;

    public BroadcastBehavior(Class producer) {
        this.producer = producer;
    }

    @Override
    protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message) {
        if (message instanceof BroadcastPayload){
            BroadcastPayload p = (BroadcastPayload) message;

            if (producer.equals(p.getProducer())){
                onBroadcast(handler, p.getKey(), p.getPayload());
            }
        }

    }

    protected abstract void onBroadcast(WebSocketRequestHandler handler, String key, Object payload);
}