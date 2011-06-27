package ru.inhell.aida.cuda;

import com.sun.jna.Library;
import com.sun.jna.Native;

import java.util.HashMap;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 27.06.11 22:23
 */
public interface CUDA_AIDA extends Library{
    CUDA_AIDA INSTANCE = (CUDA_AIDA) Native.loadLibrary("cuda_aida", CUDA_AIDA.class);

    void vssa(int n, int l, int p, int[] pp, int m, float[] timeseries, float[] forecast, int count);
}
