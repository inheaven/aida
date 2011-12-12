package ru.inhell.aida.gui;

import ru.inhell.aida.inject.AidaInjector;
import ru.inhell.aida.quik.QuikException;
import ru.inhell.aida.quik.QuikService;
import ru.inhell.aida.trader.AlphaTraderService;

import javax.swing.*;
import java.awt.*;

/**
 * @author Anatoly A. Ivanov java@inheaven.ru
 *         Date: 21.03.11 18:37
 */
public class Aida {
    private static JPanel oraclePanel;

    public static void main(String... args) throws QuikException {
        //Frame
        JFrame frame = new JFrame("Aida");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane();
        frame.add(tabbedPane);

        oraclePanel = new JPanel(new GridLayout(2,2));
        tabbedPane.addTab("Предсказатели", oraclePanel);

        process(48, 50);

        frame.pack();
        frame.setVisible(true);

        AidaInjector.getInstance(QuikService.class).connect(ru.inhell.aida.Aida.getQuikDir());
    }

    private static void process(int... ids){
        for (int id :  ids){
            oraclePanel.add(new AlphaOracleChart((long)id));
            AidaInjector.getInstance(AlphaTraderService.class).process((long)id);
        }
    }
}

