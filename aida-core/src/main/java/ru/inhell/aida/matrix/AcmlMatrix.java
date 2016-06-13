package ru.inhell.aida.matrix;

import org.ujmp.core.Matrix;
import org.ujmp.core.floatmatrix.impl.DefaultDenseFloatMatrix2D;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 23.11.10 13:36
 */
public class AcmlMatrix extends DefaultDenseFloatMatrix2D {

//    public AcmlMatrix(float[] v, int rows, int cols) {
//        super(v, rows, cols);
//    }

    public AcmlMatrix(int rows, int cols) {
        super(rows, cols);
    }

    @Override
    public Matrix[] svd() {
        //AMD ACML
//        if (ACML.jni().isLoaded()) {
//            int m = (int) getRowCount();
//            int n = (int) getColumnCount();
//
//            float[] x = getFloatArray();
//
//            float[] s = new float[Math.min(m, n)];
//            float[] u = new float[m*m];
//            float[] v = new float[n*n];
//
//            int[] info = new int[1];
//
////            ACML.jni().sgesvd("A", "A", m, n, x, m, s, u, m, v, n, info);
//            ACML.jni().sgesdd("A", m, n, x, m, s, u, m, v, n, info);
//
//            AcmlMatrix S = new AcmlMatrix(m >= n ? n : Math.min(m, n), n);
//
//            for (int i = Math.min(m, n); --i >= 0;){
//                S.setAsFloat(s[i], i, i);
//            }
//
//            return new Matrix[]{new AcmlMatrix(u, m, m), S, new AcmlMatrix(v, n, n)};
//        }

        //UJMP 3rd party
        return super.svd();
    }

    @Override
    public Matrix mtimes(Matrix m2) {
        //AMD ACML
//        if (ACML.jni().isLoaded() && m2 instanceof HasFloatArray){
//            final float alpha = 1;
//            final float beta = 1;
//            final int acols = (int) getColumnCount();
//            final int arows = (int) getRowCount();
//            final int bcols = (int) m2.getColumnCount();
//            final int brows = (int) m2.getRowCount();
//            final float[] avalues = getFloatArray();
//            final float[] bvalues = ((HasFloatArray) m2).getFloatArray();
//            final float[] cvalues = new float[arows * bcols];
//
//            ACML.jni().sgemm("N", "N", arows, bcols, acols, alpha, avalues, arows, bvalues, brows, beta, cvalues, arows);
//
//            return new AcmlMatrix(cvalues, arows, bcols);
//        }

        //UJMP 3rd party
        return super.mtimes(m2);
    }
}
