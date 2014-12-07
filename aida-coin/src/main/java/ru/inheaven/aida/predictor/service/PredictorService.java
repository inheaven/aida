package ru.inheaven.aida.predictor.service;

import ru.inheaven.aida.predictor.util.VectorForecastSSA;

import javax.ejb.Singleton;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:07
 */
@Singleton
public class PredictorService {
    private final static VectorForecastSSA VECTOR_FORECAST_SSA =  new VectorForecastSSA(512, 256, 8, 16);

    public double getPrediction(double[] timeSeries){
        if (timeSeries.length < 512){
            return 0;
        }

        try {
            return VECTOR_FORECAST_SSA.execute(timeSeries)[512 + 15];
        } catch (Exception e) {
            return 0;
        }
    }
}
