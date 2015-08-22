package ru.inheaven.aida.happy.trading.service;

import com.google.common.util.concurrent.AbstractService;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inheaven.aida.happy.trading.web.AidaHappyTradingApplication;
import ru.inhell.aida.common.wicket.BroadcastPayload;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author inheaven on 17.06.2015 22:02.
 */
@Singleton
public class BroadcastService {
    private Logger log = LoggerFactory.getLogger(AbstractService.class);

    private WebSocketPushBroadcaster broadcaster;

    @Inject
    private AidaHappyTradingApplication application;

    private ExecutorService executorService = Executors.newWorkStealingPool();

    @SuppressWarnings("unchecked")
    public  <T> void broadcast(Class producer, String key, T payload){
        executorService.submit(()->{
            try {
                if (broadcaster == null){
                    WebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);
                    broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
                }

                broadcaster.broadcastAll(application, new BroadcastPayload(producer, key, payload));
            } catch (Exception e) {
                if (e instanceof IllegalStateException && e.getMessage().contains("TEXT_FULL_WRITING")){
                    return;
                }

                log.error("broadcast error", e);
            }
        });
    }
}
