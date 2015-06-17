package ru.inheaven.aida.happy.trading.service;

import com.google.common.util.concurrent.AbstractService;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.entity.BroadcastPayload;
import ru.inheaven.aida.happy.trading.web.AidaHappyTradingApplication;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * @author inheaven on 17.06.2015 22:02.
 */
@Singleton
public class BroadcastService {
    private Logger log = LoggerFactory.getLogger(AbstractService.class);

    private WebSocketPushBroadcaster broadcaster;

    @Inject
    private AidaHappyTradingApplication application;

    public void broadcast(Class producer, Object payload){
        try {
            if (broadcaster == null && payload != null){
                IWebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
                broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
            }

            broadcaster.broadcastAll(application, new BroadcastPayload(producer, payload));
        } catch (Exception e) {
            log.error("broadcast error", e);
        }
    }
}
