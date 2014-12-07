package ru.inheaven.aida.predictor.service;

import ru.inheaven.aida.predictor.util.VectorForecastSSA;

import javax.ejb.Singleton;
import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:07
 */
@Singleton
@TransactionManagement(TransactionManagementType.BEAN)
public class PredictorService {
    public float getPrediction(float[] timeSeries){
        if (timeSeries.length < 1024){
            return 0;
        }

        try {
            return new VectorForecastSSA(1024, 512, 21, 34).execute(timeSeries)[545];
        } catch (Exception e) {
            return 0;
        }
    }
}
