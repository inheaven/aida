package ru.inhell.aida.ssa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.aida.acml.ACML;

import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 30.11.10 16:57
 */
public class VectorForecastSSA {
    private static final Logger log = LoggerFactory.getLogger(VectorForecastSSA.class);

    private final static boolean timing = false;

    private final int N;
    private final int L;
    private final int P;
    private final int[] PP;
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

    private final float[] forecast;

    private BasicAnalysisSSA basicAnalysis;

    private BasicAnalysisSSA.TYPE type;

    private double error;

    private int index = 0;

    /**
     *
     * @param N - длина временного интервала
     * @param L - длина окна
     * @param P - количество главных компонент
     * @param M - длина прогноза
     */
    public VectorForecastSSA(int N, int L, int P, int M) {
        this(N, L, P, new int[0], M, BasicAnalysisSSA.TYPE.SGESVD);
    }

    public VectorForecastSSA(int N, int L, int P, int[] PP, int M){
        this(N, L, P, PP, M, BasicAnalysisSSA.TYPE.SGESVD);
    }

    public VectorForecastSSA(int N, int L, int P, int[] PP, int M, BasicAnalysisSSA.TYPE type) {
        this.N = N;
        this.L = L;
        this.M = M;
        this.PP = PP;
        this.P = P;

        this.type = type;

        Ld = L - 1;
        K = N - L + 1;

        if (PP.length > 0 && type.equals(BasicAnalysisSSA.TYPE.SSYEV)){
            for (int i = 0; i < P; ++i){
                PP[i] = L - PP[i] - 1;
            }
        }

        basicAnalysis = new BasicAnalysisSSA(N, L, P, PP, type);

        Z = new float[L * (N + M)];
        R = new float[Ld];

        VD = new float[Ld * M];
        pi = new float[M];

        VDxVDt = new float[Ld * Ld];
        RxRt = new float[Ld * Ld];
        Pr = new float[Ld * Ld];

        Yd = new float[L-1];
        Zi = new float[L];

        forecast = new float[forecastSize()];
    }

    public int getN() {
        return N;
    }

    public int getL() {
        return L;
    }

    public int getP() {
        return P;
    }

    public int getM() {
        return M;
    }

    public int forecastSize(){
        return N + M + L - 1;
    }

    public float[] execute(float[] timeSeries){
        execute(timeSeries, forecast);

        return forecast;
    }

    /**
     *
     * @param timeSeries float[N]
     * @param forecast float[N + M + L - 1]
     */
    public void execute(float[] timeSeries, float forecast[]) {
        long time = System.currentTimeMillis();

        if (timing){
            System.out.println("VectorForecastSSA.1 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

        BasicAnalysisSSA.Result ssa = basicAnalysis.execute(timeSeries, false);

        if (timing){
            System.out.println("VectorForecastSSA.2 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

        float v2 = 0;

        for (int i = 0; i < M; ++i){
            int index = i;

            if (type.equals(BasicAnalysisSSA.TYPE.SSYEV)){
                index = L - i - 1;
            }

            System.arraycopy(ssa.U, index*L, VD, i*Ld, Ld);
            pi[i] = ssa.U[L-1 + index*L];
            v2 += Math.pow(pi[i], 2);
        }

        Arrays.fill(R, 0);

        for (int i = 0; i < M; ++i){
            for (int j = 0; j < Ld; ++j){
                R[j] += VD[j + i*Ld] * pi[i];
            }
        }

        for (int j=0; j < Ld; ++j){
            R[j] /= (1-v2);
        }

        if (timing){
            System.out.println("VectorForecastSSA.3 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

        ACML.jni().sgemm("N", "T", Ld, Ld, M, 1, VD, Ld, VD, Ld, 0, VDxVDt, Ld);
        ACML.jni().sgemm("N", "T", Ld, Ld, 1, 1-v2, R, Ld, R, Ld, 0, RxRt, Ld);

        if (timing){
            System.out.println("VectorForecastSSA.4 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

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

        if (timing){
            System.out.println("VectorForecastSSA.5 " + (System.currentTimeMillis() - time));
            time = System.currentTimeMillis();
        }

        diagonalAveraging(Z, L, N + M, forecast);

        if (timing){
            System.out.println("VectorForecastSSA.6 " + (System.currentTimeMillis() - time));
        }
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

    public String getName(){
        return N + "-" + L + "-" + P;
    }

    public double getError() {
        return error;
    }

    public void setError(double error) {
        this.error = error;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
