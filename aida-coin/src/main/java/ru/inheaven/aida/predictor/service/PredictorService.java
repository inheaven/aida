package ru.inheaven.aida.predictor.service;

import ru.inheaven.aida.predictor.util.VectorForecastSSA;

import javax.ejb.Singleton;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:07
 */
@Singleton
public class PredictorService {
    private final static VectorForecastSSA VECTOR_FORECAST_SSA =  new VectorForecastSSA(2048, 1024, 32, 64);

    public double getPrediction(double[] timeSeries){
        if (timeSeries.length < 2048){
            return 0;
        }

        try {
            return VECTOR_FORECAST_SSA.execute(timeSeries)[2111];
        } catch (Exception e) {
            return 0;
        }
    }
}
