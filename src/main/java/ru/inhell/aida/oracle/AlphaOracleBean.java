package ru.inhell.aida.oracle;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import org.apache.ibatis.session.SqlSessionManager;
import ru.inhell.aida.ssa.VectorForecast;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 16.03.11 15:46
 */
@Singleton
public class AlphaOracleBean {
    @Inject
    private SqlSessionManager session;

    private ScheduledThreadPoolExecutor executor;
    private Map<String, ScheduledFuture> scheduledFutures;

    private List<IAlphaOracleListener> listeners = new ArrayList<IAlphaOracleListener>();

    private VectorForecast vf1 = new VectorForecast(1000, 200, 14, 5);

    public void process(String symbol){
        executor.scheduleAtFixedRate(getCommand(symbol), 0, 30, TimeUnit.SECONDS);
    }

    public void addListener(IAlphaOracleListener listener){
        listeners.add(listener);
    }

    private void predicted(String symbol, AlphaOracleType type, Date date, float price){
        for (IAlphaOracleListener listener : listeners){
            listener.predicted(symbol, type, date, price);
        }
    }

    private VectorForecast getVectorForecast(){
        return vf1;
    }

    private Runnable getCommand(String symbol){
        return new Runnable() {
            @Override
            public void run() {
                //load quotes

                //process vssa

                //find max & min

                //store result
            }
        };
    }


}
