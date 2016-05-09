package ru.inhell.aida.algo.arima;

public interface ArimaModel {
    int getArOrder();

    int getIntegrationOrder();

    int getMaOrder();
}
