package ru.inhell.aida.netlib;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 22.11.10 17:12
 */
public interface LAPACK {
    /**
     * <pre>
     *  DGESVD computes the singular value decomposition (SVD) of a real
     *  M-by-N matrix A, optionally computing the left and/or right singular
     *  vectors. The SVD is written
     *
     *       A = U * SIGMA * transpose(V)
     *
     *  where SIGMA is an M-by-N matrix which is zero except for its
     *  min(m,n) diagonal elements, U is an M-by-M orthogonal matrix, and
     *  V is an N-by-N orthogonal matrix.  The diagonal elements of SIGMA
     *  are the singular values of A; they are real and non-negative, and
     *  are returned in descending order.  The first min(m,n) columns of
     *  U and V are the left and right singular vectors of A.
     *
     *  Note that the routine returns V**T, not V.
     *
     *  Arguments
     *  =========
     *
     *  JOBU    (input) CHARACTER*1
     *          Specifies options for computing all or part of the matrix U:
     *          = 'A':  all M columns of U are returned in array U:
     *          = 'S':  the first min(m,n) columns of U (the left singular
     *                  vectors) are returned in the array U;
     *          = 'O':  the first min(m,n) columns of U (the left singular
     *                  vectors) are overwritten on the array A;
     *          = 'N':  no columns of U (no left singular vectors) are
     *                  computed.
     *
     *  JOBVT   (input) CHARACTER*1
     *          Specifies options for computing all or part of the matrix
     *          V**T:
     *          = 'A':  all N rows of V**T are returned in the array VT;
     *          = 'S':  the first min(m,n) rows of V**T (the right singular
     *                  vectors) are returned in the array VT;
     *          = 'O':  the first min(m,n) rows of V**T (the right singular
     *                  vectors) are overwritten on the array A;
     *          = 'N':  no rows of V**T (no right singular vectors) are
     *                  computed.
     *
     *          JOBVT and JOBU cannot both be 'O'.
     *
     *  M       (input) INTEGER
     *          The number of rows of the input matrix A.  M >= 0.
     *
     *  N       (input) INTEGER
     *          The number of columns of the input matrix A.  N >= 0.
     *
     *  A       (input/output) DOUBLE PRECISION array, dimension (LDA,N)
     *          On entry, the M-by-N matrix A.
     *          On exit,
     *          if JOBU = 'O',  A is overwritten with the first min(m,n)
     *                          columns of U (the left singular vectors,
     *                          stored columnwise);
     *          if JOBVT = 'O', A is overwritten with the first min(m,n)
     *                          rows of V**T (the right singular vectors,
     *                          stored rowwise);
     *          if JOBU .ne. 'O' and JOBVT .ne. 'O', the contents of A
     *                          are destroyed.
     *
     *  LDA     (input) INTEGER
     *          The leading dimension of the array A.  LDA >= max(1,M).
     *
     *  S       (output) DOUBLE PRECISION array, dimension (min(M,N))
     *          The singular values of A, sorted so that S(i) >= S(i+1).
     *
     *  U       (output) DOUBLE PRECISION array, dimension (LDU,UCOL)
     *          (LDU,M) if JOBU = 'A' or (LDU,min(M,N)) if JOBU = 'S'.
     *          If JOBU = 'A', U contains the M-by-M orthogonal matrix U;
     *          if JOBU = 'S', U contains the first min(m,n) columns of U
     *          (the left singular vectors, stored columnwise);
     *          if JOBU = 'N' or 'O', U is not referenced.
     *
     *  LDU     (input) INTEGER
     *          The leading dimension of the array U.  LDU >= 1; if
     *          JOBU = 'S' or 'A', LDU >= M.
     *
     *  VT      (output) DOUBLE PRECISION array, dimension (LDVT,N)
     *          If JOBVT = 'A', VT contains the N-by-N orthogonal matrix
     *          V**T;
     *          if JOBVT = 'S', VT contains the first min(m,n) rows of
     *          V**T (the right singular vectors, stored rowwise);
     *          if JOBVT = 'N' or 'O', VT is not referenced.
     *
     *  LDVT    (input) INTEGER
     *          The leading dimension of the array VT.  LDVT >= 1; if
     *          JOBVT = 'A', LDVT >= N; if JOBVT = 'S', LDVT >= min(M,N).
     *
     *  WORK    (workspace/output) DOUBLE PRECISION array, dimension (MAX(1,LWORK))
     *          On exit, if INFO = 0, WORK(1) returns the optimal LWORK;
     *          if INFO > 0, WORK(2:MIN(M,N)) contains the unconverged
     *          superdiagonal elements of an upper bidiagonal matrix B
     *          whose diagonal is in S (not necessarily sorted). B
     *          satisfies A = U * B * VT, so it has the same singular values
     *          as A, and singular vectors related by U and VT.
     *
     *  LWORK   (input) INTEGER
     *          The dimension of the array WORK.
     *          LWORK >= MAX(1,3*MIN(M,N)+MAX(M,N),5*MIN(M,N)).
     *          For good performance, LWORK should generally be larger.
     *
     *          If LWORK = -1, then a workspace query is assumed; the routine
     *          only calculates the optimal size of the WORK array, returns
     *          this value as the first entry of the WORK array, and no error
     *          message related to LWORK is issued by XERBLA.
     *
     *  INFO    (output) INTEGER
     *          = 0:  successful exit.
     *          < 0:  if INFO = -i, the i-th argument had an illegal value.
     *          > 0:  if DBDSQR did not converge, INFO specifies how many
     *                superdiagonals of an intermediate bidiagonal form B
     *                did not converge to zero. See the description of WORK
     *                above for details.
     * </pre>
     * @param jobu
     * @param jobvt
     * @param m
     * @param n
     * @param a
     * @param lda
     * @param s
     * @param u
     * @param ldu
     * @param vt
     * @param ldvt
     * @param info
     */
    public void dgesvd(String jobu, String jobvt, int m, int n, double[] a, int lda, double[] s, double[] u,
                       int ldu, double[] vt, int ldvt,  int[] info);

