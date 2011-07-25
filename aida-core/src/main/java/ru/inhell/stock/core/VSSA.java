package ru.inhell.stock.core;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.enums.ValueType;

import java.util.Arrays;

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
    private Matrix R, Rt;

    private BasicSSA basicSSA;

    public VSSA(int rangeLength, int windowLength, int eigenfunctionsCount, int predictionPointCount) {
        this.rangeLength = rangeLength;
        this.windowLength = windowLength;
        this.eigenfunctionsCount = eigenfunctionsCount;
        this.predictionPointCount = predictionPointCount;

        this.basicSSA = new BasicSSA(rangeLength, windowLength, eigenfunctionsCount);

        Z = MatrixFactory.dense(ValueType.DOUBLE, windowLength, rangeLength + predictionPointCount);
        R = MatrixFactory.dense(ValueType.DOUBLE, windowLength-1, 1);
    }

    public double[] execute(double[] timeSeries) {
        BasicSSA.Result ssa = basicSSA.execute(timeSeries);

        Matrix VD = ssa.getU().select(Ret.NEW, "0-" + (windowLength-2) + ";0-" + (predictionPointCount-1));

        Matrix pi = ssa.getU().select(Ret.NEW, windowLength-1 + ";0-" + (predictionPointCount-1));

        double v2 = 0;
        for (int i = 0; i < predictionPointCount; ++i){
            v2 += Math.pow(pi.getAsDouble(0,i), 2);
        }

        assert v2 < 1; //проверка вертикальности

        R.fill(Ret.ORIG, 0);

        for (int i = 0; i < predictionPointCount ; ++i){
            R.plus(Ret.ORIG, false, VD.selectColumns(Ret.LINK, i).mtimes(Ret.NEW, false, pi.getAsDouble(0,i)));
        }
        R.mtimes(Ret.ORIG, false, 1/(1-v2));

        Rt = R.transpose(Ret.LINK); //транспонированная R

        Matrix Pr = VD.mtimes(Ret.NEW, false, VD.transpose(Ret.LINK))
                .plus(Ret.ORIG, false, R.mtimes(Ret.NEW, false, R.transpose(Ret.LINK)).mtimes(Ret.ORIG, false, 1 - v2));

//        Matrix Pr = matrixUtil.multiply((DefaultDenseDoubleMatrix2D)VD, (DefaultDenseDoubleMatrix2D)VD.transpose(Ret.NEW))
//                .plus(Ret.ORIG, false, matrixUtil.multiply((DefaultDenseDoubleMatrix2D)R, (DefaultDenseDoubleMatrix2D)R.transpose(Ret.NEW))
//                        .mtimes(Ret.ORIG, false, 1 - v2));


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
        Matrix Yd = Y.select(Ret.LINK, "1-" + (Y.getSize(0)-1) + ";*");

        Matrix m1 = Pr.mtimes(Ret.NEW, false, Yd);
        Matrix m2 = Rt.mtimes(Ret.NEW, false, Yd);

        return m1.append(0, m2);
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
}
