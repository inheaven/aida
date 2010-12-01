package ru.inhell.aida.ssa;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.enums.ValueType;
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
    private final int Lp;
    private final int M;

    private final float[] Z;
    private final float[] R;

    private final float[] VD;
    private final float[] pi;

    private final float[] VDxVDt;
    private final float[] RxRt;
    private final float[] Pr;

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
        Lp = L - 1;

        basicAnalysis = new BasicAnalysis(N, L, P);

        Z = new float[L * (N + M)];
        R = new float[Lp];

        VD = new float[Lp*M];
        pi = new float[M];

        VDxVDt = new float[Lp * Lp];
        RxRt = new float[Lp * Lp];
        Pr = new float[Lp * Lp];
    }

    public double[] execute(float[] timeSeries) {
        BasicAnalysis.Result ssa = basicAnalysis.execute(timeSeries);

        float v2 = 0;

        for (int i = 0; i < M; ++i){
            System.arraycopy(ssa.U, i*L, VD, i*Lp, Lp);
            pi[i] = ssa.U[i*L + L];
            v2 += Math.pow(pi[i], 2);
        }

        //проверка вертикальности
        assert v2 < 1;

        Arrays.fill(R, 0);

        for (int i = 0; i < M; ++i){ //todo check
            for (int j=0; j < Lp; ++j){
                R[j] += VD[j + i*Lp] * pi[i];
            }
        }

        for (int j=0; j < Lp; ++j){
            R[j] /= (1-v2);
        }

        ACML.jni().sgemm("N", "T", Lp, Lp, Lp, 1, VD, Lp, VD, Lp, 0, VDxVDt, Lp);
        ACML.jni().sgemm("N", "T", Lp, Lp, 1, 1 - v2, R, Lp, R, Lp, 0, RxRt, Lp);

        for (int i = 0; i < Lp; ++i){
            Pr[i] = VDxVDt[i] + RxRt[i];
        }

        Arrays.fill(Z, 0);

        for (int i = 0; i < N - L + 1; ++i){
            System.arraycopy(ssa.XI, i * (N - L + 1), Z, i * (N - L + 1), L);
        }

        for (int i = N - L + 1; i < N + M; ++i){
            Matrix Zi = Pv(Pr, Rt, Z.selectColumns(Ret.LINK, i-1));

            for (int j = 0; j < L; ++j){
                Z.setAsDouble(Zi.getAsDouble(j,0), j, i);
            }
        }

//        return getDiagonalAveraging(Z);
        return null;
    }

    private Matrix Pv(Matrix Pr, Matrix Rt, Matrix Y){
        Matrix Yd = Y.select(Ret.LINK, "1-" + (Y.getSize(0)-1) + ";*");

        Matrix m1 = Pr.mtimes(Ret.NEW, false, Yd);
        Matrix m2 = Rt.mtimes(Ret.NEW, false, Yd);

        return m1.append(0, m2);
    }

    public void diagonalAveraging(float[] Y, int rows, int cols, float[] g){
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
