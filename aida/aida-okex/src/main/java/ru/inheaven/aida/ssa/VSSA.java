package ru.inheaven.aida.ssa;

import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.DoubleMatrix;

import static org.ujmp.core.calculation.Calculation.Ret;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.06.2010 20:24:13
 */
public class VSSA {
    private int rangeLength;
    private int windowLength;
    private int eigenfunctionsCount;
    private int predictionPointCount;

    private Matrix Z;
    private Matrix R;

    private BasicSSA basicSSA;

    private double error = 0;
    private int index = 0;

    public VSSA(int rangeLength, int windowLength, int eigenfunctionsCount, int predictionPointCount) {
        this.rangeLength = rangeLength;
        this.windowLength = windowLength;
        this.eigenfunctionsCount = eigenfunctionsCount;
        this.predictionPointCount = predictionPointCount;

        this.basicSSA = new BasicSSA(rangeLength, windowLength, eigenfunctionsCount);

        Z = DoubleMatrix.Factory.zeros(windowLength, rangeLength + predictionPointCount);
        R = DoubleMatrix.Factory.zeros(windowLength-1, 1);
    }

    public double[] execute(double[] timeSeries) {
        BasicSSA.Result ssa = basicSSA.execute(timeSeries);

        Matrix VD = ssa.getU().select(Ret.NEW, "0-" + (windowLength-1) + ";0-" + predictionPointCount);

        Matrix pi = ssa.getU().select(Ret.NEW, windowLength-1 + ";0-" + predictionPointCount);

        double v2 = 0;
        for (int i = 0; i < predictionPointCount; ++i){
            v2 += Math.pow(pi.getAsDouble(0,i), 2);
        }

        assert v2 < 1; //проверка вертикальности

        R.fill(Ret.ORIG, 0);

        for (int i = 0; i < predictionPointCount ; ++i){
            R.plus(Ret.ORIG, false, VD.selectColumns(Ret.LINK, i).times(Ret.NEW, false, pi.getAsDouble(0,i)));
        }
        R.times(Ret.ORIG, false, 1/(1-v2));

        Matrix Rt = R.transpose(Ret.LINK); //транспонированная R

        Matrix Pr = VD.mtimes(Ret.NEW, false, VD.transpose(Ret.LINK))
                .plus(Ret.ORIG, false, R.mtimes(Ret.NEW, false, R.transpose(Ret.LINK)).times(Ret.ORIG, false, 1 - v2));

        Z.fill(Ret.ORIG, 0);

        for (int i = 0; i < rangeLength - windowLength + 1; ++i){
            for (int j = 0; j < windowLength; ++j){
                Z.setAsDouble(ssa.getXI().getAsDouble(j,i), j, i);
            }
        }

        for (int i = rangeLength - windowLength + 1; i < rangeLength + predictionPointCount; ++i){
            Matrix Zi = Pv(Pr, Rt, Z.selectColumns(Ret.LINK, i-1));

            for (int j = 0; j < windowLength; ++j){
                Z.setAsDouble(Zi.getAsDouble(j,0), j, i);
            }
        }

        return getDiagonalAveraging(Z);
    }

    private Matrix Pv(Matrix Pr, Matrix Rt, Matrix Y){
        Matrix Yd = Y.select(Ret.LINK, "1-" + Y.getSize(0) + ";*");

        return Pr.mtimes(Ret.NEW, false, Yd).append(Ret.NEW, 0, Rt.mtimes(Ret.NEW, false, Yd));
    }

    public double[] getDiagonalAveraging(Matrix Y){
        int L = (int) Y.getSize(0);
        int K = (int) Y.getSize(1);

        int L1 = Math.min(L, K);
        int K1 = Math.max(L, K);

        int N = L + K - 1;

        double[] g = new double[N];

        for (int k = 0; k < L1 - 1; ++k){
            g[k] = getSumY1(Y, 1, k+1, k) / (k + 1);
        }

        for (int k = L1 - 1; k < K1; ++k){
            g[k] = getSumY1(Y, 1, L1, k) / L1;
        }

        for (int k = K1; k < N; ++k){
            g[k] = getSumY1(Y, k - K1 + 2, N - K1 + 1, k) / (N - k);
        }

        return g;
    }

    private double getSumY1(Matrix Y, int first, int last, int k){
        double sum = 0;

        for (int m = first; m <= last; ++m){
            sum += Y.getSize(0) < Y.getSize(1) ? Y.getAsDouble(m - 1, k - m + 1) : Y.getAsDouble(k - m + 1, m - 1);
        }

        return sum;
    }

    public void clear(){
        basicSSA.clear();

        R.clear();
        Z.clear();
    }

    public int getRangeLength() {
        return rangeLength;
    }

    public int getWindowLength() {
        return windowLength;
    }

    public int getEigenfunctionsCount() {
        return eigenfunctionsCount;
    }

    public int getPredictionPointCount() {
        return predictionPointCount;
    }

    public String getName(){
        return rangeLength + "-" + windowLength + "-" + eigenfunctionsCount;
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
