package ru.inhell.aida.cula;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * @author Anatoly A. Ivanov java@inhell.ru
 * Date: 18.06.11 15:29
 */
public class CULA {
    private final static Logger log = LoggerFactory.getLogger(CULA.class);

    private static ThreadLocal<CULA> instance = new ThreadLocal<CULA>();

    private CULA() {
        try {
            System.loadLibrary("cula_wrapper");

            log.info("CULA library loaded successfully.");
        } catch (Exception e) {

            log.error("CULA library not found", e);
        }
    }

    public static CULA jni(){
        CULA cula = instance.get();

        if (cula == null){
            cula = new CULA();

            instance.set(cula);
        }

        return cula;
    }

    public native void vssa(int n, int l, int p, int[] pp, int m, float[] timeseries, float[] forecat, int count);
}
