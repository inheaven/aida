package ru.inheaven.aida.backtest;

import ru.inhell.aida.algo.arima.ArimaProcess;

/**
 * @author inheaven on 12.06.2016.
 */
public class LevelArimaBacktestParameters {
    private int p, d, q, arimaNext, arimaFilter;
    private double arimaCoef;
    private ArimaProcess arimaProcess;
    private int arimaError = 0;

    public LevelArimaBacktestParameters(int p, int d, int q, int arimaNext, int arimaFilter, double arimaCoef, ArimaProcess arimaProcess) {
        this.p = p;
        this.d = d;
        this.q = q;
        this.arimaNext = arimaNext;
        this.arimaFilter = arimaFilter;
        this.arimaCoef = arimaCoef;
        this.arimaProcess = arimaProcess;
    }

    public int getP() {
        return p;
    }

    public void setP(int p) {
        this.p = p;
    }

    public int getD() {
        return d;
    }

    public void setD(int d) {
        this.d = d;
    }

    public int getQ() {
        return q;
    }

    public void setQ(int q) {
        this.q = q;
    }

    public int getArimaNext() {
        return arimaNext;
    }

    public void setArimaNext(int arimaNext) {
        this.arimaNext = arimaNext;
    }

    public int getArimaFilter() {
        return arimaFilter;
    }

    public void setArimaFilter(int arimaFilter) {
        this.arimaFilter = arimaFilter;
    }

    public double getArimaCoef() {
        return arimaCoef;
    }

    public void setArimaCoef(double arimaCoef) {
        this.arimaCoef = arimaCoef;
    }

    public ArimaProcess getArimaProcess() {
        return arimaProcess;
    }

    public void setArimaProcess(ArimaProcess arimaProcess) {
        this.arimaProcess = arimaProcess;
    }

    public int getArimaError() {
        return arimaError;
    }

    public void setArimaError(int arimaError) {
        this.arimaError = arimaError;
    }

    @Override
    public String toString() {
        return  " " + arimaNext + " " +
                arimaFilter + " " +
                (arimaProcess != null) + " " +
                arimaError + " " +
                arimaCoef + " " +
                p + " " +
                d + " " +
                q + " ";
    }
}
