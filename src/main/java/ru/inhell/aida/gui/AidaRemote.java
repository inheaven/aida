package ru.inhell.aida.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.04.11 16:37
 */
public class AidaRemote {
    private static JPanel oraclePanel;

    public static void main(String... args){
        JFrame frame = new JFrame("Aida");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        frame.add(tabbedPane);

        oraclePanel = new JPanel(new GridLayout(3,3));
        tabbedPane.addTab("Предсказатели", oraclePanel);

        process(3,4,14,19,27,28,31,32,33);

        tabbedPane.addTab("Статистика", new AlphaStatisticPanel());

        frame.pack();
        frame.setSize(1024, 768);
        frame.setVisible(true);
    }

    private static void process(int... ids){
        for (int id : ids){
            oraclePanel.add(new AlphaOracleChart((long)id, 30));
        }
    }
}
