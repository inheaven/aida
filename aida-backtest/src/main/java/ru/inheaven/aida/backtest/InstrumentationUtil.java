package ru.inheaven.aida.backtest;

import java.lang.instrument.Instrumentation;

/**
 * inheaven on 06.05.2016.
 */
public class InstrumentationUtil {
    private static Instrumentation instrumentation;

    public static void premain(String args, Instrumentation inst) {
        instrumentation = inst;
    }


}
