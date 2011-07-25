package ru.inhell.aida.netlib;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 23.11.10 17:28
 */
public interface BLAS {
    /**
     * <pre>
     * DGEMM  performs one of the matrix-matrix operations
     *
     *     C := alpha*op( A )*op( B ) + beta*C,
     *
     *  where  op( X ) is one of
     *
     *     op( X ) = X   or   op( X ) = X',
     *
     *  alpha and beta are scalars, and A, B and C are matrices, with op( A )
     *  an m by k matrix,  op( B )  a  k by n matrix and  C an m by n matrix.
     *
     *  Arguments
     *  ==========
     *
     *  TRANSA - CHARACTER*1.
     *           On entry, TRANSA specifies the form of op( A ) to be used in
     *           the matrix multiplication as follows:
     *
     *              TRANSA = 'N' or 'n',  op( A ) = A.
     *
     *              TRANSA = 'T' or 't',  op( A ) = A'.
     *
     *              TRANSA = 'C' or 'c',  op( A ) = A'.
     *
     *           Unchanged on exit.
     *
     *  TRANSB - CHARACTER*1.
     *           On entry, TRANSB specifies the form of op( B ) to be used in
     *           the matrix multiplication as follows:
     *
     *              TRANSB = 'N' or 'n',  op( B ) = B.
     *
     *              TRANSB = 'T' or 't',  op( B ) = B'.
     *
     *              TRANSB = 'C' or 'c',  op( B ) = B'.
     *
     *           Unchanged on exit.
     *
     *  M      - INTEGER.
     *           On entry,  M  specifies  the number  of rows  of the  matrix
     *           op( A )  and of the  matrix  C.  M  must  be at least  zero.
     *           Unchanged on exit.
     *
     *  N      - INTEGER.
     *           On entry,  N  specifies the number  of columns of the matrix
     *           op( B ) and the number of columns of the matrix C. N must be
     *           at least zero.
     *           Unchanged on exit.
     *
     *  K      - INTEGER.
     *           On entry,  K  specifies  the number of columns of the matrix
     *           op( A ) and the number of rows of the matrix op( B ). K must
     *           be at least  zero.
     *           Unchanged on exit.
     *
     *  ALPHA  - DOUBLE PRECISION.
     *           On entry, ALPHA specifies the scalar alpha.
     *           Unchanged on exit.
     *
     *  A      - DOUBLE PRECISION array of DIMENSION ( LDA, ka ), where ka is
     *           k  when  TRANSA = 'N' or 'n',  and is  m  otherwise.
     *           Before entry with  TRANSA = 'N' or 'n',  the leading  m by k
     *           part of the array  A  must contain the matrix  A,  otherwise
     *           the leading  k by m  part of the array  A  must contain  the
     *           matrix A.
     *           Unchanged on exit.
     *
     *  LDA    - INTEGER.
     *           On entry, LDA specifies the first dimension of A as declared
     *           in the calling (sub) program. When  TRANSA = 'N' or 'n' then
     *           LDA must be at least  max( 1, m ), otherwise  LDA must be at
     *           least  max( 1, k ).
     *           Unchanged on exit.
     *
     *  B      - DOUBLE PRECISION array of DIMENSION ( LDB, kb ), where kb is
     *           n  when  TRANSB = 'N' or 'n',  and is  k  otherwise.
     *           Before entry with  TRANSB = 'N' or 'n',  the leading  k by n
     *           part of the array  B  must contain the matrix  B,  otherwise
     *           the leading  n by k  part of the array  B  must contain  the
     *           matrix B.
     *           Unchanged on exit.
     *
     *  LDB    - INTEGER.
     *           On entry, LDB specifies the first dimension of B as declared
     *           in the calling (sub) program. When  TRANSB = 'N' or 'n' then
     *           LDB must be at least  max( 1, k ), otherwise  LDB must be at
     *           least  max( 1, n ).
     *           Unchanged on exit.
     *
     *  BETA   - DOUBLE PRECISION.
     *           On entry,  BETA  specifies the scalar  beta.  When  BETA  is
     *           supplied as zero then C need not be set on input.
     *           Unchanged on exit.
     *
     *  C      - DOUBLE PRECISION array of DIMENSION ( LDC, n ).
     *           Before entry, the leading  m by n  part of the array  C must
     *           contain the matrix  C,  except when  beta  is zero, in which
     *           case C need not be set on entry.
     *           On exit, the array  C  is overwritten by the  m by n  matrix
     *           ( alpha*op( A )*op( B ) + beta*C ).
     *
     *  LDC    - INTEGER.
     *           On entry, LDC specifies the first dimension of C as declared
     *           in  the  calling  (sub)  program.   LDC  must  be  at  least
     *           max( 1, m ).
     *           Unchanged on exit.
     * </pre>
     * @param transa
     * @param transb
     * @param m
     * @param n
     * @param k
     * @param alpha
     * @param a
     * @param lda
     * @param b
     * @param ldb
     * @param beta
     * @param c
     * @param ldc
     */
    public void dgemm(String transa, String transb, int m, int n, int k, double alpha, double[] a, int lda, double[] b,
                      int ldb, double beta, double[] c, int ldc);

