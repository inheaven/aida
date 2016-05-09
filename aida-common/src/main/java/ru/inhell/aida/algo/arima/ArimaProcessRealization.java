package ru.inhell.aida.algo.arima;

public interface ArimaProcessRealization extends ArimaProcess {
    double next();

    double[] next(int size);
}
