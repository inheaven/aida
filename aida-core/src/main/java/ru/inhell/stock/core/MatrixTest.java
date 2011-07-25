package ru.inhell.stock.core;

import org.ujmp.core.Matrix;
import org.ujmp.core.MatrixFactory;
import org.ujmp.core.calculation.Calculation;
import org.ujmp.core.enums.FileFormat;
import org.ujmp.core.enums.ValueType;
import org.ujmp.core.util.UJMPSettings;
import org.ujmp.gui.plot.MatrixPlot;
import ru.inhell.aida.acml.ACML;
import ru.inhell.aida.matrix.AcmlMatrix;


import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 04.06.2010 3:02:03
 */
public class MatrixTest {
    public static void main(String... args) throws Exception {

//        Matrix matrix = MatrixFactory.importFromFile(FileFormat.CSV, "I:\\Java\\Projects-2010\\ru.inhell.stock\\data\\GAZP_100126_100626.csv",";");
//
//        Matrix price = matrix.selectColumns(Calculation.Ret.LINK, 7)
//                .delete(Calculation.Ret.LINK, new long[]{0,0});
//                .times(Calculation.Ret.LINK, false, 100);


//        double[] fullTS = price.transpose().toDoubleArray()[0];

//        double[] g = VSSA.vssa(fullTS, 500, 15, 30);
//        MatrixFactory.linkToArray(g).showGUI();
//
//        test2(fullTS);

//        test3(fullTS);

//        test5();
//
        AcmlMatrix m = new AcmlMatrix(5000, 5000);
        m.rand(Calculation.Ret.ORIG);

        AcmlMatrix m1 = new AcmlMatrix(5000, 5000);
        m1.rand(Calculation.Ret.ORIG);

        Long time = System.currentTimeMillis();

        m.mtimes(m1);

        System.out.println("ACML. The time is " + (System.currentTimeMillis() - time));

        UJMPSettings.setNumberOfThreads(3);
        Matrix m2 = MatrixFactory.rand(ValueType.DOUBLE, 5000, 5000);
        Matrix m3 = MatrixFactory.rand(ValueType.DOUBLE, 5000, 5000);

        time = System.currentTimeMillis();

        m2.mtimes(m3);

        System.out.println("UJMP. The time is " + (System.currentTimeMillis() - time));



    }

    private static void test5(){
        double[] x = new double[]{11,21,31,4,5,6,7,8,9,10};
//
//        Matrix m = MatrixFactory.dense(ValueType.DOUBLE, 5,6);
//
//        for (int i = 0; i < 5 ; i++){
//            for (int j = 0; j < 6; j++){
//                m.setAsDouble(x[i+j], i, j);
//            }
//        }


//        for (int i = 1024; i < 2049; i+= 512) {
//            Matrix m2 = MatrixFactory.dense(ValueType.FLOAT, i, i);
//
//            long time = System.currentTimeMillis();
//
//            Matrix[] svd = m2.svd();
//
//            System.out.println("UJMP (s): " + (System.currentTimeMillis() - time));
//        }
//
//        Matrix[] svd = m.svd();
//
//        Matrix U = svd[0];
//        Matrix S = svd[1];
//        Matrix V = svd[2];
//
//        System.out.println(m.toString());
//        System.out.println(U.toString());
//        System.out.println(S.toString());
//        System.out.println(V.toString());

        BasicSSA basicSSA = new BasicSSA(x.length, 5, 3);

        basicSSA.execute(x);

    }

    private static void test3(double[] fullTS){
        double[] timeSeries = new double[990];

        BasicSSA basicSSA = new BasicSSA(timeSeries.length, 445, 15);

        Long time;

        for (int i = 0; i < 990; ++i) {
            System.out.println(i);

            time = System.currentTimeMillis();

            System.arraycopy(fullTS, i, timeSeries, 0, 990);

            basicSSA.execute(timeSeries);

            System.out.println("t2: " + (System.currentTimeMillis() - time) + "\n");            
        }

    }

    private static void test1(double[] fullTS) throws IOException {
        int N = fullTS.length;
        int L = N/2;
        int P = 10;

        double[] timeSeries = new double[N];

        System.arraycopy(fullTS, fullTS.length - N, timeSeries, 0, N);

        BasicSSA basicSSA = new BasicSSA(timeSeries.length, L, P);

        double[][] Yi = basicSSA.execute(timeSeries).getG().transpose().toDoubleArray();

        JFrame frame = new JFrame();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);


        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());

        JPanel panel2 = new JPanel();
        panel2.setLayout(new GridLayout(0,1));

        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, panel, panel2);
        frame.setContentPane(splitPane);


        MatrixPlot mpTS = new MatrixPlot(MatrixFactory.linkToArray(timeSeries));
        mpTS.getPlotSettings().setShowRunningAverage(false);
        mpTS.getPlotSettings().setShowPlotBackGround(false);
        panel.add(mpTS);

        for (int i = 0; i < P; ++i){
            addMatrixPlot(panel2, Yi[i]);
        }

        frame.pack();
        frame.setVisible(true);
    }

    private static void test2(final double[] fullTS) throws IOException{
        final int N = 990;
        final int M = 45;
        final int L = 445;
        final int P = 15;

        final JFrame frame = new JFrame();
        frame.getRootPane().setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        final double[] g = new double[N];

        final Matrix matrix = MatrixFactory.dense(ValueType.DOUBLE, N+M, 3);

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
                        VSSA vssa = new VSSA(N, L, P, M);

                        for (int k = index; k < fullTS.length-N; k++){

                            System.arraycopy(fullTS, index, g, 0, N);

                            long time = System.currentTimeMillis();

                            double[] f = vssa.execute(g);

                            System.out.println("t" + k  + ": " + (System.currentTimeMillis() - time));

                            for (int i = 0; i < M+N; ++i){
                                matrix.setAsDouble(i==N ? f[i] + 2 : f[i], i, 0);
                                matrix.setAsDouble(fullTS[i+index], i, 1);
                                matrix.setAsDouble(f[i], i, 2);
                            }

                            try {
                                matrix.exportToFile(FileFormat.CSV, "I:\\Java\\Projects-2010\\ru.inhell.stock\\data\\test-01-06-990-45-445-15\\test"+k+".csv", ";");
                            } catch (IOException e1) {
                                e1.printStackTrace();
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

    private static void addMatrixPlot(JPanel panel, double[] data){
        MatrixPlot mp = new MatrixPlot(MatrixFactory.linkToArray(data));
        mp.getPlotSettings().setShowRunningAverage(false);
        mp.getPlotSettings().setShowPlotBackGround(false);
        panel.add(mp);
    }
}
