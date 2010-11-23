package ru.inhell.aida.matrix;

import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.impl.DefaultDenseDoubleMatrix2D;
import org.ujmp.core.exceptions.MatrixException;
import org.ujmp.core.interfaces.HasColumnMajorDoubleArray1D;
import ru.inhell.aida.acml.ACML;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 23.11.10 13:36
 */
public class AcmlMatrix extends DefaultDenseDoubleMatrix2D {

    public AcmlMatrix(double[] v, int rows, int cols) {
        super(v, rows, cols);
    }

    public AcmlMatrix(long rows, long cols) {
        super(rows, cols);
    }

    @Override
    public Matrix[] svd() throws MatrixException {
        //AMD ACML
        if (ACML.jni().isLoaded()) {
            int m = (int) getRowCount();
            int n = (int) getColumnCount();

            double[] x = new double[m*n];

            double[] s = new double[Math.min(m, n)];
            double[] u = new double[m*m];
            double[] v = new double[n*n];

            int[] info = new int[1];

            ACML.jni().dgesvd("A", "A", m, n, x, m, s, u, m, v, n, info);

            AcmlMatrix S = new AcmlMatrix(m >= n ? n : Math.min(m, n), n);

            for (int i = Math.min(m, n); --i >= 0;){
                S.setAsDouble(s[i], i, i);
            }

            return new Matrix[]{new AcmlMatrix(u, m, m), S, new AcmlMatrix(v, n, n)};
        }

        //UJMP 3rd party
        return super.svd();
    }

    @Override
    public Matrix mtimes(Matrix m2) {
        //AMD ACML
        if (ACML.jni().isLoaded() && m2 instanceof HasColumnMajorDoubleArray1D){
            final int alpha = 1;
            final int beta = 1;
            final int acols = (int) getColumnCount();
            final int arows = (int) getRowCount();
            final int bcols = (int) m2.getColumnCount();
            final int brows = (int) m2.getRowCount();
            final double[] avalues = getColumnMajorDoubleArray1D();
            final double[] bvalues = ((HasColumnMajorDoubleArray1D) m2).getColumnMajorDoubleArray1D();
            final double[] cvalues = new double[arows * bcols];

            ACML.jni().dgemm("N", "N", arows, bcols, acols, alpha, avalues, arows, bvalues, brows, beta, cvalues, arows);

            return new AcmlMatrix(cvalues, arows, bcols);
        }

        //UJMP 3rd party
        return super.mtimes(m2);
    }
}
