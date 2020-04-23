package ru.inheaven.aida.common.web.wicket;

import org.apache.wicket.protocol.ws.api.WebSocketBehavior;
import org.apache.wicket.protocol.ws.api.WebSocketRequestHandler;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

/**
 * inheaven on 20.04.2016.
 */
public abstract class BroadcastBehavior<T> extends WebSocketBehavior {
    private Class<?> producer;

    public BroadcastBehavior(Class producer) {
        this.producer = producer;
    }

    @Override
    protected void onPush(WebSocketRequestHandler handler, IWebSocketPushMessage message) {
        if (message instanceof BroadcastPayload){
            @SuppressWarnings("unchecked")
            BroadcastPayload<T> p = (BroadcastPayload) message;

            if (producer.isAssignableFrom(p.getProducer())){
                onBroadcast(handler, p.getKey(), p.getPayload());
            }
        }
    }

    protected abstract void onBroadcast(WebSocketRequestHandler handler, String key, T payload);
}
