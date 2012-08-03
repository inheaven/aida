package ru.inhell.aida.matrix.service;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.service.IProcessListener;
import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.matrix.entity.MatrixType;

import javax.annotation.Resource;
import javax.ejb.*;
import javax.ejb.Timer;
import java.util.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 27.07.12 16:03
 */

@Singleton
public class MatrixTimerService {
    private final static Logger log = LoggerFactory.getLogger(MatrixTimerService.class);

    public static final long INTERVAL_DURATION = 500;

    @EJB
    private MatrixBean matrixBean;

    @Resource
    private TimerService timerService;

    private Multimap<MatrixType, IProcessListener<List<Matrix>>> listenerMap = HashMultimap.create();
    private Map<MatrixType, Timer> timerMap = new HashMap<>();
    private Map<MatrixType, Date> dateMap = new HashMap<>();

    public void addListener(MatrixType type, IProcessListener<List<Matrix>> listener){
        listenerMap.put(type, listener);

        if (!timerMap.containsKey(type)){
            //create timer
            Timer timer = timerService.createIntervalTimer(0, INTERVAL_DURATION, new TimerConfig(type, false));
            timerMap.put(type, timer);

            //init start date
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.DAY_OF_MONTH, 3);
            calendar.set(Calendar.HOUR_OF_DAY, 13);   //todo msk -3

            dateMap.put(type, calendar.getTime());
        }
    }

    public void removeListener(MatrixType type, IProcessListener<List<Matrix>> listener){
        listenerMap.remove(type, listener);

        if (!listenerMap.containsKey(type)){
            //cancel timer
            Timer timer = timerMap.get(type);
            timer.cancel();

            timerMap.remove(type);
        }
    }

    @Timeout
    public void onTimer(Timer timer){
        MatrixType type = (MatrixType) timer.getInfo();

        Collection<IProcessListener<List<Matrix>>> listeners = listenerMap.get(type);

        if (listeners != null && !listeners.isEmpty()){
            //List<Matrix> list = matrixBean.getMatrixStartList(type.getSymbol(), dateMap.get(type), type.getPeriodType());

            Date end = new Date(dateMap.get(type).getTime() + 1000*60);
            List<Matrix> list = matrixBean.getMatrixList(type.getSymbol(), dateMap.get(type), end, type.getPeriodType());

            if (!list.isEmpty()){
                //invoke listeners
                for (IProcessListener<List<Matrix>> listener : listeners){
                    try {
                        listener.processed(list);
                    } catch (Exception e) {
                        log.error("WTF", e);
                    }
                }

                //update start date
                //dateMap.put(type, list.get(list.size() - 1).getCreated());

            }

            dateMap.put(type, end); //todo debug
        }else {
            log.warn("Чета слушателей нет, таймер то не выключился");
        }
    }
}
