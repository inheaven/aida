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
            VSSA vssa = localQueue.poll();

            if (vssa == null) {
                int L = random.nextInt(N/2 - 8) + 8;
                int P = random.nextInt(L/4 - 1) + 1;

                vssa = new VSSA(N, L, P, M);
            }

            int error = 0;

            for (int t = 0; t < trainCount; ++t){
                int start = random.nextInt(series.length - N - M);

                double[] train = new double[N];

                System.arraycopy(series, start, train, 0, train.length);

                double[] forecasts = vssa.execute(train);

                double forecast = forecasts[N + M - 1] - forecasts[N - 1];
                double test = series[start + N + M - 1] - series[start + N - 1];

                if (Double.isNaN(forecast) || (int)Math.signum(test) != (int)Math.signum(forecast)){
                    error++;
                }
            }

            double trainError = (double) error/trainCount;
            vssa.setError(trainError);

            if (trainError <= threshold){
                fitQueue.add(vssa);

                if (queue.contains(vssa)){
                    vssa.setIndex(vssa.getIndex() + 1);
                }

                log.info(fitQueue.size() + " " + trainError + " " + vssa.getName() + " " + vssa.getIndex());
            }else{
                vssa.setIndex(vssa.getIndex()/2);

                if (vssa.getIndex() > 0){
                    fitQueue.add(vssa);

                    log.info(fitQueue.size() + " " + trainError + " " + vssa.getName() + " " + vssa.getIndex());
                }
            }

            if (fitQueue.size() >= vssaCount){
                break;
            }
        }

        queue.clear();
        queue.addAll(fitQueue);
    }

    public int execute(double[] series){
        int up = 0;
        int down = 0;

        double[] seriesF = new double[N];

        int start = series.length - N;

        System.arraycopy(series, start, seriesF, 0, series.length);

        for (VSSA vssa : queue){
            double[] forecasts = vssa.execute(seriesF);

            double forecast = forecasts[N + M - 1] - forecasts[N - 1];

            if (forecast > 0){
                up++;
            }

            if (forecast < 0){
                down++;
            }
        }

        return up > down ? 1 : up == down ? 0 : -1;
    }
}
