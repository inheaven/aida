package ru.inhell.aida.entity;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 18.03.11 18:21
 */
public class AlphaOracle {
    private Long id;
    private VectorForecast vectorForecast;
    private float price;
    private Prediction prediction;
    private float stopPrice;
    private StopType stopType;
    private float stopFactor;
    private int stopCount;
    private float score;
    private PriceType priceType;
    private int maxStopCount;
    private Status status;

    public boolean isInMarket(){
        return Prediction.LONG.equals(prediction) || Prediction.SHORT.equals(prediction);
    }

    public void update(Prediction prediction, float price){
        switch (prediction){
            case STOP_BUY:
                stopCount++;
                score += this.price - price;
                this.price = 0;

                break;
            case STOP_SELL:
                stopCount++;
                score += price - this.price;
                this.price = 0;

                break;
            case LONG:
                if (this.price > 0) score += 2*(this.price - price);
                this.price = price;

                break;
            case SHORT:
                if (this.price > 0) score += 2*(price - this.price);
                this.price = price;

                break;
        }

        this.prediction = prediction;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public VectorForecast getVectorForecast() {
        return vectorForecast;
    }

    public void setVectorForecast(VectorForecast vectorForecast) {
        this.vectorForecast = vectorForecast;
    }

    public float getPrice() {
        return price;
    }

    public void setPrice(float price) {
        this.price = price;
    }

    public Prediction getPrediction() {
        return prediction;
    }

    public void setPrediction(Prediction prediction) {
        this.prediction = prediction;
    }

    public float getStopPrice() {
        return stopPrice;
    }

    public void setStopPrice(float stopPrice) {
        this.stopPrice = stopPrice;
    }

    public StopType getStopType() {
        return stopType;
    }

    public void setStopType(StopType stopType) {
        this.stopType = stopType;
    }

    public float getStopFactor() {
        return stopFactor;
    }

    public void setStopFactor(float stopFactor) {
        this.stopFactor = stopFactor;
    }

    public int getStopCount() {
        return stopCount;
    }

    public void setStopCount(int stopCount) {
        this.stopCount = stopCount;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public void setPriceType(PriceType priceType) {
        this.priceType = priceType;
    }

    public int getMaxStopCount() {
        return maxStopCount;
    }

    public void setMaxStopCount(int maxStopCount) {
        this.maxStopCount = maxStopCount;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
