package ru.inhell.aida.ssa;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.inhell.stock.core.VSSA;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author inheaven on 20.06.2016.
 */
public class VSSABoost {
    private Logger log = LoggerFactory.getLogger(VSSABoost.class);

    private static final int MAX_VSSA_ITERATION = 10000;

    private Queue<VSSA> queue = new ConcurrentLinkedQueue<>();

    private AtomicLong index = new AtomicLong(0);

    private double threshold;
    private int vssaCount;
    private int trainCount;
    private int N;
    private int M;


    private AtomicBoolean fitting = new AtomicBoolean(false);

    public VSSABoost(double threshold, int vssaCount, int trainCount, int n, int m) {
        this.threshold = threshold;
        this.vssaCount = vssaCount;
        this.trainCount = trainCount;
        N = n;
        M = m;
    }

    private ExecutorService executor = Executors.newWorkStealingPool();

    public void fit(List<Double> series){
        if (fitting.get()){
            return;
        }

        fitting.set(true);

        Random random = new SecureRandom("corets".getBytes());

        Queue<VSSA> localQueue = new ConcurrentLinkedQueue<>();
        localQueue.addAll(queue);

        Queue<VSSA> fitQueue = new ConcurrentLinkedQueue<>();

        List<Future> futures = new ArrayList<>();

        for (int p = 0; p < Runtime.getRuntime().availableProcessors()-1; ++p){
            futures.add(executor.submit(() -> {
                for (int v = 0; v < MAX_VSSA_ITERATION; v++){
                    try {
                        boost(series, random, localQueue, fitQueue);
                    } catch (Exception e) {
                        log.error("error boost iteration", e);
                    }

                    if (fitQueue.size() >= vssaCount || !fitting.get()){
                        break;
                    }
                }
            }));
        }

        futures.forEach(f -> {
            try {
                f.get();
            } catch (Exception e) {
                log.error("error boost", e);
            }
        });


        queue.forEach(VSSA::clear);
        queue.clear();

        for (int i = 0; i < vssaCount; ++i){
            queue.add(fitQueue.poll());
        }

        fitting.set(false);
    }

    private void boost(List<Double> series, Random random, Queue<VSSA> localQueue, Queue<VSSA> fitQueue) {
        int N;

        VSSA vssa;
        VSSA local = localQueue.poll();

        if (local == null) {
            //150*sin(2*3.14*x/150), x from 0 to 150

            N = random.nextInt(this.N - 2*M - 4) + 2*M + 4;
            int L = random.nextInt(N/2 - M - 1) + M + 1;
            int P = random.nextInt(L - 1) + 1;

            vssa = new VSSA(N, L, P, M);
        }else{
            N = local.getRangeLength();

            vssa = new VSSA(local.getRangeLength(), local.getWindowLength(), local.getEigenfunctionsCount(), local.getPredictionPointCount());
            vssa.setIndex(local.getIndex() + 1);
        }

        int error = 0;

        for (int t = 0; t < trainCount; ++t){
            int size = series.size() - N - M;

            int start = size > 0 ? random.nextInt(size) : 0;

            double[] train = new double[N];

            for (int i = 0; i < train.length; ++i){
                train[i] = series.get(start + i);
            }

            double[] forecasts = vssa.execute(train);

            double forecast = getTarget(forecasts, N, M) - forecasts[N - 1];
            double test = getTarget(series, start + N, M) - series.get(start + N - 1);

            if (Double.isNaN(forecast) || (int)Math.signum(test) != (int)Math.signum(forecast)){
                error++;
            }

            if (fitQueue.size() >= vssaCount || !fitting.get()){
                return;
            }
        }

        double trainError = (double) error/trainCount;
        vssa.setError(trainError);

        if (trainError <= threshold){
            fitQueue.add(vssa);

            log.debug(fitQueue.size() + " " + trainError + " " + vssa.getName() + " " + vssa.getIndex());
        }else{
            vssa.setIndex(vssa.getIndex() - 2);

            if (vssa.getIndex() > 0){
                fitQueue.add(vssa);

                log.debug(fitQueue.size() + " " + trainError + " " + vssa.getName() + " " + vssa.getIndex());
            }else{
                vssa.clear();
            }
        }
    }

    public double execute(double[] series){
        int forecast = 0;

        int index = 0;

        for (VSSA vssa : queue){
            int N = vssa.getRangeLength();

            double[] seriesF = new double[N];

            int start = series.length - N;

            System.arraycopy(series, start, seriesF, 0, N);

            double[] forecasts = vssa.execute(seriesF);

            forecast += Math.signum(getTarget(forecasts, N, M) - forecasts[N - 1]);

            if (++index > vssaCount){
                break;
            }
        }

        return forecast;
    }

    public double getTarget(List<Double> series, int start, int size){
        return series.get(start + size - 1);
    }

    public double getTarget(double[] series, int start, int size){
        return series[start + size - 1];
    }

    public double getTarget1(double[] series, int start, int size){
        double sum = 0;

        for (int i = start; i < start + size && i < series.length; ++i){
            sum += series[i];
        }

        return sum/size;
    }

    public double getTarget2(double[] series, int start, int size){
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
