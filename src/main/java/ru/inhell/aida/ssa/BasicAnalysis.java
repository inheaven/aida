package ru.inhell.aida.ssa;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.enums.ValueType;
import org.ujmp.core.floatmatrix.factory.AbstractFloatMatrix2DFactory;
import org.ujmp.core.floatmatrix.factory.DefaultFloatMatrix2DFactory;
import org.ujmp.core.floatmatrix.factory.FloatMatrix2DFactory;
import org.ujmp.core.interfaces.HasColumnMajorDoubleArray1D;
import org.ujmp.core.interfaces.HasFloatArray;
import org.ujmp.core.matrix.factory.Matrix2DFactory;
import ru.inhell.aida.matrix.AcmlMatrixFactory;

import static org.ujmp.core.calculation.Calculation.Ret.LINK;
import static org.ujmp.core.calculation.Calculation.Ret.NEW;
import static org.ujmp.core.calculation.Calculation.Ret.ORIG;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 30.11.10 1:40
 */
public class BasicAnalysis {
    public static enum MATRIX_FACTORY{DEFAULT, ACML}

     public static class Result{
        public Matrix U;
        public Matrix S;
        public Matrix V;
        public Matrix X;
        public Matrix XI;
        public Matrix G;
     }

    private int rangeLength;
    private int windowLength;
    private int eigenfunctionsCount;
    private int N;

    private Result result;

    private FloatMatrix2DFactory factory;

    private BasicAnalysis(int rangeLength, int windowLength, int eigenfunctionsCount, MATRIX_FACTORY matrixFactory) {
        this.rangeLength = rangeLength;
        this.windowLength = windowLength;
        this.eigenfunctionsCount = eigenfunctionsCount;

        this.N = rangeLength - windowLength + 1;

        switch (matrixFactory){
            case DEFAULT: factory =  new DefaultFloatMatrix2DFactory(); break;
            case ACML: factory =  new AcmlMatrixFactory(); break;
        }

        result = new Result();

        result.X = newMatrix(windowLength, N);
        result.XI = newMatrix(windowLength, N);
        result.G = newMatrix(rangeLength, eigenfunctionsCount);
    }

    private Matrix newMatrix(int rows, int cols){
        return factory.zeros(rows, cols);
    }

    public Result execute(float[] timeSeries){
        float[] values = ((HasFloatArray)result.X).getFloatArray();

        for (int j = 0; j < N; j++){
            System.arraycopy(timeSeries, j, values, j*windowLength, windowLength);
        }

        Matrix[] svd = result.X.svd();

        result.U = svd[0];
        result.S = svd[1];
        result.V = svd[2];

        float[] valuesXI = ((HasFloatArray)result.XI).getFloatArray();
        for (int i = 0; i < valuesXI.length; ++i){
            valuesXI[i] = 0;
        }

        for (int i = 0 ; i < eigenfunctionsCount ; ++i){
            Matrix Xi = result.U.selectColumns(LINK, i).mtimes(result.V.selectColumns(LINK, i).transpose(LINK));
//            Matrix Xi = newMatrix(windowLength, N); todo




            float[] valuesXi = ((HasFloatArray)Xi).getFloatArray();
            float si = result.S.getAsFloat(i, i);
            for (int j=0; j < valuesXi.length; ++j ){
                valuesXi[j] = valuesXi[j] * si;
            }

            result.XI.plus(ORIG, false, Xi);

            int L1 = Math.min(windowLength, N);
            int K1 = Math.max(windowLength, N);

            for (int k = 0; k < L1 - 1; ++k){
                result.G.setAsFloat(getFloatSum(Xi, 1, k+1, k) / (k + 1), k, i);
            }

            for (int k = L1 - 1; k < K1; ++k){
                result.G.setAsFloat(getFloatSum(Xi, 1, L1, k) / L1, k, i);
            }

            for (int k = K1; k < rangeLength; ++k){
                result.G.setAsFloat(getFloatSum(Xi, k - K1 + 2, rangeLength - K1 + 1, k) / (rangeLength - k), k, i);
            }
        }

        return result;
    }

    private float getFloatSum(Matrix Y, int first, int last, int k){
        float sum = 0;

        for (int m = first; m <= last; ++m){
            sum += Y.getSize(0) < Y.getSize(1) ? Y.getAsFloat(m - 1, k - m + 1) : Y.getAsFloat(k - m + 1, m - 1);
        }

        return sum;
    }
}
