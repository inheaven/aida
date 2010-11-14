package ru.inhell.stock.core;

import com.nativelibs4java.opencl.CLBuildException;
import com.nativelibs4java.opencl.CLDoubleBuffer;
import com.nativelibs4java.opencl.CLEvent;
import com.nativelibs4java.opencl.CLMem;
import org.ujmp.core.Matrix;
import org.ujmp.core.doublematrix.impl.DefaultDenseDoubleMatrix2D;
import ru.inhell.stock.cuda.LinearAlgebraUtils;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 25.06.2010 22:23:02
 */
public class MatrixUtil {
    private LinearAlgebraUtils linearAlgebraUtils;

    public MatrixUtil() {
        try {
            linearAlgebraUtils = new LinearAlgebraUtils();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (CLBuildException e) {
            e.printStackTrace();
        }
    }

    public Matrix multiply(DefaultDenseDoubleMatrix2D a, DefaultDenseDoubleMatrix2D b){
        DefaultDenseDoubleMatrix2D c = new DefaultDenseDoubleMatrix2D(a.getSize(0), b.getSize(1));

        DoubleBuffer valueA = DoubleBuffer.wrap(a.getColumnMajorDoubleArray1D());
        DoubleBuffer valueB = DoubleBuffer.wrap(b.getColumnMajorDoubleArray1D());

        CLDoubleBuffer bufA = linearAlgebraUtils.getContext().createDoubleBuffer(CLMem.Usage.Input, valueA, true);
        CLDoubleBuffer bufB = linearAlgebraUtils.getContext().createDoubleBuffer(CLMem.Usage.Input, valueB, true);
        CLDoubleBuffer bufC = linearAlgebraUtils.getContext().createDoubleBuffer(CLMem.Usage.Output, a.getSize(0)*b.getSize(1));

        try {
            CLEvent clEvent = linearAlgebraUtils.multiply(bufA, a.getSize(0), a.getSize(1), bufB, b.getSize(0), b.getSize(1), bufC);

            while(clEvent.getCommandExecutionStatus() != CLEvent.CommandExecutionStatus.Complete){
                //wait
            }

            DoubleBuffer db = bufC.read(linearAlgebraUtils.getQueue());

            double[] bb = new double[(int) (a.getSize(0)*b.getSize(1))];
            db.get(bb);

            System.out.println("bb: " + Arrays.toString(bb));
            



            c.showGUI();

           return c;

        } catch (CLBuildException e) {
            e.printStackTrace();
        }

        return c;
    }
}
