package ru.inhell.aida.oracle;

import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.Status;
import ru.inhell.aida.common.inject.AidaInjector;
import ru.inhell.aida.ssa.RemoteVSSAException;

import java.util.Calendar;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 05.05.11 0:02
 */
public class ScoreAll {
    public static void main(String... args){
        final AlphaOracleBean alphaOracleBean = AidaInjector.getInstance(AlphaOracleBean.class);
        final AlphaOracleService alphaOracleService = AidaInjector.getInstance(AlphaOracleService.class);

        final Calendar start = Calendar.getInstance();
        start.add(Calendar.MINUTE, - 720*7);

        final Calendar end = Calendar.getInstance();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

        for (final AlphaOracle alphaOracle : alphaOracleBean.getAlphaOracles()){
            if ((alphaOracle.getVectorForecast().getSymbol().equals("GZM1") ||  alphaOracle.getVectorForecast().getSymbol().equals("GZM1"))
                    && !alphaOracle.getStatus().equals(Status.ARCHIVE)){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            alphaOracleService.predict(alphaOracle, 720*7, true, false);
                            alphaOracleService.score(alphaOracle, start.getTime(), end.getTime());
                        } catch (RemoteVSSAException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        }
    }
}
