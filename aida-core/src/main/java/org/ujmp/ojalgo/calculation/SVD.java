package org.ujmp.ojalgo.calculation;

import org.ojalgo.matrix.decomposition.SingularValue;
import org.ujmp.core.Matrix;
import org.ujmp.ojalgo.OjalgoDenseDoubleMatrix2D;

/**
 * @author inheaven on 15.06.2016.
 */
public class SVD implements org.ujmp.core.doublematrix.calculation.general.decomposition.SVD<Matrix> {

    public static SVD INSTANCE = new SVD();

    public Matrix[] calc(Matrix source) {
        final SingularValue<Double> svd = SingularValue.makePrimitive();
        if (source instanceof OjalgoDenseDoubleMatrix2D) {
            svd.compute(((OjalgoDenseDoubleMatrix2D) source).getWrappedObject());
        } else {
            svd.compute(new OjalgoDenseDoubleMatrix2D(source).getWrappedObject());
        }
        final Matrix u = new OjalgoDenseDoubleMatrix2D(svd.getQ1());
        final Matrix s = new OjalgoDenseDoubleMatrix2D(svd.getD());
        final Matrix v = new OjalgoDenseDoubleMatrix2D(svd.getQ2());
        return new Matrix[] { u, s, v };
    }
}