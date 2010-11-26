package ru.inhell.aida.test;

import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Test;
import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.doublematrix.factory.DefaultDenseDoubleMatrix2DFactory;
import org.ujmp.core.doublematrix.factory.DoubleMatrix2DFactory;
import org.ujmp.core.enums.ValueType;
import org.ujmp.core.util.UJMPSettings;
import org.ujmp.mtj.MTJDenseDoubleMatrix2DFactory;

import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.11.10 16:10
 */
public class AcmlMatrixTest {
    //Matrix M*N
    private static final int M = 1000; //Row count
    private static final int N = 1000; //Column count

    private static final int DF = 1500; //Double factor for random matrix

    private Matrix etalonSvd;
    private Matrix[] etalonSvdResult;

    private Matrix etalonMtimes;
    private Matrix etalonMtimes2;
    private Matrix etalonMtimesResult;

//    @BeforeSuite
    private void initSvd(){
        UJMPSettings.setUseMTJ(false);

        //SVD Etalon
        etalonSvd = MatrixFactory.rand(ValueType.DOUBLE, M, N);
        etalonSvd.mtimes(DF);

        long time = System.currentTimeMillis();

        etalonSvdResult = etalonSvd.svd();

        System.out.println("Etalon svd calculated: " + (System.currentTimeMillis() - time) + "ms");

        //abs
        etalonSvdResult[0].abs(Calculation.Ret.ORIG);
        etalonSvdResult[1].abs(Calculation.Ret.ORIG);
        etalonSvdResult[2].abs(Calculation.Ret.ORIG);
    }

    @BeforeSuite
    private void initMTimes(){
        UJMPSettings.setUseMTJ(false);

        //Mtimes Etalon
        etalonMtimes = MatrixFactory.rand(ValueType.DOUBLE, M, N);
        etalonMtimes.mtimes(DF);

        etalonMtimes2 = MatrixFactory.rand(ValueType.DOUBLE, N, M);
        etalonMtimes2.mtimes(DF);

        long time = System.currentTimeMillis();

        UJMPSettings.setUseMTJ(false);
        etalonMtimesResult = etalonMtimes.mtimes(etalonMtimes2);

        System.out.println("Etalon mtimes calculated: " + (System.currentTimeMillis() - time) + "ms");
    }

    private void svd(DoubleMatrix2DFactory factory){
        System.out.println("");

        Matrix matrix = factory.zeros(M, N);
        matrix.plus(Calculation.Ret.ORIG, false, etalonSvd);

        long time = System.currentTimeMillis();

        Matrix[] svd = matrix.svd();

        System.out.println(factory.getClass().getSimpleName() + " svd(): " + (System.currentTimeMillis() - time) + "ms");

        //Comparison of results
        System.out.printf("distance u, s, v: %2.10f, %2.10f, %2.10f %n",
                svd[0].abs(Calculation.Ret.ORIG).euklideanDistanceTo(etalonSvdResult[0], false),
                svd[1].abs(Calculation.Ret.ORIG).euklideanDistanceTo(etalonSvdResult[1], false),
                svd[2].abs(Calculation.Ret.ORIG).euklideanDistanceTo(etalonSvdResult[2], false));
    }

    private void mtimes(DoubleMatrix2DFactory factory){
        System.out.println("");

        Matrix matrix = factory.zeros(M, N);
        matrix.plus(Calculation.Ret.ORIG, false, etalonMtimes);

        Matrix matrix2 = factory.zeros(N, M);
        matrix2.plus(Calculation.Ret.ORIG, false, etalonMtimes2);

        long time = System.currentTimeMillis();

        Matrix mtimes = matrix.mtimes(matrix2);

        System.out.println(factory.getClass().getSimpleName() + " mtimes(): " + (System.currentTimeMillis() - time) + "ms");

        //Comparison of results
        System.out.printf("distance: %2.10f", mtimes.euklideanDistanceTo(etalonMtimesResult, false)).println();
    }

    @Test(invocationCount = 3)
    public void svdMJTTest(){ //jrmc 20.77s, hotspot 9,84s
        svd(new MTJDenseDoubleMatrix2DFactory());
    }

    @Test(invocationCount = 3)
    public void svdDefaultTest(){ //jrmc 50.15s, hotspot 57,84s
        svd(new DefaultDenseDoubleMatrix2DFactory());
    }

    @Test(invocationCount = 3)
    public void mtimesMJTTest(){ //jrmc 4.05s, hotspot 2.09s
        mtimes(new MTJDenseDoubleMatrix2DFactory());
    }

    @Test(invocationCount = 3)
    public void mtimesDefaultTest(){ //jrmc 1.41s, hotspot 1.63s
        mtimes(new DefaultDenseDoubleMatrix2DFactory());
    }
}
