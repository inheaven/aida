package ru.inheaven.aida.coin.service;

import org.apache.wicket.atmosphere.EventBus;

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

    public void startTestTickerUpdateManagedService(EventBus eventBus){
        managedScheduledExecutorService.schedule(new Runnable() {
            @Override
            public void run() {
                eventBus.post(new Date().toString());
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
