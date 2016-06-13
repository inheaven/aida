package ru.inhell.aida.test;

import ru.inhell.aida.acml.ACML_DLL;
import ru.inhell.aida.ssa.BasicAnalysisSSA;
import ru.inhell.aida.ssa.VectorForecastSSA;

import java.util.Arrays;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 10.07.11 23:00
 */
public class SyevDebug {
    public static void main(String ... args) {
        forecast();
    }

    private static void forecast(){
         float[] ts = {1,2,3,4,5};

        int n = 5;
        int l = 3;

        int m = 2;
        int p = 2;

        int[] pp = {0, 1};

        float[] f = new float[n+l+m-1];

        new VectorForecastSSA(n, l, p, pp, m, BasicAnalysisSSA.TYPE.SGESVD).execute(ts, f);
        print("SGESVD", f);

        new VectorForecastSSA(n, l, p, pp, m, BasicAnalysisSSA.TYPE.SGESDD).execute(ts, f);
        print("SGESDD", f);

        new VectorForecastSSA(n, l, p, pp, m, BasicAnalysisSSA.TYPE.SSYEV).execute(ts, f);
        print("SSYEV", f);

    }

    private static void simple(){
        //ACML.jni().sgesvd("S", "S", L, K, r.X, L, r.S, r.U, L, r.VT, K, new int[1]);
        int l = 3;
        int k = 3;

        float[] x = {1,2,3,2,3,4,3,4,5};

        float[] u = new float[l*l];
        float[] vt = new float[k*k];
        float[] s = new float[Math.min(l, k)];

        ACML_DLL.sgesvd('A', 'A', l, k, x, l, s, u, l, vt, k, new int[1]);

        print("x", x);
        print("s", s);
        print("u", u);
        print("vt", vt);

        println();

        ACML_DLL.ssyev('V', 'U', l, x, l, s, new int[1]);
        print("x", x);
        print("s", s);
    }

    public static void print(String prefix, float[] a){
        System.out.println(prefix + ":" + Arrays.toString(a));
    }

    public static void println(){
        System.out.println();
    }
}
