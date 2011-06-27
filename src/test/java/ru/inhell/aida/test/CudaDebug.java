package ru.inhell.aida.test;

import ru.inhell.aida.acml.ACML;
import ru.inhell.aida.cuda.CUDA_AIDA;
import ru.inhell.aida.cula.CULA;
import ru.inhell.aida.ssa.VectorForecastSSA;

import java.util.Arrays;
import java.util.Random;
import java.util.TreeSet;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 25.06.11 22:20
 */
public class CudaDebug {
    public static void main(String... args){
        Random random = new Random();

        int n = 1024;
        int count = 1000;

        float[] ts = new float[n + count];

        for (int i = 0; i < n; ++i){
            ts[i] = random.nextInt(20000);
        }

        int l = 512;
        int p = 256;
        int[] pp = new int[p];

        for (int i=0; i<p; ++i){
            pp[i] = i;
        }

        int m = 128;

        int f_size = n+l+m-1;

        float[] f1 = new float[f_size];
        float[] f2 = new float[f_size];
        float[] f3 = new float[f_size];
        float[] f4 = new float[f_size];
        float[] f_temp = new float[f_size];

        //JAVA
        long time = System.currentTimeMillis();
        VectorForecastSSA vectorForecastSSA = new VectorForecastSSA(n ,l, p, pp, m);
        for (int i=0; i < count; ++i) {
            vectorForecastSSA.execute(ts, f_temp);
            System.arraycopy(f_temp, 0, f1, i*f_size, f_size);
        }
        System.out.println("JAVA: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        for (int i=0; i < count; ++i) {
            ACML.jni().vssa(n,l,p,pp,m, ts, f_temp, 0);
            System.arraycopy(f_temp, 0, f2, i*f_size, f_size);
        }
        System.out.println("ACML: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        CULA.jni().vssa(n,l,p,pp,m, ts, f3, count);
        System.out.println("CULA: " + (System.currentTimeMillis() - time));

        time = System.currentTimeMillis();
        CUDA_AIDA.INSTANCE.vssa(n, l, p, pp, m, ts, f4, count);
        System.out.println("CUDA: " + (System.currentTimeMillis() - time));

        //check
        boolean check;

        check = true;
        for (int i=0; i < f_size*count; ++i){
            if (f1[i] - f2[i] > 0.1){
                check = false;
                break;
            }
        }
        System.out.println("JAVA-ACML Cheked:" + check);

        check = true;
        for (int i=0; i < f_size*count; ++i){
            if (f1[i] - f3[i] > 0.1){
                check = false;
                break;
            }
        }
        System.out.println("JAVA-CULA Cheked:" + check);

        check = true;
        for (int i=0; i < f_size*count; ++i){
            if (f1[i] - f4[i] > 0.1){
                check = false;
                break;
            }
        }
        System.out.println("JAVA-CUDA Cheked:" + check);
    }
}
