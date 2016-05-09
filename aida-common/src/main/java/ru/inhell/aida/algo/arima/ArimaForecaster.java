package ru.inhell.aida.algo.arima;

public interface ArimaForecaster {
    double next();

    double[] next(int size);
}
