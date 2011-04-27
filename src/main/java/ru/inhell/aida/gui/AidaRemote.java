package ru.inhell.aida.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 06.04.11 16:37
 */
public class AidaRemote {
    private static JPanel root;

    public static void main(String... args){
        JFrame frame = new JFrame("Aida");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        root = new JPanel(new GridLayout(3,3));
        frame.add(root);

        process(1,2,14,19,27,28,31,32,33);

        frame.pack();
        frame.setVisible(true);
    }

    private static void process(int... ids){
        for (int id : ids){
            root.add(new AlphaOracleChart((long)id, 30));
        }
    }
}
