package ru.inheaven.aida.predictor.util;

import com.sun.jna.Native;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:29
 */
public class ACML {
    static {
        Native.register("c:\\dll\\libacml_dll");
    }

    private static ACML instance;

    public static ACML jna() {
        if (instance == null){
            instance = new ACML();
        }

        return instance;
    }

    public native void sgesvd(char jobu, char jobvt, int m, int n, float[] a, int lda, float[] s, float[] u,
                              int ldu, float[] vt, int ldvt, int[] info);

    public native void sgesdd(char jobz, int m, int n, float[] a, int lda, float[] s, float[] u, int ldu, float[] vt,
                              int ldvt, int[] info);

    public native void sgemm(char transa, char transb, int m, int n, int k, float alpha, float[] a, int lda,
                             float[] b, int ldb, float beta, float[] c, int ldc);
}
