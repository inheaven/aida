package ru.inhell.aida.ssa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.acml.ACML;

import java.util.Arrays;
import java.util.Random;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 30.11.10 1:40
 */
public class BasicAnalysisSSA {
    private static final Logger log = LoggerFactory.getLogger(BasicAnalysisSSA.class);

    private final static boolean timing = false;

    private int count = 0;

    public static class Result{
        public float[] U;
        public float[] S;
        public float[] VT;
        public float[] X;
        public float[] XI;
        public float[] G;
    }

    private int N;
    private int L;
    private int P;
    private int[] PP;

    private int K;

    private Result r;

    float[] Ui;
    float[] Vi;
    float[] Xi;

    /**
     *
     * @param N - Длина временного ряда
     * @param L - Длина окна L < N
     * @param P - Количество главных компонент
     */
    public BasicAnalysisSSA(int N, int L, int P) {
        this(N, L, P, new int[0]);
    }

    public BasicAnalysisSSA(int N, int L, int P, int[] PP) {
        this.N = N;
        this.L = L;
        this.P = P;
        this.PP = PP;

        if (PP.length == 0){
            this.PP = new int[P];

            for (int i = 0; i < P; ++i){
                this.PP[i] = i;
            }
        }

        if (PP.length == 0){
            this.PP = new int[P];

            for (int i = 0; i < P; ++i){
                this.PP[i] = i;
            }
        }

        K = N - L + 1;

        r = new Result();

        r.X = new float[L * K];
        r.XI = new float[L * K];
        r.G = new float[N * this.PP.length];
        r.S = new float[Math.min(L, K)];
        r.U = new float[L * L];
        r.VT = new float[K * K];

        Ui = new float[L];
        Vi = new float[K];
        Xi = new float[L * K];
    }

    private final static Random RANDOM = new Random();

    public Result execute(float[] timeSeries, boolean diagonalAverage){
        long time = System.currentTimeMillis();

        for (int j = 0; j < K; j++){
            System.arraycopy(timeSeries, j, r.X, j* L, L);
        }

        if (timing){
            System.out.println("BasicAnalysisSSA.1 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

        if (RANDOM.nextBoolean()){
            ACML.jni().sgesvd("S", "S", L, K, r.X, L, r.S, r.U, L, r.VT, K, new int[1]);
        }else {
            ACML.jni().sgesdd("S", L, K, r.X, L, r.S, r.U, L, r.VT, K, new int[1]);
        }

        if (timing){
            System.out.println("BasicAnalysisSSA.2 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

        Arrays.fill(r.XI, 0);

        //Восстановление по первым P компонентам
        for (int i : PP){
            System.arraycopy(r.U, L *i, Ui, 0, L);
            for (int j = 0; j < K; ++j){
                Vi[j] = r.VT[i + j*K];
            }

            ACML.jni().sgemm("N", "T", L, K, 1, r.S[i], Ui, L, Vi, K, 0, Xi, L);

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

    private float getSum(float[] Y, int rows, int cols, int first, int last, int k){
        float sum = 0;

        for (int m = first; m <= last; ++m){
            sum += rows < cols ? Y[m - 1 + rows*(k - m + 1)] : Y[k - m + 1 + rows*(m - 1)];
        }

        return sum;
    }
}
