package ru.inheaven.aida.backtest.netlib;

import com.github.fommil.netlib.BLAS;
import org.testng.annotations.Test;

/**
 * @author inheaven on 15.06.2016.
 */
public class NetLibTest {
    private final BLAS blas = BLAS.getInstance();

    @Test
    public void offsets() {
        double[] matrix = new double[]{
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1,
                1, 1, 1, 1, 1
        };
        blas.dscal(5, 2.0, matrix, 2, 5);
        double[] expected = new double[]{
                1, 1, 2, 1, 1,
                1, 1, 2, 1, 1,
                1, 1, 2, 1, 1,
                1, 1, 2, 1, 1,
                1, 1, 2, 1, 1
        };
    }

    @Test
    public void ddot() {
        double[] dx = {1.1, 2.2, 3.3, 4.4};
        double[] dy = {1.1, 2.2, 3.3, 4.4};
        int n = dx.length;

        double answer = blas.ddot(n, dx, 1, dy, 1);
        assert Math.abs(answer - 36.3) < 0.00001d;
    }
}
