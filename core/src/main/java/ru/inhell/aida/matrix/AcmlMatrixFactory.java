package ru.inhell.aida.matrix;

import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.DoubleMatrix2D;
import org.ujmp.core.doublematrix.calculation.entrywise.creators.Eye;
import org.ujmp.core.doublematrix.factory.AbstractDoubleMatrix2DFactory;
import org.ujmp.core.exceptions.MatrixException;
import org.ujmp.core.floatmatrix.FloatMatrix2D;
import org.ujmp.core.floatmatrix.factory.AbstractFloatMatrix2DFactory;
import org.ujmp.core.matrix.Matrix2D;
import org.ujmp.core.matrix.factory.Matrix2DFactory;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.11.10 17:49
 */
public class AcmlMatrixFactory extends AbstractFloatMatrix2DFactory implements Matrix2DFactory{
    @Override
    public FloatMatrix2D dense(long rows, long columns) throws MatrixException {
        return new AcmlMatrix(rows, columns);
    }

    @Override
    public Matrix rand(final long... size) {
		final Matrix m = zeros(size);
		m.rand(Calculation.Ret.ORIG);
		return m;
	}

    @Override
	public Matrix randn(final long... size) {
		final Matrix m = zeros(size);
		m.randn(Calculation.Ret.ORIG);
		return m;
	}

    @Override
	public Matrix ones(final long... size) {
		final Matrix m = zeros(size);
		m.ones(Calculation.Ret.ORIG);
		return m;
	}

    @Override
	public Matrix eye(final long... size) {
		final Matrix m = zeros(size);
		Eye.calcOrig(m);
		return m;
	}

    @Override
    public Matrix2D zeros(long... size) {
        return dense(size[0], size[1]);
    }
}
