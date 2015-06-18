package ru.inheaven.aida.coin.service;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.coin.entity.ExchangeMessage;
import ru.inheaven.aida.coin.entity.ExchangeType;

import javax.ejb.Asynchronous;

/**
 * @author inheaven on 26.02.2015 23:40.
 */
public abstract class AbstractService {
    private Logger log = LoggerFactory.getLogger(AbstractService.class);

    private WebSocketPushBroadcaster broadcaster;

    @Asynchronous
    protected void broadcast(ExchangeType exchange, Object payload){
        try {
            if (payload != null) {
                Application application = Application.get("aida-coin");

                if (broadcaster == null){
                    IWebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
                    broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
                }

                broadcaster.broadcastAll(application, new ExchangeMessage<>(exchange, payload));
            }
        } catch (Exception e) {
            log.error("broadcast error", e);
        }
    }
}
