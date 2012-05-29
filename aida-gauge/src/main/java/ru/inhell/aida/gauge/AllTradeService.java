package ru.inhell.aida.gauge;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import ru.inhell.aida.entity.AllTrade;
import ru.inhell.aida.quotes.AllTradeBean;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 20.12.11 23:18
 */
@Singleton
public class AllTradeService {
    @Inject
    private AllTradeBean allTradeBean;

    private Map<String, List<AllTrade>> allTradeMap = new ConcurrentHashMap<>();
    
    private int start = 0;
    
    public void start(String symbol){
        new ScheduledThreadPoolExecutor(1).scheduleAtFixedRate(command(symbol), 0, 500, TimeUnit.MILLISECONDS);
    }

    public Runnable command(final String symbol){
        return new Runnable() {
            @Override
            public void run() {
                Long count = allTradeBean.getAllTradesCount(symbol);

//                List<String> list = allTradeBean.
                

            }
        };
    }



}
