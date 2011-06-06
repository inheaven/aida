package ru.inhell.aida.test;

import ru.inhell.aida.acml.ACML;
import ru.inhell.aida.entity.VectorForecast;
import ru.inhell.aida.ssa.VectorForecastSSA;

import java.util.Arrays;

/**
 * Created by IntelliJ IDEA.
 * User: anatoly
 * Date: 06.06.11
 * Time: 1:22
 * To change this template use File | Settings | File Templates.
 */
public class NativeVSSATest {
    public static void main(String... args){
        float[] forecast = new float[10 + 4 + 2 - 1];

        ACML.jni().vssa(10, 4, 2, new int[]{0,1}, 2, new float[]{2,2,3,4,5,6,7,8,9, 10}, forecast);

        System.out.println(Arrays.toString(forecast));

        System.out.println(Arrays.toString(new VectorForecastSSA(10, 4, 0, new int[]{0, 1}, 2).execute(new float[]{2, 2, 3, 4, 5, 6, 7, 8, 9, 10})));


    }
}
