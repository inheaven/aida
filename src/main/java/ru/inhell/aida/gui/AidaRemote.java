package ru.inhell.aida.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.04.11 16:37
 */
public class AidaRemote {
    public static void main(String... args){
        JFrame frame = new JFrame("Aida");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new GridLayout(1,2));
        frame.add(root);

        root.add(new AlphaOracleChart(3L, 60));
        root.add(new AlphaOracleChart(2L, 60));

        frame.pack();
        frame.setVisible(true);
    }
}
