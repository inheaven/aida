package ru.inhell.aida.acml;

import ru.inhell.aida.netlib.BLAS;
import ru.inhell.aida.netlib.LAPACK;

import java.util.logging.Logger;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.11.10 17:32
 */
public class ACML implements LAPACK, BLAS {
    private static ThreadLocal<ACML> localInstance = new ThreadLocal<ACML>();

    private boolean loaded;

    public boolean isLoaded(){
        return loaded;
    }

    public static ACML jni() {
        ACML acml = localInstance.get();

        if (acml == null){
            acml = new ACML();
            localInstance.set(acml);
        }

        return acml;
    }

    private ACML() {
        Logger logger = Logger.getLogger("ru.inhell.aida");

        try {
            System.loadLibrary("acml_wrapper_gpu");

            loaded = true;
            logger.config("ACML GPU library loaded successfully.");
            System.out.println("ACML GPU library loaded successfully.");
        } catch (UnsatisfiedLinkError e) {
            loaded = false;
            logger.config("ACML GPU library is not found.");
        }

        if (!loaded){
            try {
                System.loadLibrary("acml_wrapper_mp");

                loaded = true;
                logger.config("ACML MP library loaded successfully.");
                System.out.println("ACML MP library loaded successfully.");
            } catch (UnsatisfiedLinkError e) {
                loaded = false;
                logger.config("ACML MP library is not found.");
            }
        }

        if (!loaded){
            try {
                System.loadLibrary("acml_wrapper");

                loaded = true;
                logger.config("ACML library loaded successfully.");
                System.out.println("ACML library loaded successfully.");
            } catch (UnsatisfiedLinkError e) {
                loaded = false;
                logger.config("ACML library is not found.");
            }
        }
    }

    @Override
    public native void dgesvd(String jobu, String jobvt, int m, int n, double[] a, int lda, double[] s, double[] u,
                              int ldu, double[] vt, int ldvt, int[] info);

    @Override
    public native void dgemm(String transa, String transb, int m, int n, int k, double alpha, double[] a, int lda,
                             double[] b, int ldb, double beta, double[] c, int ldc);


    @Override
    public native void sgesvd(String jobu, String jobvt, int m, int n, float[] a, int lda, float[] s, float[] u,
                              int ldu, float[] vt, int ldvt, int[] info);

    @Override
    public native void sgesdd(String jobz, int m, int n, float[] a, int lda, float[] s, float[] u, int ldu, float[] vt,
                              int ldvt, int[] info);

    @Override
    public native void sgemm(String transa, String transb, int m, int n, int k, float alpha, float[] a, int lda,
                             float[] b, int ldb, float beta, float[] c, int ldc);

    public native void test(String test);
}