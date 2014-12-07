package ru.inheaven.aida.predictor.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 30.11.10 1:40
 */
public class BasicAnalysisSSA {
    private static final Logger log = LoggerFactory.getLogger(BasicAnalysisSSA.class);

    public static enum TYPE{SSYEV, SGESDD, SGESVD}

    private final static boolean timing = false;

    public static class Result{
        public double[] U;
        public double[] S;
        public double[] VT;
        public double[] X;
        public double[] XI;
        public double[] G;
    }

    private int N;
    private int L;
    private int P;
    private int[] PP;

    private int K;

    private Result r;

    double[] Ui;
    double[] Vi;
    double[] Xi;
    double[] XiXit;

    private TYPE type;

    /**
     *
     * @param N - Длина временного ряда
     * @param L - Длина окна L < N
     * @param P - Количество главных компонент
     */
    public BasicAnalysisSSA(int N, int L, int P) {
        this(N, L, P, new int[0], TYPE.SSYEV);
    }

    public BasicAnalysisSSA(int N, int L, int P, int[] PP, TYPE type) {
        this.N = N;
        this.L = L;
        this.P = P;
        this.PP = PP;
        this.type = type;

        if (PP.length == 0){
            this.PP = new int[P];

            for (int i = 0; i < P; ++i){
                this.PP[i] = i;
            }
        }

        K = N - L + 1;

        r = new Result();

        r.X = new double[L * K];
        r.XI = new double[L * K];
        r.G = new double[N * this.PP.length];
        r.S = new double[Math.min(L, K)];
        r.U = new double[L * L];
        r.VT = new double[K * K];

        Ui = new double[L];
        Vi = new double[K];
        Xi = new double[L * K];
        XiXit = new double[L*L];
    }
    public Result execute(double[] timeSeries, boolean diagonalAverage){
        long time = System.currentTimeMillis();

        for (int j = 0; j < K; j++){
            System.arraycopy(timeSeries, j, r.X, j* L, L);
        }

        if (timing){
            System.out.println("BasicAnalysisSSA.1 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

        switch (type){
            case SGESDD:
                ACML.jna().DGESDD('S', L, K, r.X, L, r.S, r.U, L, r.VT, K, new int[1]);
                break;
            case SGESVD:
                ACML.jna().DGESVD('S', 'S', L, K, r.X, L, r.S, r.U, L, r.VT, K, new int[1]);
                break;
        }

        if (timing){
            System.out.println("BasicAnalysisSSA.2 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

        Arrays.fill(r.XI, 0);

        //Восстановление по первым P компонентам
        for (int i : PP){
            System.arraycopy(r.U, L *i, Ui, 0, L);

            if (type.equals(TYPE.SSYEV)) {
                if (r.S[i] < 0){
                    continue;
                }

                ACML.jna().DGEMM('T', 'N', L, 1, L, 1/r.S[i], r.X, L, Ui, L, 0, Vi, L);
            } else {
                for (int j = 0; j < K; ++j){
                    Vi[j] = r.VT[i + j*K];
                }
            }

            ACML.jna().DGEMM('N', 'T', L, K, 1, r.S[i], Ui, L, Vi, K, 0, Xi, L);

            for (int j = 0; j < L * K; ++j){
                r.XI[j] += Xi[j];
            }

            if (diagonalAverage){
                int L1 = Math.min(L, K);
                int K1 = Math.max(L, K);

                for (int k = 0; k < L1 - 1; ++k){
                    r.G[k + i*N] = getSum(Xi, L, K, 1, k+1, k) / (k + 1);
                }

                for (int k = L1 - 1; k < K1; ++k){
                    r.G[k + i*N] = getSum(Xi, L, K, 1, L1, k) / L1;
                }

                for (int k = K1; k < N; ++k){
                    r.G[k + i*N] = getSum(Xi, L, K, k - K1 + 2, N - K1 + 1, k) / (N - k);
                }
            }
        }

        if (timing){
            System.out.println("BasicAnalysisSSA.3 " + (System.currentTimeMillis() - time));
        }

        return r;
    }

    private double getSum(double[] Y, int rows, int cols, int first, int last, int k){
        double sum = 0;

        for (int m = first; m <= last; ++m){
            sum += rows < cols ? Y[m - 1 + rows*(k - m + 1)] : Y[k - m + 1 + rows*(m - 1)];
        }

        return sum;
    }
}
