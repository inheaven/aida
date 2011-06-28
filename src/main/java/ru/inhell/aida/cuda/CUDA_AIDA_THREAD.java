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

    private static ThreadLocal<CUDA_AIDA> instance = new ThreadLocal<>();

    public static CUDA_AIDA get(){
        CUDA_AIDA cuda = instance.get();

        if (cuda == null){
            cuda = (CUDA_AIDA) Native.loadLibrary("cuda_aida", CUDA_AIDA.class);

            log.info("CUDA_AIDA library loaded successfully.");

            instance.set(cuda);
        }

        return cuda;
    }
}
