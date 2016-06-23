package ru.inhell.aida.ssa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.stock.core.VSSA;

import java.security.SecureRandom;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * @author inheaven on 20.06.2016.
 */
public class VSSABoost {
    private Logger log = LoggerFactory.getLogger(VSSABoost.class);

    private static final int MAX_VSSA_ITERATION = 10000;

    private Queue<VSSA> queue = new ConcurrentLinkedQueue<>();

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

        Queue<VSSA> localQueue = new ConcurrentLinkedQueue<>();
        localQueue.addAll(queue);

        Queue<VSSA> fitQueue = new ConcurrentLinkedQueue<>();

        for (int v = 0; v < MAX_VSSA_ITERATION; v++){
            VSSA vssa;
            VSSA local = localQueue.poll();

            if (local == null) {
                int L = random.nextInt(N/2 - M) + M;
                int P = random.nextInt(L/4 - 1) + 1;

                vssa = new VSSA(N, L, P, M);
            }else{
                vssa = new VSSA(local.getRangeLength(), local.getWindowLength(), local.getEigenfunctionsCount(), local.getPredictionPointCount());
            }

            int error = 0;

            for (int t = 0; t < trainCount; ++t){
                int start = random.nextInt(series.length - N - M);

                double[] train = new double[N];

                System.arraycopy(series, start, train, 0, train.length);

                double[] forecasts = vssa.execute(train);

                double forecast = getTarget(forecasts, N, M) - forecasts[N - 1];
                double test = getTarget(series, start + N, M) - series[start + N - 1];

                if (Double.isNaN(forecast) || (int)Math.signum(test) != (int)Math.signum(forecast)){
                    error++;
                }
            }

            double trainError = (double) error/trainCount;
            vssa.setError(trainError);

            if (trainError <= threshold){
                fitQueue.add(vssa);

                VSSA vq = queue.stream().filter(q -> q.getName().equals(vssa.getName())).findAny().orElse(null);

                if (vq != null){
                    vssa.setIndex(vq.getIndex() + 1);
                }

                log.info(fitQueue.size() + " " + trainError + " " + vssa.getName() + " " + vssa.getIndex());
            }else{
                vssa.setIndex(vssa.getIndex()/2);

                if (vssa.getIndex() > 0){
                    fitQueue.add(vssa);

                    log.info(fitQueue.size() + " " + trainError + " " + vssa.getName() + " " + vssa.getIndex());
                }else{
                    vssa.clear();
                }
            }

            if (fitQueue.size() >= vssaCount){
                break;
            }
        }

        queue.forEach(VSSA::clear);
        queue.clear();

        queue.addAll(fitQueue);
    }

    public double execute(double[] series){
        int forecast = 0;

        double[] seriesF = new double[N];

        int start = series.length - N;

        System.arraycopy(series, start, seriesF, 0, N);

        for (VSSA vssa : queue){
            double[] forecasts = vssa.execute(seriesF);

            forecast += Math.signum(getTarget(forecasts, N, M) - forecasts[N - 1]);
        }

        return forecast;
    }

    public double getTarget2(double[] series, int start, int size){
        double sum = 0;

        for (int i = start; i < start + size; ++i){
            sum += series[i];
        }

        return sum/size;
    }

    public double getTarget(double[] series, int start, int size){
        double max = series[start];
        double min = series[start];

        for (int i = start; i < start + size; ++i){
            double value = series[i];

            if (value > max){
                max = value;
            }

            if (value < min){
                min = value;
            }
        }

        return (max + min)/2;
    }
}
