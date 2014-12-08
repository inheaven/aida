package ru.inheaven.aida.predictor.util;

import com.sun.jna.Native;

/**
 * inheaven on 09.12.2014 2:19.
 */
public class CLBLAS {
    static {
        Native.register("c:\\dll\\clBLAS");
    }

    public CLBLAS() {
        clblasSetup();
    }

    public native void clblasSetup();
}
