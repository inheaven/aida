package ru.inhell.aida.matrix.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.common.service.IProcessListener;
import ru.inhell.aida.common.service.ProcessCommand;
import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;

import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 15:59
 */
@Singleton
public class MatrixService {
    private final static Logger log = LoggerFactory.getLogger(MatrixService.class);

    @EJB
    private MatrixBean matrixBean;

    @Asynchronous
    public Future<String> populateMatrixTable(String symbol, Date start, Date end, MatrixPeriodType periodType,
                                    IProcessListener<Matrix> listener, ProcessCommand command){
        command.start();
        log.info("Началась обработка");

        Matrix matrix = null;

        try {
            Calendar startCalendar = Calendar.getInstance();
            startCalendar.setTime(start);

            long next;
            switch (periodType) {
                case ONE_MINUTE:
                    next = 1000*60;
                    startCalendar.set(Calendar.SECOND, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    break;
                case ONE_HOUR:
                    next = 1000*60*60;
                    startCalendar.set(Calendar.MINUTE, 0);
                    startCalendar.set(Calendar.SECOND, 0);
                    startCalendar.set(Calendar.MILLISECOND, 0);
                    break;
                default:
                    throw new IllegalArgumentException();
            }

            Calendar endCalendar = Calendar.getInstance();

            for (Date date = startCalendar.getTime(); date.before(end); date.setTime(date.getTime() + next)){
                endCalendar.setTime(date);

                //skip non trading time
                if (endCalendar.get(Calendar.HOUR_OF_DAY) < 9){
                    continue;
                }

                if (command.isCancel()){
                    log.info("Обработка прервана пользователем");
                    break;
                }

                switch (periodType) {
                    case ONE_MINUTE:
                        endCalendar.set(Calendar.SECOND, 59);
                        endCalendar.set(Calendar.MILLISECOND, 999);
                        break;
                    case ONE_HOUR:
                        endCalendar.set(Calendar.MINUTE, 59);
                        endCalendar.set(Calendar.SECOND, 59);
                        endCalendar.set(Calendar.MILLISECOND, 999);
                        break;
                }

                List<Matrix> list = matrixBean.getMatrixListFromAllTrades(symbol, date, endCalendar.getTime(), periodType);

                for (Matrix m : list){
                    matrix = m;

                    m.setDate(date);

                    if (matrixBean.getMatrixId(m, periodType) == null){
                        matrixBean.save(m, periodType);

                        listener.processed(m);
                    }else {
                        listener.skipped(m);
                    }
                }
            }
        } catch (Exception e) {
            listener.error(matrix, e);
        }

        command.done();
        log.info("Обработка закончилась");

        return new AsyncResult<>("PROCESSED");
    }
}
