package ru.inhell.stock.core;

import com.nativelibs4java.opencl.*;

import java.nio.DoubleBuffer;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 26.06.2010 14:02:03
 */
public class VectorAdd {
    public static void main(String[] args) {
        try {
            DoubleBuffer a = DoubleBuffer.wrap(new double[] {  1,  2,  3,  4 });
            DoubleBuffer b = DoubleBuffer.wrap(new double[] { 10, 20, 30, 40 });

            DoubleBuffer sum = add(a, b);
            for (int i = 0, n = sum.capacity(); i < n; i++)
                System.out.println(sum.get(i));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static DoubleBuffer add(DoubleBuffer a, DoubleBuffer b) throws CLBuildException {
        int n = a.capacity();

        CLContext context = JavaCL.createBestContext();
        CLQueue queue = context.createDefaultQueue();

        String source =
                "#pragma OPENCL EXTENSION cl_khr_fp64: enable\n" +
                        "__kernel void addFloats(__global const double* a, __global const double* b, __global double* output)     " +
                        "{                                                                                                     " +
                        "   int i = get_global_id(0);                                                                          " +
                        "   output[i] = a[i] + b[i];                                                                           " +
                        "}                                                                                                     ";

        CLKernel kernel = context.createProgram(source).createKernel("addFloats");
        CLDoubleBuffer aBuf = context.createDoubleBuffer(CLMem.Usage.Input, a, true);
        CLDoubleBuffer bBuf = context.createDoubleBuffer(CLMem.Usage.Input, b, true);
        CLDoubleBuffer outBuf = context.createDoubleBuffer(CLMem.Usage.Output, n);
        kernel.setArgs(aBuf, bBuf, outBuf);

        kernel.enqueueNDRange(queue, new int[]{n}, new int[] { 1 });
        queue.finish();

        return outBuf.read(queue);
    }
}
