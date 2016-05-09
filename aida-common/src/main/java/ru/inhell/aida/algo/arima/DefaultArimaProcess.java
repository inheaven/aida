package ru.inhell.aida.algo.arima;

import java.util.Arrays;

import static ru.inhell.aida.algo.arima.DoubleUtils.EMPTY_DOUBLE_ARRAY;

public class DefaultArimaProcess implements ArimaProcess {
    private double[] arCoefficients = EMPTY_DOUBLE_ARRAY;
    private double[] maCoefficients = EMPTY_DOUBLE_ARRAY;
    private int integrationOrder;
    private double expectation;
    private double variation = 1;
    private double constant;

    @Override
    public int getArOrder() {
        return arCoefficients.length;
    }

    @Override
    public int getIntegrationOrder() {
        return integrationOrder;
    }

    @Override
    public int getMaOrder() {
        return maCoefficients.length;
    }

    public void setIntegrationOrder(int integrationOrder) {
        this.integrationOrder = integrationOrder;
    }

    @Override
    public double[] getArCoefficients() {
        return arCoefficients;
    }

    public void setArCoefficients(double... arCoefficients) {
        this.arCoefficients = arCoefficients;
    }

    @Override
    public double[] getMaCoefficients() {
        return maCoefficients;
    }

    public void setMaCoefficients(double... maCoefficients) {
        this.maCoefficients = maCoefficients;
    }

    @Override
    public double getExpectation() {
        return expectation;
    }

    public void setExpectation(double expectation) {
        this.expectation = expectation;
    }

    @Override
    public double getVariation() {
        return variation;
    }

    public void setVariation(double variation) {
        this.variation = variation;
    }

    @Override
    public double getConstant() {
        return constant;
    }

    public void setConstant(double constant) {
        this.constant = constant;
    }

    @Override
    public String toString() {
        return "ArimaProcess{" +
                "arCoefficients=" + Arrays.toString(arCoefficients) +
                ", maCoefficients=" + Arrays.toString(maCoefficients) +
                ", integrationOrder=" + integrationOrder +
                ", expectation=" + expectation +
                ", variation=" + variation +
                ", constant=" + constant +
                '}';
    }
}
