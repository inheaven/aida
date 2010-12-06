package ru.inhell.aida.ssa;

import org.ujmp.core.Matrix;
import ru.inhell.aida.acml.ACML;

import java.util.Arrays;

import static org.ujmp.core.calculation.Calculation.*;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 30.11.10 16:57
 */
public class VectorForecast {
    private final int N;
    private final int L;
    private final int Ld;
    private final int M;
    private final int K;

    private final float[] Z;
    private final float[] R;

    private final float[] VD;
    private final float[] pi;

    private final float[] VDxVDt;
    private final float[] RxRt;
    private final float[] Pr;

    private final float[] Yd;
    private final float[] Zi;

    private BasicAnalysis basicAnalysis;

    /**
     *
     * @param N - длина временного интервала
     * @param L - длина окна
     * @param P - количество главных компонент
     * @param M - длина прогноза
     */
    public VectorForecast(int N, int L, int P, int M) {
        this.N = N;
        this.L = L;
        this.M = M;

        Ld = L - 1;
        K = N - L + 1;

        basicAnalysis = new BasicAnalysis(N, L, P);

        Z = new float[L * (N + M)];
        R = new float[Ld];

        VD = new float[Ld * M];
        pi = new float[M];

        VDxVDt = new float[Ld * Ld];
        RxRt = new float[Ld * Ld];
        Pr = new float[Ld * Ld];

        Yd = new float[L-1];
        Zi = new float[L];
    }

    /**
     *
     * @param timeSeries float[N]
     * @param forecast float[N + M + L - 1]
     */
    public void execute(float[] timeSeries, float forecast[]) {
        BasicAnalysis.Result ssa = basicAnalysis.execute(timeSeries);

        float v2 = 0;

        for (int i = 0; i < M; ++i){
            System.arraycopy(ssa.U, i*L, VD, i*Ld, Ld);
            pi[i] = ssa.U[L-1 + i*L];
            v2 += Math.pow(pi[i], 2);
        }

        //проверка вертикальности
        assert v2 < 1;

        Arrays.fill(R, 0);

        for (int i = 0; i < M; ++i){
            for (int j = 0; j < Ld; ++j){
                R[j] += VD[j + i*Ld] * pi[i];
            }
        }

        for (int j=0; j < Ld; ++j){
            R[j] /= (1-v2);
        }

        ACML.jni().sgemm("N", "T", Ld, Ld, M, 1, VD, Ld, VD, Ld, 0, VDxVDt, Ld);
        ACML.jni().sgemm("N", "T", Ld, Ld, 1, 1-v2, R, Ld, R, Ld, 0, RxRt, Ld);

        for (int i = 0; i < Ld * Ld; ++i){
            Pr[i] = VDxVDt[i] + RxRt[i];
        }

        System.arraycopy(ssa.XI, 0, Z, 0, L * K);

        for (int i = K; i < N + M; ++i){
            System.arraycopy(Z, 1 + (i-1)*L, Yd, 0, Ld);

            ACML.jni().sgemm("N", "N", Ld, 1, Ld, 1, Pr, Ld, Yd, Ld, 0, Zi, Ld);

            Zi[L-1] = 0;
            for (int j = 0; j < Ld; ++j){
                Zi[L-1] += R[j] * Yd[j];
            }

            System.arraycopy(Zi, 0, Z, i * L, L);
        }

        diagonalAveraging(Z, L, N + M, forecast);
    }

    private void diagonalAveraging(float[] Y, int rows, int cols, float[] g){
        int L1 = Math.min(rows, cols);
        int K1 = Math.max(rows, cols);

        int N = L1 + K1 - 1;

        for (int k = 0; k < L1 - 1; ++k){
            g[k] = getSum(Y, rows, cols, 1, k + 1, k) / (k + 1);
        }

        for (int k = L1 - 1; k < K1; ++k){
            g[k] = getSum(Y, rows, cols, 1, L1, k) / L1;
        }

        for (int k = K1; k < N; ++k){
            g[k] = getSum(Y, rows, cols, k - K1 + 2, N - K1 + 1, k) / (N - k);
        }
    }

    private float getSum(float Y[], int rows, int cols, int first, int last, int k){
        float sum = 0;

        for (int m = first; m <= last; ++m){
            sum += rows < cols ? Y[m - 1 + (k - m + 1)*rows] : Y[k - m + 1 + (m - 1)*rows];
        }

        return sum;
    }
}
