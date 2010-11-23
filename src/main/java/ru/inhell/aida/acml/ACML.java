package ru.inhell.aida.acml;

import ru.inhell.aida.netlib.BLAS;
import ru.inhell.aida.netlib.LAPACK;

import java.util.logging.Logger;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 15.11.10 17:32
 */
public class ACML implements LAPACK, BLAS {
    private static ACML instance = new ACML();

    private boolean loaded;

    public boolean isLoaded(){
        return loaded;
    }

    public static ACML jni() {
        return instance;
    }

    private ACML() {
        Logger logger = Logger.getLogger("ru.inhell.aida");

        try {
            System.loadLibrary("aida");

            loaded = true;
            logger.config("ACML library loaded successfully.");
        } catch (Exception e) {
            loaded = false;
            logger.config("ACML library is not found.");
        }
    }

    @Override
    public native void dgesvd(String jobu, String jobvt, int m, int n, double[] a, int lda, double[] s, double[] u,
                              int ldu, double[] vt, int ldvt, int[] info);

    @Override
    public native void dgemm(String transa, String transb, int m, int n, int k, double alpha, double[] a, int lda,
                             double[] b, int ldb, double beta, double[] c, int ldc);
}