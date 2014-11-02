package ru.inheaven.aida.predictor.service;

import ru.inheaven.aida.predictor.util.VectorForecastSSA;

import javax.ejb.Singleton;
import java.util.Arrays;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:07
 */
@Singleton
public class PredictorService {
    public void test(){
        VectorForecastSSA vectorForecastSSA = new VectorForecastSSA(10, 5, 2, 2);

        System.out.println(Arrays.toString(vectorForecastSSA.execute(new float[]{0,1,2,3,4,5,6,7,8,9})));
    }
}
