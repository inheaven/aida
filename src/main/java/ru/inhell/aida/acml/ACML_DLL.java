package ru.inhell.aida.acml;

import com.sun.jna.Native;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 10.07.11 23:47
 */
public class ACML_DLL {
    private static ThreadLocal<ACML_DLL> instance = new ThreadLocal<ACML_DLL>();

    static {
        Native.register("libacml_dll");
    }

    public static ACML_DLL jna(){
        if (instance.get() == null){
            instance.set(new ACML_DLL());
        }

        return instance.get();
    }

    public native static void ssyev(char jobz, char uplo, int n, float[] a, int lda, float[] w, int[] info);
    public native static void sgesvd(char jobu, char jobvt, int m, int n, float[] a, int lda, float[] sing, float[] u, int ldu, float[] vt, int ldvt, int[] info);
}
