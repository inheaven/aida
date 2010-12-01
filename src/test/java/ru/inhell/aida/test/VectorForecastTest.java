package ru.inhell.aida.test;

import ru.inhell.aida.ssa.VectorForecast;
import ru.inhell.stock.core.VSSA;

import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 02.12.10 1:22
 */
public class VectorForecastTest {
    public static void main(String... args){
        VectorForecastTest vectorForecastTest = new VectorForecastTest();
        vectorForecastTest.test1();
    }

    private void test1(){
        float[] ts = new float[]{0,1,2,3,4,5,6,7,8,9};
        double[] tsD = new double[]{0,1,2,3,4,5,6,7,8,9};

        //VectorForecast
        System.out.println(Arrays.toString(new VectorForecast(10, 5, 3, 2).execute(ts)));

        //VSSA
        System.out.println(Arrays.toString(new VSSA(10, 5, 3, 2).execute(tsD)));
    }
}
