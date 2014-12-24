package ru.inheaven.aida.predictor.service;

import ru.inheaven.aida.predictor.util.VectorForecastSSA;

import javax.ejb.Singleton;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:07
 */
@Singleton
public class PredictorService {
    public static final int SIZE = 1024;

    private final static VectorForecastSSA VECTOR_FORECAST_SSA =  new VectorForecastSSA(SIZE, 512, 4, 64);

    public double getPrediction(double[] timeSeries){
        if (timeSeries.length < SIZE){
            return 0;
        }

        try {
            return VECTOR_FORECAST_SSA.execute(timeSeries)[SIZE + 63];
        } catch (Exception e) {
            return 0;
        }
    }
}