    /**
     * <pre>
     *  SGEMM  performs one of the matrix-matrix operations
     *
     *     C := alpha*op( A )*op( B ) + beta*C,
     *
     *  where  op( X ) is one of
     *
     *     op( X ) = X   or   op( X ) = X',
     *
     *  alpha and beta are scalars, and A, B and C are matrices, with op( A )
     *  an m by k matrix,  op( B )  a  k by n matrix and  C an m by n matrix.
     *
     *  Arguments
     *  ==========
     *
     *  TRANSA - CHARACTER*1.
     *           On entry, TRANSA specifies the form of op( A ) to be used in
     *           the matrix multiplication as follows:
     *
     *              TRANSA = 'N' or 'n',  op( A ) = A.
     *
     *              TRANSA = 'T' or 't',  op( A ) = A'.
     *
     *              TRANSA = 'C' or 'c',  op( A ) = A'.
     *
     *           Unchanged on exit.
     *
     *  TRANSB - CHARACTER*1.
     *           On entry, TRANSB specifies the form of op( B ) to be used in
     *           the matrix multiplication as follows:
     *
     *              TRANSB = 'N' or 'n',  op( B ) = B.
     *
     *              TRANSB = 'T' or 't',  op( B ) = B'.
     *
     *              TRANSB = 'C' or 'c',  op( B ) = B'.
     *
     *           Unchanged on exit.
     *
     *  M      - INTEGER.
     *           On entry,  M  specifies  the number  of rows  of the  matrix
     *           op( A )  and of the  matrix  C.  M  must  be at least  zero.
     *           Unchanged on exit.
     *
     *  N      - INTEGER.
     *           On entry,  N  specifies the number  of columns of the matrix
     *           op( B ) and the number of columns of the matrix C. N must be
     *           at least zero.
     *           Unchanged on exit.
     *
     *  K      - INTEGER.
     *           On entry,  K  specifies  the number of columns of the matrix
     *           op( A ) and the number of rows of the matrix op( B ). K must
     *           be at least  zero.
     *           Unchanged on exit.
     *
     *  ALPHA  - REAL            .
     *           On entry, ALPHA specifies the scalar alpha.
     *           Unchanged on exit.
     *
     *  A      - REAL             array of DIMENSION ( LDA, ka ), where ka is
     *           k  when  TRANSA = 'N' or 'n',  and is  m  otherwise.
     *           Before entry with  TRANSA = 'N' or 'n',  the leading  m by k
     *           part of the array  A  must contain the matrix  A,  otherwise
     *           the leading  k by m  part of the array  A  must contain  the
     *           matrix A.
     *           Unchanged on exit.
     *
     *  LDA    - INTEGER.
     *           On entry, LDA specifies the first dimension of A as declared
     *           in the calling (sub) program. When  TRANSA = 'N' or 'n' then
     *           LDA must be at least  max( 1, m ), otherwise  LDA must be at
     *           least  max( 1, k ).
     *           Unchanged on exit.
     *
     *  B      - REAL             array of DIMENSION ( LDB, kb ), where kb is
     *           n  when  TRANSB = 'N' or 'n',  and is  k  otherwise.
     *           Before entry with  TRANSB = 'N' or 'n',  the leading  k by n
     *           part of the array  B  must contain the matrix  B,  otherwise
     *           the leading  n by k  part of the array  B  must contain  the
     *           matrix B.
     *           Unchanged on exit.
     *
     *  LDB    - INTEGER.
     *           On entry, LDB specifies the first dimension of B as declared
     *           in the calling (sub) program. When  TRANSB = 'N' or 'n' then
     *           LDB must be at least  max( 1, k ), otherwise  LDB must be at
     *           least  max( 1, n ).
     *           Unchanged on exit.
     *
     *  BETA   - REAL            .
     *           On entry,  BETA  specifies the scalar  beta.  When  BETA  is
     *           supplied as zero then C need not be set on input.
     *           Unchanged on exit.
     *
     *  C      - REAL             array of DIMENSION ( LDC, n ).
     *           Before entry, the leading  m by n  part of the array  C must
     *           contain the matrix  C,  except when  beta  is zero, in which
     *           case C need not be set on entry.
     *           On exit, the array  C  is overwritten by the  m by n  matrix
     *           ( alpha*op( A )*op( B ) + beta*C ).
     *
     *  LDC    - INTEGER.
     *           On entry, LDC specifies the first dimension of C as declared
     *           in  the  calling  (sub)  program.   LDC  must  be  at  least
     *           max( 1, m ).
     *           Unchanged on exit.
     * </pre>
     * @param transa
     * @param transb
     * @param m
     * @param n
     * @param k
     * @param alpha
     * @param a
     * @param lda
     * @param b
     * @param ldb
     * @param beta
     * @param c
     * @param ldc
     */
    public void sgemm(String transa, String transb, int m, int n, int k, float alpha, float[] a, int lda,
                      float[] b, int ldb, float beta, float[] c, int ldc);
}
