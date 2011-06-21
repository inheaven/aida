package ru.inhell.aida.test;

import ru.inhell.aida.acml.ACML;
import ru.inhell.aida.cula.CULA;
import ru.inhell.aida.oracle.VectorForecastBean;
import ru.inhell.aida.ssa.VectorForecastSSA;

import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 18.06.11 21:05
 */
public class CulaTest {
    public static void main(String... args){
        float[] timeseries = new float[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15,16};
        float[] forecast = new float[(12+5+2-1)*2];

        CULA.jni().vssa(12, 5, 2, new int[]{0,1}, 2, timeseries, forecast, 2);

        System.out.println(Arrays.toString(forecast));

        Arrays.fill(forecast, 0);

        ACML.jni().vssa(12, 5, 2, new int[]{0,1},2, timeseries, forecast, 0);

        System.out.println(Arrays.toString(forecast));

        System.out.println(Arrays.toString(new VectorForecastSSA(12,5,2,new int[]{0,1}, 2).execute(timeseries)));

    }
}
