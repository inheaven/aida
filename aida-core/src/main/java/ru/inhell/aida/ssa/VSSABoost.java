package ru.inhell.aida.ssa;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author inheaven on 20.06.2016.
 */
public class VSSABoost {
    private static final int MAX_VSSA_ITERATION = 10000;

    private List<VectorForecastSSA> list = new ArrayList<>();

    private double threshold;
    private int vssaCount;
    private int trainCount;
    private int N;
    private int M;

    public VSSABoost(double threshold, int vssaCount, int trainCount, int n, int m) {
        this.threshold = threshold;
        this.vssaCount = vssaCount;
        this.trainCount = trainCount;
        N = n;
        M = m;
    }

    public void fit(double[] series){
        Random random = new SecureRandom();

        for (int v = 0; v < MAX_VSSA_ITERATION; v++){
            int L = random.nextInt(N/2 - 4) + 4;
            int P = random.nextInt(L/2 - 1) + 1;

            VectorForecastSSA vssa = new VectorForecastSSA(N, L, P, M);

            int error = 0;

            for (int t = 0; t < trainCount; ++t){
                int start = random.nextInt(series.length - N - M);

                float[] train = new float[N];

                for (int i = 0; i < train.length; ++i){
                    train[i] = (float) series[start + i];
                }

                float[] forecasts = vssa.execute(train);

                double forecast = forecasts[N + M - 1] - forecasts[N - 1];

                if (Double.isNaN(forecast) || (series[start + N + M - 1] - series[start + N - 1])*forecast < 0){
                    error++;
                }
            }

            double trainError = (double) error/trainCount;

            if (trainError < threshold){
                vssa.setError(trainError);
                list.add(vssa);
            }

            if (list.size() > vssaCount){
                break;
            }
        }
    }

    public double execute(double[] series){
        int up = 0;

        float[] seriesF = new float[series.length];

        for (int i = 0; i < series.length; ++i){
            seriesF[i] = (float) series[i];
        }

        for (VectorForecastSSA vssa : list){
            float[] forecasts = vssa.execute(seriesF);

            double forecast = forecasts[N + M - 1] - forecasts[N - 1];

            if (forecast > 0){
                up++;
            }
        }

        return (double)up/list.size();
    }

    public List<VectorForecastSSA> getList() {
        return list;
    }
}