    /**
     * <pre>
     *  SGESVD computes the singular value decomposition (SVD) of a real
     *  M-by-N matrix A, optionally computing the left and/or right singular
     *  vectors. The SVD is written
     *
     *       A = U * SIGMA * transpose(V)
     *
     *  where SIGMA is an M-by-N matrix which is zero except for its
     *  min(m,n) diagonal elements, U is an M-by-M orthogonal matrix, and
     *  V is an N-by-N orthogonal matrix.  The diagonal elements of SIGMA
     *  are the singular values of A; they are real and non-negative, and
     *  are returned in descending order.  The first min(m,n) columns of
     *  U and V are the left and right singular vectors of A.
     *
     *  Note that the routine returns V**T, not V.
     *
     *  Arguments
     *  =========
     *
     *  JOBU    (input) CHARACTER*1
     *          Specifies options for computing all or part of the matrix U:
     *          = 'A':  all M columns of U are returned in array U:
     *          = 'S':  the first min(m,n) columns of U (the left singular
     *                  vectors) are returned in the array U;
     *          = 'O':  the first min(m,n) columns of U (the left singular
     *                  vectors) are overwritten on the array A;
     *          = 'N':  no columns of U (no left singular vectors) are
     *                  computed.
     *
     *  JOBVT   (input) CHARACTER*1
     *          Specifies options for computing all or part of the matrix
     *          V**T:
     *          = 'A':  all N rows of V**T are returned in the array VT;
     *          = 'S':  the first min(m,n) rows of V**T (the right singular
     *                  vectors) are returned in the array VT;
     *          = 'O':  the first min(m,n) rows of V**T (the right singular
     *                  vectors) are overwritten on the array A;
     *          = 'N':  no rows of V**T (no right singular vectors) are
     *                  computed.
     *
     *          JOBVT and JOBU cannot both be 'O'.
     *
     *  M       (input) INTEGER
     *          The number of rows of the input matrix A.  M >= 0.
     *
     *  N       (input) INTEGER
     *          The number of columns of the input matrix A.  N >= 0.
     *
     *  A       (input/output) REAL array, dimension (LDA,N)
     *          On entry, the M-by-N matrix A.
     *          On exit,
     *          if JOBU = 'O',  A is overwritten with the first min(m,n)
     *                          columns of U (the left singular vectors,
     *                          stored columnwise);
     *          if JOBVT = 'O', A is overwritten with the first min(m,n)
     *                          rows of V**T (the right singular vectors,
     *                          stored rowwise);
     *          if JOBU .ne. 'O' and JOBVT .ne. 'O', the contents of A
     *                          are destroyed.
     *
     *  LDA     (input) INTEGER
     *          The leading dimension of the array A.  LDA >= max(1,M).
     *
     *  S       (output) REAL array, dimension (min(M,N))
     *          The singular values of A, sorted so that S(i) >= S(i+1).
     *
     *  U       (output) REAL array, dimension (LDU,UCOL)
     *          (LDU,M) if JOBU = 'A' or (LDU,min(M,N)) if JOBU = 'S'.
     *          If JOBU = 'A', U contains the M-by-M orthogonal matrix U;
     *          if JOBU = 'S', U contains the first min(m,n) columns of U
     *          (the left singular vectors, stored columnwise);
     *          if JOBU = 'N' or 'O', U is not referenced.
     *
     *  LDU     (input) INTEGER
     *          The leading dimension of the array U.  LDU >= 1; if
     *          JOBU = 'S' or 'A', LDU >= M.
     *
     *  VT      (output) REAL array, dimension (LDVT,N)
     *          If JOBVT = 'A', VT contains the N-by-N orthogonal matrix
     *          V**T;
     *          if JOBVT = 'S', VT contains the first min(m,n) rows of
     *          V**T (the right singular vectors, stored rowwise);
     *          if JOBVT = 'N' or 'O', VT is not referenced.
     *
     *  LDVT    (input) INTEGER
     *          The leading dimension of the array VT.  LDVT >= 1; if
     *          JOBVT = 'A', LDVT >= N; if JOBVT = 'S', LDVT >= min(M,N).
     *
     *  WORK    (workspace/output) REAL array, dimension (MAX(1,LWORK))
     *          On exit, if INFO = 0, WORK(1) returns the optimal LWORK;
     *          if INFO > 0, WORK(2:MIN(M,N)) contains the unconverged
     *          superdiagonal elements of an upper bidiagonal matrix B
     *          whose diagonal is in S (not necessarily sorted). B
     *          satisfies A = U * B * VT, so it has the same singular values
     *          as A, and singular vectors related by U and VT.
     *
     *  LWORK   (input) INTEGER
     *          The dimension of the array WORK.
     *          LWORK >= MAX(1,3*MIN(M,N)+MAX(M,N),5*MIN(M,N)).
     *          For good performance, LWORK should generally be larger.
     *
     *          If LWORK = -1, then a workspace query is assumed; the routine
     *          only calculates the optimal size of the WORK array, returns
     *          this value as the first entry of the WORK array, and no error
     *          message related to LWORK is issued by XERBLA.
     *
     *  INFO    (output) INTEGER
     *          = 0:  successful exit.
     *          < 0:  if INFO = -i, the i-th argument had an illegal value.
     *          > 0:  if SBDSQR did not converge, INFO specifies how many
     *                superdiagonals of an intermediate bidiagonal form B
     *                did not converge to zero. See the description of WORK
     *                above for details.
     *</pre>
     * @param jobu
     * @param jobvt
     * @param m
     * @param n
     * @param a
     * @param lda
     * @param s
     * @param u
     * @param ldu
     * @param vt
     * @param ldvt
     * @param info
     */
    public void sgesvd(String jobu, String jobvt, int m, int n, float[] a, int lda, float[] s, float[] u,
                       int ldu, float[] vt, int ldvt, int[] info);

