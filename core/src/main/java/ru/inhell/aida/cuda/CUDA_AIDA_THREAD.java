package ru.inhell.aida.cuda;

import com.sun.jna.Native;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 28.06.11 2:01
 */
public class CUDA_AIDA_THREAD {
    private final static Logger log = LoggerFactory.getLogger(CUDA_AIDA_THREAD.class);

    private static ThreadLocal<CUDA_AIDA_THREAD> instance = new ThreadLocal<CUDA_AIDA_THREAD>();

    public CUDA_AIDA_THREAD() {
        Native.register("cuda_aida");
    }

    public static CUDA_AIDA_THREAD get(){
        CUDA_AIDA_THREAD cuda = instance.get();

        if (cuda == null){
            cuda = new CUDA_AIDA_THREAD();

            log.info("CUDA_AIDA library loaded successfully.");

            instance.set(cuda);
        }

        return cuda;
    }

    public native void vssa(int n, int l, int p, int[] pp, int m, float[] timeseries, float[] forecast, int count);
}
