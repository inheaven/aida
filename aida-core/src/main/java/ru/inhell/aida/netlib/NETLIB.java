package ru.inhell.aida.netlib;

import com.github.fommil.netlib.LAPACK;

/**
 * @author inheaven on 15.06.2016.
 */
public class NETLIB {
    static {
        System.setProperty("com.github.fommil.netlib.BLAS", "com.github.fommil.netlib.NativeSystemBLAS");
        System.setProperty("com.github.fommil.netlib.NativeSystemBLAS.natives", "clBLAS");

        System.setProperty("com.github.fommil.netlib.LAPACK", "com.github.fommil.netlib.NativeSystemLAPACK");
        System.setProperty("com.github.fommil.netlib.NativeSystemLAPACK.natives", "clBLAS");
    }

    private static com.github.fommil.netlib.BLAS blas = com.github.fommil.netlib.BLAS.getInstance();
    private static com.github.fommil.netlib.LAPACK lapack = LAPACK.getInstance();

    public void dgesvd(String jobu, String jobvt, int m, int n, double[] a, int lda, double[] s, double[] u,
                              int ldu, double[] vt, int ldvt, int[] info){
//        lapack.dgesvd(jobu, jobvt, m, n, a, lda, s, u, ldu, vt, ldvt, );

    }

    public void dgemm(String transa, String transb, int m, int n, int k, double alpha, double[] a, int lda,
                             double[] b, int ldb, double beta, double[] c, int ldc){
        blas.dgemm(transa, transb, m, n, k, alpha, a, lda, b, ldb, beta, c, ldc);
    }


    public void sgesvd(String jobu, String jobvt, int m, int n, float[] a, int lda, float[] s, float[] u,
                              int ldu, float[] vt, int ldvt, int[] info){

    }

    public void sgesdd(String jobz, int m, int n, float[] a, int lda, float[] s, float[] u, int ldu, float[] vt,
                              int ldvt, int[] info){

    }

    public void sgemm(String transa, String transb, int m, int n, int k, float alpha, float[] a, int lda,
                             float[] b, int ldb, float beta, float[] c, int ldc){
        blas.sgemm(transa, transb, m, n, k, alpha, a, lda, b, ldb, beta, c, ldc);

    }

    public void vssa(int n, int l, int p, int[] pp, int m, float[] timeseries, float[] forecat, int svd){

    }
}
