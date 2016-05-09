package ru.inhell.aida.algo.arima;

public interface ArimaProcess extends ArimaModel {
    double[] getArCoefficients();

    double[] getMaCoefficients();

    double getExpectation();

    double getVariation();

    double getConstant();
}
