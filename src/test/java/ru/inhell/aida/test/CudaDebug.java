package ru.inhell.aida.test;

import ru.inhell.aida.acml.ACML;
import ru.inhell.aida.cuda.CUDA_AIDA;
import ru.inhell.aida.cuda.CUDA_AIDA_THREAD;
import ru.inhell.aida.ssa.BasicAnalysisSSA;
import ru.inhell.aida.ssa.VectorForecastSSA;

import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 25.06.11 22:20
 */
public class CudaDebug {
    static AtomicInteger done = new AtomicInteger();

    public static void main(String... args) throws ExecutionException, InterruptedException {
        Random random = new Random();

        int n = 1024*3-1;
        int count = 4;

        float[] ts = new float[n + count];

        for (int i = 0; i < n; ++i){
            ts[i] = random.nextInt(200);
//            ts[i] = n;
        }

        int l = n/2;
        int p = l-1;
        int[] pp = new int[p];

        for (int i=0; i<p; ++i){
            pp[i] = i;
        }

        int m = 2;

        int f_size = n+l+m-1;

        float[] f1 = new float[f_size*count];
        float[] f2 = new float[f_size*count];
        float[] f3 = new float[f_size*count];
        float[] f4 = new float[f_size*count];
        float[] f5 = new float[f_size*count];
        float[] f6 = new float[f_size*count];

        ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(5);

        long time = System.currentTimeMillis();

        //JAVA
//        VectorForecastSSA vectorForecastSSA = new VectorForecastSSA(n ,l, p, pp, m);
//        done.set(0);
//        for (int i=0; i < count; ++i) {
//            executor.execute(getJava(ts, f_size, f1, vectorForecastSSA, i));
//        }
//        while (done.get() < count){
//            Thread.sleep(10);
//        }
//
//        System.out.println("JAVA: " + (System.currentTimeMillis() - time));

        //SYEV
        time = System.currentTimeMillis();
        VectorForecastSSA v2 = new VectorForecastSSA(n ,l, p, pp, m, BasicAnalysisSSA.TYPE.SSYEV);
        done.set(0);
        for (int i=0; i < count; ++i) {
            executor.execute(getJava(ts, f_size, f6, v2, i));
        }
        while (done.get() < count){
            Thread.sleep(10);
        }

        System.out.println("JAVA SYEV: " + (System.currentTimeMillis() - time));

        //C ACML
//        time = System.currentTimeMillis();
//        done.set(0);
//        for (int i=0; i < count; ++i) {
//            executor.execute(getC(n, ts, l, p, pp, m, f_size, f2, i));
//        }
//        while (done.get() < count){
//            Thread.sleep(10);
//        }
//        System.out.println("ACML: " + (System.currentTimeMillis() - time));

        //C ACML DD
        time = System.currentTimeMillis();
        done.set(0);
        for (int i=0; i < count; ++i) {
            executor.execute(getC_DD(n, ts, l, p, pp, m, f_size, f5, i));
        }
        while (done.get() < count){
            Thread.sleep(10);
        }
        System.out.println("ACML_DD: " + (System.currentTimeMillis() - time));
//
        ScheduledThreadPoolExecutor executor2 = new ScheduledThreadPoolExecutor(10);

        //CUDAx1
        time = System.currentTimeMillis();
        done.set(0);
        for (int i=0; i < count; ++i) {
            executor2.execute(getCuda(n, ts, l, p, pp, m, f_size, f3, i));
        }
        while (done.get() < count){
            Thread.sleep(10);
        }
        System.out.println("CUDAx1: " + (System.currentTimeMillis() - time));

        //Executor Shutdown
        executor.shutdown();
        executor2.shutdown();

        //CUDA+
//        time = System.currentTimeMillis();
//        CUDA_AIDA_THREAD.get().vssa(n, l, p, pp, m, ts, f4, count);
//        System.out.println("CUDA: " + (System.currentTimeMillis() - time));

        //check
        boolean check;

        check = true;
        for (int i=0; i < f_size*count; ++i){
            if (f1[i] - f6[i] > 1){
                check = false;
                break;
            }
        }
        System.out.println("JAVA-SYEV Cheked:" + check);

        check = true;
        for (int i=0; i < f_size*count; ++i){
            if (f1[i] - f2[i] > 1){
                check = false;
                break;
            }
        }
        System.out.println("JAVA-ACML Cheked:" + check);

        check = true;
        for (int i=0; i < f_size*count; ++i){
            if (f1[i] - f5[i] > 1){
                check = false;
                break;
            }
        }
        System.out.println("JAVA-ACML_DD Cheked:" + check);

        check = true;
        for (int i=0; i < f_size*count; ++i){
            if (f1[i] - f3[i] > 1){
                check = false;
                break;
            }
        }
        System.out.println("JAVA-CUDAx1 Cheked:" + check);

        check = true;
        for (int i=0; i < f_size*count; ++i){
            if (f1[i] - f4[i] > 1){
                check = false;
                break;
            }
        }
        System.out.println("JAVA-CUDA Cheked:" + check);
    }

    private static Runnable getCuda(final int n, final float[] ts, final int l, final int p, final int[] pp, final int m, final int f_size, final float[] f3, final int i) {
        return new Runnable() {
            @Override
            public void run() {
                try {
                    float[] f_temp = new float[f_size];
                    float[] ts_temp = new float[n];
                    System.arraycopy(ts, i, ts_temp, 0, n);
                    CUDA_AIDA_THREAD.get().vssa(n, l, p, pp, m, ts_temp, f_temp, 1);
                    System.arraycopy(f_temp, 0, f3, i*f_size, f_size);
                    done.incrementAndGet();
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            }
        };
    }


    private static Runnable getC(final int n, final float[] ts, final int l, final int p, final int[] pp, final int m, final int f_size, final float[] f2, final int i) {
        return new Runnable() {
            @Override
            public void run() {
                float[] f_temp = new float[f_size];
                float[] ts_temp = new float[n];
                System.arraycopy(ts, i, ts_temp, 0, n);
                ACML.jni().vssa(n,l,p,pp,m, ts_temp, f_temp, 1);
                System.arraycopy(f_temp, 0, f2, i*f_size, f_size);
                done.incrementAndGet();
            }
        };
    }

    private static Runnable getC_DD(final int n, final float[] ts, final int l, final int p, final int[] pp, final int m, final int f_size, final float[] f3,  final int i) {
        return new Runnable() {
            @Override
            public void run() {
                float[] f_temp = new float[f_size];
                float[] ts_temp = new float[n];
                System.arraycopy(ts, i, ts_temp, 0, n);
                ACML.jni().vssa(n,l,p,pp,m, ts_temp, f_temp, 0);
                System.arraycopy(f_temp, 0, f3, i*f_size, f_size);
                done.incrementAndGet();
            }
        };
    }

    private static Runnable getJava(final float[] ts, final int f_size, final float[] f1, final VectorForecastSSA vectorForecastSSA, final int i) {
        return new Runnable() {
            @Override
            public void run() {
                float[] f_temp = new float[f_size];
                float[] ts_temp = new float[vectorForecastSSA.getN()];
                System.arraycopy(ts, i, ts_temp, 0, vectorForecastSSA.getN());
                vectorForecastSSA.execute(ts_temp, f_temp);
                System.arraycopy(f_temp, 0, f1, i*f_size, f_size);
                done.incrementAndGet();
            }
        };
    }
}
