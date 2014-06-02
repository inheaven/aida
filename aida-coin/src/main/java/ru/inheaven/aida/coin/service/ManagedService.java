package ru.inheaven.aida.coin.service;

import org.apache.wicket.Application;
import org.apache.wicket.protocol.ws.IWebSocketSettings;
import org.apache.wicket.protocol.ws.WebSocketSettings;
import org.apache.wicket.protocol.ws.api.WebSocketPushBroadcaster;
import org.apache.wicket.protocol.ws.api.message.IWebSocketPushMessage;

import javax.annotation.Resource;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import javax.enterprise.concurrent.LastExecution;
import javax.enterprise.concurrent.ManagedScheduledExecutorService;
import javax.enterprise.concurrent.Trigger;
import java.util.Date;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 29.05.2014 5:35
 */
@Singleton
public class ManagedService {
    @Resource
    private ManagedScheduledExecutorService managedScheduledExecutorService;

    @EJB
    private TraderService traderService;

    public void startTestTickerUpdateManagedService(){
        managedScheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                Application application = Application.get("aida-coin");
                IWebSocketSettings webSocketSettings = WebSocketSettings.Holder.get(application);

                WebSocketPushBroadcaster broadcaster = new WebSocketPushBroadcaster(webSocketSettings.getConnectionRegistry());
                broadcaster.broadcastAll(application, new IWebSocketPushMessage() {
                    @Override
                    public String toString() {
                        return new Date().toString();
                    }
                });
            }
        }, new Trigger() {
            @Override
            public Date getNextRunTime(LastExecution lastExecutionInfo, Date taskScheduledTime) {
                return lastExecutionInfo != null
                        ? new Date(lastExecutionInfo.getRunEnd().getTime() + 1000)
                        : taskScheduledTime;
            }

            @Override
            public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
                return System.currentTimeMillis() - scheduledRunTime.getTime() > 60000;
            }
        });
    }
}
