package ru.inheaven.aida.predictor.service;

import ru.inheaven.aida.predictor.util.VectorForecastSSA;

import javax.ejb.Singleton;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:07
 */
@Singleton
public class PredictorService {
    private VectorForecastSSA vectorForecastSSA = new VectorForecastSSA(100, 50, 3, 5);

    public float getPrediction(float[] timeSeries){
        if (timeSeries.length < 100){
            return 0;
        }

        try {
            return vectorForecastSSA.execute(timeSeries)[104];
        } catch (Exception e) {
            return 0;
        }
    }
}
