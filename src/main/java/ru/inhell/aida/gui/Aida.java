package ru.inhell.aida.gui;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.03.11 18:37
 */
public class Aida {
    public static void main(String... args){
        //Frame
        JFrame frame = new JFrame();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new BorderLayout());
        frame.add(root);

        root.add(new AlphaOracleChart(), BorderLayout.CENTER);
        root.add(new AlphaOraclePanel(), BorderLayout.SOUTH);


        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }
}
