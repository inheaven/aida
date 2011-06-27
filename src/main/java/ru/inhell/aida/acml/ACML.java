package ru.inhell.aida.acml;

import org.slf4j.LoggerFactory;
import ru.inhell.aida.netlib.BLAS;
import ru.inhell.aida.netlib.LAPACK;

import java.util.logging.Logger;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.11.10 17:32
 */
public class ACML implements LAPACK, BLAS {
    private static ACML instance;

    private boolean loaded;

    public boolean isLoaded(){
        return loaded;
    }

    public static ACML jni() {
        if (instance == null){
            instance = new ACML();
        }

        return instance;
    }

    private ACML() {
        org.slf4j.Logger log = LoggerFactory.getLogger(ACML.class);

        if (!loaded){
            try {
                System.loadLibrary("acml_wrapper");

                loaded = true;
                log.info("ACML library loaded successfully.");
            } catch (UnsatisfiedLinkError e) {
                loaded = false;
                log.error("ACML library is not found.");
            }
        }

        if (!loaded) {
            try {
                ///media/Storage/Java/Projects-2010/ru.inhell.aida/aida/lib/acml_wrapper_gpu.so
                System.loadLibrary("acml_wrapper_gpu");

                loaded = true;
                log.info("ACML GPU library loaded successfully.");
            } catch (UnsatisfiedLinkError e) {
                loaded = false;
                log.error("ACML GPU library is not found.");
            }
        }

        if (!loaded){
            try {
                System.loadLibrary("acml_wrapper32_mp");

                loaded = true;
                log.info("ACML32 MP library loaded successfully.");
            } catch (UnsatisfiedLinkError e) {
                loaded = false;
                log.error("ACML32 MP library is not found.");
            }
        }

        if (!loaded){
            try {
                System.loadLibrary("acml_wrapper_mp");

                loaded = true;
                log.info("ACML MP library loaded successfully.");
            } catch (UnsatisfiedLinkError e) {
                loaded = false;
                log.error("ACML MP library is not found.");
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

    public native void vssa(int n, int l, int p, int[] pp, int m, float[] timeseries, float[] forecat, int svd);
}