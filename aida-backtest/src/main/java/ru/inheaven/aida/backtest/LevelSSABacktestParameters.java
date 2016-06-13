package ru.inheaven.aida.backtest;

/**
 * @author inheaven on 12.06.2016.
 */
public class LevelSSABacktestParameters {
    private int rangeLength;
    private int windowLength;
    private int eigenfunctionsCount;
    private int predictionPointCount;

    public LevelSSABacktestParameters(int rangeLength, int windowLength, int eigenfunctionsCount, int predictionPointCount) {
        this.rangeLength = rangeLength;
        this.windowLength = windowLength;
        this.eigenfunctionsCount = eigenfunctionsCount;
        this.predictionPointCount = predictionPointCount;
    }

    public int getRangeLength() {
        return rangeLength;
    }

    public void setRangeLength(int rangeLength) {
        this.rangeLength = rangeLength;
    }

    public int getWindowLength() {
        return windowLength;
    }

    public void setWindowLength(int windowLength) {
        this.windowLength = windowLength;
    }

    public int getEigenfunctionsCount() {
        return eigenfunctionsCount;
    }

    public void setEigenfunctionsCount(int eigenfunctionsCount) {
        this.eigenfunctionsCount = eigenfunctionsCount;
    }

    public int getPredictionPointCount() {
        return predictionPointCount;
    }

    public void setPredictionPointCount(int predictionPointCount) {
        this.predictionPointCount = predictionPointCount;
    }

    @Override
    public String toString() {
        return " " + rangeLength +
                " " + windowLength +
                " " + eigenfunctionsCount +
                " " + predictionPointCount;
    }
}
