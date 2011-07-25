package ru.inhell.aida.test;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.enums.FileFormat;
import org.ujmp.core.enums.ValueType;
import org.ujmp.gui.plot.MatrixPlot;
import ru.inhell.aida.mybatis.SqlSessionFactory;
import ru.inhell.aida.ssa.VectorForecastSSA;
import ru.inhell.stock.core.VSSA;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Random;

/**
 * @author Anatoly A. Ivanov java@inhell.ru
 *         Date: 02.12.10 1:22
 */
public class VectorForecastTest {
    public static void main(String... args) throws IOException {
//        VectorForecastTest vectorForecastTest = new VectorForecastTest();
//
//        vectorForecastTest.test2();
        SqlSessionFactory.getSessionManager();
    }



    private void test1(){
        int N = 1000;
        int L = 500;
        int P = 20;
        int M = 100;

        int R = 10;

        float[] ts = new float[N];
        double[] tsD = new double[N];

        Random random = new Random();

        for (int i=0; i < N; ++i){
            ts[i] = random.nextFloat()*180;
            tsD[i] = random.nextDouble()*180;
        }

        //VectorForecastSSA
//        System.out.println(Arrays.toString(new VectorForecastSSA(10, 5, 3, 2).execute(ts)));
        long time = System.currentTimeMillis();
        VectorForecastSSA v1 = new VectorForecastSSA(N, L, P, M);
        for (int i=0; i < R; ++i) {
            v1.execute(ts, new float[N+M+L-1]);
        }
        System.out.println("time1: " + (System.currentTimeMillis() - time));

        //VSSA
//        System.out.println(Arrays.toString(new VSSA(10, 5, 3, 2).execute(tsD)));
        VSSA v2 = new VSSA(N, L, P, M);
        for (int i=0; i < R; ++i) {
            v2.execute(tsD);
        }
        System.out.println("time2: " + (System.currentTimeMillis() - time));
    }

    private  void test2() throws IOException {
        Matrix importFromCsv = MatrixFactory.importFromFile(FileFormat.CSV, "E:\\Java\\Projects-2010\\aida\\data\\GAZP_091202_101202.csv",";");

        Matrix price = importFromCsv.selectColumns(Calculation.Ret.LINK, 4);

        final float[] fullTS = price.transpose().toFloatArray()[0];

        final int N = 480;
        final int M = 30;
        final int L = 240;
        final int P = 8;

        final JFrame frame = new JFrame();
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final float[] g = new float[N];

        final float[] f = new float[N+M+L-1];

        final Matrix matrix = MatrixFactory.dense(ValueType.FLOAT, N + M, 3);

        final MatrixPlot mp = new MatrixPlot(matrix);
        mp.getPlotSettings().setShowRunningAverage(false);


        frame.add(mp, BorderLayout.CENTER);

        final JButton go = new JButton("PREDICT");
        go.addActionListener(new ActionListener(){
            int index = 17;

            @Override
            public void actionPerformed(ActionEvent e) {


                new Thread(new Runnable(){
                    @Override
                    public void run() {
                        VectorForecastSSA vssa = new VectorForecastSSA(N, L, P, M);

                        for (int k = index; k < fullTS.length-N; k++){

                            System.arraycopy(fullTS, index, g, 0, N);

                            long time = System.currentTimeMillis();

                            vssa.execute(g, f);

                            System.out.println("t" + k  + ": " + (System.currentTimeMillis() - time));

                            for (int i = 0; i < M+N; ++i){
                                matrix.setAsFloat(i == N ? f[i] + 2 : f[i], i, 0);
                                matrix.setAsFloat(fullTS[i+index], i, 1);
                                matrix.setAsFloat(f[i], i, 2);
                            }

                            SwingUtilities.invokeLater(new Runnable(){
                                @Override
                                public void run() {
                                    go.setText(String.valueOf("PREDICT: " + index));
                                    go.repaint();

                                    mp.repaint();
                                }
                            });

                            ++index;
                        }
                    }
                }).start();

            }
        });

        frame.add(go, BorderLayout.SOUTH);

        frame.pack();
        frame.setVisible(true);
    }

    //-Xmx512M -XX:ParallelGCThreads=20 -XX:+UseConcMarkSweepGC -XX:+UseParNewGC -XX:SurvivorRatio=8
    // -XX:TargetSurvivorRatio=90 -XX:MaxTenuringThreshold=31 -XX:+AggressiveOpts -XX:+UseBiasedLocking
}
