package ru.inhell.aida.ssa;

import org.ujmp.core.Matrix;
import org.ujmp.core.interfaces.HasFloatArray;
import ru.inhell.aida.acml.ACML;

import static org.ujmp.core.calculation.Calculation.Ret.LINK;
import static org.ujmp.core.calculation.Calculation.Ret.ORIG;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 30.11.10 1:40
 */
public class BasicAnalysis {
    public static class Result{
        public float[] U;
        public float[] S;
        public float[] V;
        public float[] X;
        public float[] XI;
        public float[] G;
    }

    private int N;
    private int L;
    private int P;

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
    public BasicAnalysis(int N, int L, int P) {
        this.N = N;
        this.L = L;
        this.P = P;

        K = N - L + 1;

        r = new Result();

        r.X = new float[L * K];
        r.XI = new float[L * K];
        r.G = new float[N * P];
        r.S = new float[Math.min(L, K)];
        r.U = new float[L * L];
        r.V = new float[K * K];

        Ui = new float[L];
        Vi = new float[K];
        Xi = new float[L * K];
    }

    public Result execute(float[] timeSeries){
        for (int j = 0; j < K; j++){
            System.arraycopy(timeSeries, j, r.X, j* L, L);
        }

        ACML.jni().sgesvd("A", "A", L, K, r.X, L, r.S, r.U, L, r.V, K, new int[1]);

        for (int i = 0; i < r.XI.length; ++i){
            r.XI[i] = 0;
        }

        //Восстановление по первым P компонентам
        for (int i = 0 ; i < P; ++i){
            System.arraycopy(r.U, L*i, Ui, 0, L);
            System.arraycopy(r.V, K*i, Vi, 0, K);

            ACML.jni().sgemm("N", "T", L, K, 1, r.S[i], Ui, L, Vi, K, 0, Xi, L);

            for (int j = 0; j < L * K; ++j){
                r.XI[j] += Xi[j];
            }

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

        return r;
    }

    private float getSum(float[] Y, int cols, int rows, int first, int last, int k){
        float sum = 0;


        for (int m = first; m <= last; ++m){
            sum += cols < rows ? Y[m - 1 + rows*(k - m + 1)] : Y[k - m + 1 + rows*(m - 1)];
        }

        return sum;
    }
}
