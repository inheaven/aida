package ru.inhell.aida.matrix.service;

import ru.inhell.aida.matrix.entity.Matrix;
import ru.inhell.aida.matrix.entity.MatrixPeriodType;

import javax.ejb.EJB;
import javax.ejb.Singleton;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 01.06.12 15:59
 */
@Singleton
public class MatrixService {
    @EJB
    private MatrixBean matrixBean;

    public void populateMatrixTable(String symbol, Date start, Date end, MatrixPeriodType periodType){
        long next;
        switch (periodType) {
            case ONE_MINUTE:
                next = 1000;
                break;
            case ONE_HOUR:
                next = 1000*60;
                break;
            default:
                throw new IllegalArgumentException();
        }

        Calendar calendar = Calendar.getInstance();

        //clear second
        calendar.setTime(start);
        calendar.set(Calendar.SECOND, 0);

        for (Date date = calendar.getTime(); date.before(end); date.setTime(date.getTime() + next)){
            calendar.setTime(date);

            //skip non trading time
            if (calendar.get(Calendar.HOUR_OF_DAY) < 10){
                continue;
            }

            calendar.set(Calendar.SECOND, 59);

            List<Matrix> list = matrixBean.getMatrixListFromAllTrades(symbol, date, calendar.getTime(), periodType);

            for (Matrix m : list){
                Matrix db = matrixBean.getMatrix(m, periodType);

                if (db == null){
                    matrixBean.save(m, periodType);
                }
            }
        }
    }
}
