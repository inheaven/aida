package ru.inheaven.aida.predictor.service;

import ru.inheaven.aida.predictor.util.VectorForecastSSA;

import javax.ejb.Singleton;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:07
 */
@Singleton
public class PredictorService {
    private VectorForecastSSA vectorForecastSSA = new VectorForecastSSA(1024, 512, 21, 34);

    public float getPrediction(float[] timeSeries){
        if (timeSeries.length < 1024){
            return 0;
        }

        try {
            return vectorForecastSSA.execute(timeSeries)[545];
        } catch (Exception e) {
            return 0;
        }
    }
}