    /**
     * <pre>
     *  SGESDD computes the singular value decomposition (SVD) of a real
     *  M-by-N matrix A, optionally computing the left and right singular
     *  vectors.  If singular vectors are desired, it uses a
     *  divide-and-conquer algorithm.
     *
     *  The SVD is written
     *
     *       A = U * SIGMA * transpose(V)
     *
     *  where SIGMA is an M-by-N matrix which is zero except for its
     *  min(m,n) diagonal elements, U is an M-by-M orthogonal matrix, and
     *  V is an N-by-N orthogonal matrix.  The diagonal elements of SIGMA
     *  are the singular values of A; they are real and non-negative, and
     *  are returned in descending order.  The first min(m,n) columns of
     *  U and V are the left and right singular vectors of A.
     *
     *  Note that the routine returns VT = V**T, not V.
     *
     *  The divide and conquer algorithm makes very mild assumptions about
     *  floating point arithmetic. It will work on machines with a guard
     *  digit in add/subtract, or on those binary machines without guard
     *  digits which subtract like the Cray X-MP, Cray Y-MP, Cray C-90, or
     *  Cray-2. It could conceivably fail on hexadecimal or decimal machines
     *  without guard digits, but we know of none.
     *
     *  Arguments
     *  =========
     *
     *  JOBZ    (input) CHARACTER*1
     *          Specifies options for computing all or part of the matrix U:
     *          = 'A':  all M columns of U and all N rows of V**T are
     *                  returned in the arrays U and VT;
     *          = 'S':  the first min(M,N) columns of U and the first
     *                  min(M,N) rows of V**T are returned in the arrays U
     *                  and VT;
     *          = 'O':  If M >= N, the first N columns of U are overwritten
     *                  on the array A and all rows of V**T are returned in
     *                  the array VT;
     *                  otherwise, all columns of U are returned in the
     *                  array U and the first M rows of V**T are overwritten
     *                  in the array A;
     *          = 'N':  no columns of U or rows of V**T are computed.
     *
     *  M       (input) INTEGER
     *          The number of rows of the input matrix A.  M >= 0.
     *
     *  N       (input) INTEGER
     *          The number of columns of the input matrix A.  N >= 0.
     *
     *  A       (input/output) REAL array, dimension (LDA,N)
     *          On entry, the M-by-N matrix A.
     *          On exit,
     *          if JOBZ = 'O',  A is overwritten with the first N columns
     *                          of U (the left singular vectors, stored
     *                          columnwise) if M >= N;
     *                          A is overwritten with the first M rows
     *                          of V**T (the right singular vectors, stored
     *                          rowwise) otherwise.
     *          if JOBZ .ne. 'O', the contents of A are destroyed.
     *
     *  LDA     (input) INTEGER
     *          The leading dimension of the array A.  LDA >= max(1,M).
     *
     *  S       (output) REAL array, dimension (min(M,N))
     *          The singular values of A, sorted so that S(i) >= S(i+1).
     *
     *  U       (output) REAL array, dimension (LDU,UCOL)
     *          UCOL = M if JOBZ = 'A' or JOBZ = 'O' and M < N;
     *          UCOL = min(M,N) if JOBZ = 'S'.
     *          If JOBZ = 'A' or JOBZ = 'O' and M < N, U contains the M-by-M
     *          orthogonal matrix U;
     *          if JOBZ = 'S', U contains the first min(M,N) columns of U
     *          (the left singular vectors, stored columnwise);
     *          if JOBZ = 'O' and M >= N, or JOBZ = 'N', U is not referenced.
     *
     *  LDU     (input) INTEGER
     *          The leading dimension of the array U.  LDU >= 1; if
     *          JOBZ = 'S' or 'A' or JOBZ = 'O' and M < N, LDU >= M.
     *
     *  VT      (output) REAL array, dimension (LDVT,N)
     *          If JOBZ = 'A' or JOBZ = 'O' and M >= N, VT contains the
     *          N-by-N orthogonal matrix V**T;
     *          if JOBZ = 'S', VT contains the first min(M,N) rows of
     *          V**T (the right singular vectors, stored rowwise);
     *          if JOBZ = 'O' and M < N, or JOBZ = 'N', VT is not referenced.
     *
     *  LDVT    (input) INTEGER
     *          The leading dimension of the array VT.  LDVT >= 1; if
     *          JOBZ = 'A' or JOBZ = 'O' and M >= N, LDVT >= N;
     *          if JOBZ = 'S', LDVT >= min(M,N).
     *
     *  WORK    (workspace/output) REAL array, dimension (MAX(1,LWORK))
     *          On exit, if INFO = 0, WORK(1) returns the optimal LWORK;
     *
     *  LWORK   (input) INTEGER
     *          The dimension of the array WORK. LWORK >= 1.
     *          If JOBZ = 'N',
     *            LWORK >= 3*min(M,N) + max(max(M,N),6*min(M,N)).
     *          If JOBZ = 'O',
     *            LWORK >= 3*min(M,N) +
     *                     max(max(M,N),5*min(M,N)*min(M,N)+4*min(M,N)).
     *          If JOBZ = 'S' or 'A'
     *            LWORK >= 3*min(M,N) +
     *                     max(max(M,N),4*min(M,N)*min(M,N)+4*min(M,N)).
     *          For good performance, LWORK should generally be larger.
     *          If LWORK = -1 but other input arguments are legal, WORK(1)
     *          returns the optimal LWORK.
     *
     *  IWORK   (workspace) INTEGER array, dimension (8*min(M,N))
     *
     *  INFO    (output) INTEGER
     *          = 0:  successful exit.
     *          < 0:  if INFO = -i, the i-th argument had an illegal value.
     *          > 0:  SBDSDC did not converge, updating process failed.
     * </pre>
     */
    public void sgesdd(String jobz, int m, int n, float[] a, int lda, float[] s, float[] u, int ldu, float[] vt, int ldvt, int[] info);

}
