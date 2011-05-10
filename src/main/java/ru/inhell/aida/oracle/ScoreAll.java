package ru.inhell.aida.oracle;

import ru.inhell.aida.entity.AlphaOracle;
import ru.inhell.aida.entity.Status;
import ru.inhell.aida.inject.AidaInjector;
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
        start.set(2011, 0, 1, 10, 30, 0);

        final Calendar end = Calendar.getInstance();

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(2);

        for (final AlphaOracle alphaOracle : alphaOracleBean.getAlphaOracles()){
            if (alphaOracle.getVectorForecast().getSymbol().equals("GAZP") && !alphaOracle.getStatus().equals(Status.ARCHIVE)){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try {
//                            alphaOracleService.predict(alphaOracle, 495*5*4*5, true, false);
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
