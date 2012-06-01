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

    private Map<MatrixCacheKey, Matrix> cache = new ConcurrentHashMap<>();
    private Map<MatrixPeriodCacheKey, List<Matrix>> cachePeriod = new ConcurrentHashMap<>();

    public List<Matrix> getMatrixList(String symbol, Date start,  Date end, MatrixPeriodType type){
        MatrixPeriodCacheKey key = new MatrixPeriodCacheKey(symbol, start, end, type);

        List<Matrix> list = cachePeriod.get(key);

        if (list == null){
            list = new ArrayList<>();

            //todo add scan from local cache and load necessary data
            List<Matrix> db = matrixBean.getMatrixList(symbol, start, end, type);

            for (Matrix m : db){
                MatrixCacheKey k = new MatrixCacheKey(m.getSymbol(), m.getDate(), m.getTransaction());

                Matrix cm = cache.get(k);

                if (cm == null){
                    cm = m;
                    cache.put(k, m);
                }

                list.add(cm);
            }

            list = Collections.unmodifiableList(list);

            cachePeriod.put(key, list);
        }

        return list;
    }
}
