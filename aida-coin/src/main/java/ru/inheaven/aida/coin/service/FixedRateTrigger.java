package ru.inheaven.aida.coin.service;

import javax.enterprise.concurrent.LastExecution;
import javax.enterprise.concurrent.Trigger;
import java.util.Date;

/**
 * @author Anatoly Ivanov
 *         Date: 005 05.08.14 13:04
 */
public class FixedRateTrigger implements Trigger {
    private Long period;

    public FixedRateTrigger(Long period) {
        this.period = period;
    }

    @Override
    public Date getNextRunTime(LastExecution lastExecutionInfo, Date taskScheduledTime) {
        return lastExecutionInfo != null
                ? new Date(lastExecutionInfo.getRunEnd().getTime() + period)
                : taskScheduledTime;
    }

    @Override
    public boolean skipRun(LastExecution lastExecutionInfo, Date scheduledRunTime) {
        return false;
    }
}
