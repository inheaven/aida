package ru.inhell.aida.gui;

import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.trader.AlphaTraderService;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.03.11 18:37
 */
public class Aida {
    public static void main(String... args){
        //Frame
        JFrame frame = new JFrame("Aida");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel root = new JPanel(new GridLayout(2,1));
        frame.add(root);

        root.add(new AlphaOracleChart(1L));
        root.add(new AlphaOracleChart(2L));

        AidaInjector.getInstance(AlphaTraderService.class).process(1L);
        AidaInjector.getInstance(AlphaTraderService.class).process(2L);

        frame.setLocationRelativeTo(null);
        frame.pack();
        frame.setVisible(true);
    }
}
