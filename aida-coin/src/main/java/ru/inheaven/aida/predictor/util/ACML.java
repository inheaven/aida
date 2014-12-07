package ru.inheaven.aida.predictor.util;

import com.sun.jna.Native;

/**
 * @author Anatoly Ivanov
 *         Date: 02.11.2014 5:29
 */
public class ACML {
    private static ThreadLocal<ACML> instance = new ThreadLocal<>();

    static {
        Native.register("c:\\dll\\libacml_dll");
    }

    public static ACML jna(){
        if (instance.get() == null){
            instance.set(new ACML());
        }

        return instance.get();
    }

    public native void sgesvd(char jobu, char jobvt, int m, int n, float[] a, int lda, float[] s, float[] u,
                              int ldu, float[] vt, int ldvt, int[] info);
    
    public native void sgesdd(char jobz, int m, int n, float[] a, int lda, float[] s, float[] u, int ldu, float[] vt,
                              int ldvt, int[] info);

    public native void sgemm(char transa, char transb, int m, int n, int k, float alpha, float[] a, int lda,
                             float[] b, int ldb, float beta, float[] c, int ldc);

    public native void dgesvd(char jobu, char jobvt, int m, int n, double[] a, int lda, double[] s, double[] u,
                              int ldu, double[] vt, int ldvt, int[] info);
    
    public native void dgesdd(char jobz, int m, int n, double[] a, int lda, double[] s, double[] u, int ldu, double[] vt,
                              int ldvt, int[] info);

    public native void dgemm(char transa, char transb, int m, int n, int k, double alpha, double[] a, int lda,
                             double[] b, int ldb, double beta, double[] c, int ldc);
}
