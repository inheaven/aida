package ru.inhell.aida.test;


import org.ujmp.core.Matrix;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.floatmatrix.factory.DefaultFloatMatrix2DFactory;
import org.ujmp.core.floatmatrix.factory.FloatMatrix2DFactory;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.11.10 16:10
 */
public class AcmlMatrixTest {
    //Matrix M*N
    private static final int M = 1000; //Row count
    private static final int N = 1000; //Column count

    private static final int DF = 1; //Double factor for random matrix

    private Matrix etalonSvd;
    private Matrix[] etalonSvdResult;

    private Matrix etalonMtimes;
    private Matrix etalonMtimes2;
    private Matrix etalonMtimesResult;

//    @BeforeSuite
    private void initUJMP(){
//        UJMPSettings.setUseMTJ(false);
//        UJMPSettings.setNumberOfThreads(3);
    }

  //  @BeforeSuite
    private void initSvd(){
        //SVD Etalon
//        etalonSvd = MatrixFactory.rand(ValueType.FLOAT, M, N);
//        etalonSvd.mtimes(Calculation.Ret.ORIG, false, DF);

        long time = System.currentTimeMillis();

        etalonSvdResult = etalonSvd.svd();

        System.out.println("Etalon svd calculated: " + (System.currentTimeMillis() - time) + "ms");

        //abs
        etalonSvdResult[0].abs(Calculation.Ret.ORIG);
        etalonSvdResult[1].abs(Calculation.Ret.ORIG);
        etalonSvdResult[2].abs(Calculation.Ret.ORIG);
    }

//    @BeforeSuite
    private void initMTimes(){
        //Mtimes Etalon
//        etalonMtimes = MatrixFactory.rand(ValueType.FLOAT, M, N);
//        etalonMtimes.mtimes(Calculation.Ret.ORIG, false, DF);
//
//        etalonMtimes2 = MatrixFactory.rand(ValueType.FLOAT, N, M);
//        etalonMtimes2.mtimes(Calculation.Ret.ORIG, false, DF);

        long time = System.currentTimeMillis();

        
        etalonMtimesResult = etalonMtimes2;
        //etalonMtimesResult = etalonMtimes.mtimes(etalonMtimes2);

        System.out.println("Etalon mtimes calculated: " + (System.currentTimeMillis() - time) + "ms");
    }

    private void svd(FloatMatrix2DFactory factory){
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

    private void mtimes(FloatMatrix2DFactory factory){
        //Matrix matrix = factory.zeros(M, N);
        //matrix.plus(Calculation.Ret.ORIG, false, etalonMtimes);
        Matrix matrix = factory.zeros(M,N);
//        matrix.plus(Calculation.Ret.ORIG, false, MatrixFactory.rand(ValueType.FLOAT, M, N));

        Matrix matrix2 = factory.zeros(N, M);
        matrix2.plus(Calculation.Ret.ORIG, false, etalonMtimes2);

        long time = System.currentTimeMillis();

        Matrix mtimes = matrix.mtimes(matrix2);

        System.out.println(factory.getClass().getSimpleName() + " mtimes(): " + (System.currentTimeMillis() - time) + "ms");

        //Comparison of results
        System.out.printf("distance: %2.10f", mtimes.euklideanDistanceTo(etalonMtimesResult, false)).println();

//        System.out.println(mtimes) ;
//        System.out.println(etalonMtimesResult);
    }

    //SVD Test

//    @Test(invocationCount = 3)
    public void svdMJTTest(){ //jrmc 20.77s, hotspot 9,84s
//        svd(new MTJDenseDoubleMatrix2DFactory());
    }

//    @Test(invocationCount = 3)
    public void svdDefaultTest(){ //jrmc 50.15s, hotspot 57,84s
//        svd(new DefaultDenseDoubleMatrix2DFactory());
    }

//    @Test(invocationCount = 3)
//    public void svdACMLTest(){ //jrmc 50.15s, hotspot 57,84s
//        assert ACML.jni().isLoaded();
//        svd(new AcmlMatrixFactory());
//    }

    //Mtimes Test

//    @Test(invocationCount = 3)
    public void mtimesMJTTest(){ //jrmc 4.05s, hotspot 2.09s
//        mtimes(new MTJDenseDoubleMatrix2DFactory());
    }

//    @Test(invocationCount = 1000)
    public void mtimesDefaultTest(){ //jrmc 1.41s, hotspot 1.63s
        mtimes(new DefaultFloatMatrix2DFactory());
    }

//    @Test(invocationCount = 1000)
//    public void mtimesACMLTest(){
//        assert ACML.jni().isLoaded();
//        mtimes(new AcmlMatrixFactory());
//    }
}
