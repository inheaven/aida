package ru.inhell.aida.matrix;

import org.ujmp.core.doublematrix.DoubleMatrix2D;
import org.ujmp.core.doublematrix.factory.AbstractDoubleMatrix2DFactory;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.11.10 17:49
 */
public class AcmlMatrixFactory extends AbstractDoubleMatrix2DFactory {
    @Override
    public DoubleMatrix2D zeros(long rows, long columns) {
        return new AcmlMatrix(rows, columns);
    }
}
